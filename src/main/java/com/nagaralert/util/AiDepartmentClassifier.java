package com.nagaralert.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nagaralert.model.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI-powered department classifier using Groq API.
 * Falls back to keyword-based routing if the API is unavailable.
 */
@Component
public class AiDepartmentClassifier {

    private static final Logger log = LoggerFactory.getLogger(AiDepartmentClassifier.class);

    @Value("${groq.api.key:}")
    private String apiKey;

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.3-70b-versatile"; // fast + cheap
    private static final int    TIMEOUT = 5; // seconds — fail fast, hit fallback

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------
    
    public record ClassificationResult(String department, String reason) {}

    public ClassificationResult classifyDepartment(String description) {
        if (apiKeyPresent()) {
            try {
                return classifyWithGroq(description);
            } catch (Exception e) {
                log.warn("Groq API failed ({}), using keyword fallback", e.getMessage());
            }
        }
        return new ClassificationResult(keywordFallback(description), "Determined via keyword heuristic fallback");
    }

    public Severity classifySeverity(String description) {
        // SeverityDetector already works well — keep it.
        // Only department routing had the keyword collision problem.
        return SeverityDetector.determineSeverity(description);
    }

    // -----------------------------------------------------------------------
    // Groq API call
    // -----------------------------------------------------------------------

    private ClassificationResult classifyWithGroq(String description) throws Exception {
        String systemPrompt = """
                You are a civic incident classifier for a city alert system.
                Given a citizen's incident description, respond with ONLY a valid JSON object — no explanation, no markdown.
                
                Format:
                {"department": "<dept>", "reason": "<one short phrase>"}
                
                Choose department from exactly these options:
                - Police     (crime, theft, assault, suspicious activity, violence)
                - Fire       (fire, smoke, gas leak, burning smell, explosion)
                - Medical    (accident, injury, unconscious, bleeding, heart attack)
                - Electrical (power outage, electric shock, fallen wire, voltage, transformer, streetlight fault)
                - Municipal  (pothole, road damage, pipe burst, water leakage, sewage, garbage, waterlogging, broken footpath)
                - Traffic    (signal fault, road block, accident causing traffic, illegal parking)
                
                Important: "water", "pipe", "drain", "leakage" always → Municipal.
                "current" meaning water flow → Municipal. Only "electric current" → Electrical.
                """;

        String userMessage = "Classify this incident: " + description;

        // Build request body
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", MODEL);
        // max_tokens is max_completion_tokens or max_tokens in OpenAI format
        body.put("max_tokens", 120);

        ArrayNode messagesArray = body.putArray("messages");
        
        ObjectNode sysMsg = messagesArray.addObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", systemPrompt);

        ObjectNode userMsg = messagesArray.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);

        String requestJson = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Groq API error body: {}", response.body());
            throw new RuntimeException("API returned HTTP " + response.statusCode());
        }

        return parseDepartmentFromResponse(response.body());
    }

    private ClassificationResult parseDepartmentFromResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();

        // Strip markdown fences if Groq added them
        text = text.replaceAll("```json", "").replaceAll("```", "").trim();

        JsonNode result = objectMapper.readTree(text);
        String dept = result.path("department").asText();

        // Validate it's one of our 6 departments
        List<String> valid = List.of("Police", "Fire", "Medical", "Electrical", "Municipal", "Traffic");
        if (!valid.contains(dept)) {
            log.warn("Groq returned unknown department '{}', using fallback", dept);
            throw new RuntimeException("Unknown department: " + dept);
        }

        log.info("Groq classified as: {} (reason: {})", dept, result.path("reason").asText());
        return new ClassificationResult(dept, result.path("reason").asText());
    }

    // -----------------------------------------------------------------------
    // Keyword fallback (improved — no ambiguous words)
    // -----------------------------------------------------------------------

    private String keywordFallback(String description) {
        String d = description.toLowerCase();
        Map<String, Integer> scores = new HashMap<>();

        // --- Municipal (water/road/civic infrastructure) ---
        scoreKeywords(scores, "Municipal", d, List.of(
                "pipe", "pipeline", "water pipe", "pipe burst", "burst pipe",
                "water coming", "water leaking", "water leak", "leakage",
                "sewage", "drainage", "drain blocked", "drain overflow",
                "pothole", "road damage", "pavement", "footpath",
                "garbage", "waste", "waterlogging", "flood", "manhole",
                "municipal", "water supply", "tap", "overhead tank"
        ));

        // --- Electrical (unambiguous terms only) ---
        scoreKeywords(scores, "Electrical", d, List.of(
                "electric", "electricity", "voltage", "electrocute",
                "wire", "wiring", "live wire", "fallen wire",
                "transformer", "fuse", "switchboard", "circuit",
                "power outage", "power failure", "power cut",
                "streetlight", "street light", "pole", "spark"
        ));

        // --- Fire ---
        scoreKeywords(scores, "Fire", d, List.of(
                "fire", "flame", "smoke", "burning", "burn",
                "gas leak", "cylinder", "explosion", "blast",
                "inflammable", "fire brigade", "fire spreading"
        ));

        // --- Medical ---
        scoreKeywords(scores, "Medical", d, List.of(
                "injured", "injury", "accident", "bleeding", "blood",
                "unconscious", "fainted", "heart attack", "ambulance",
                "medical", "hospital", "fracture", "wound", "dead body"
        ));

        // --- Police ---
        scoreKeywords(scores, "Police", d, List.of(
                "theft", "robbery", "stolen", "assault", "fight",
                "crime", "criminal", "murder", "suspicious", "threat",
                "harassment", "abuse", "police", "arrest", "violence"
        ));

        // --- Traffic ---
        scoreKeywords(scores, "Traffic", d, List.of(
                "traffic", "signal", "traffic light", "road block",
                "jam", "illegal parking", "wrong side", "rash driving",
                "road accident", "vehicle", "car crash", "truck"
        ));

        // Bigram boost — double weight for specific phrases
        applyBigramBoosts(scores, d);

        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Municipal"); // safe default
    }

    private void scoreKeywords(Map<String, Integer> scores, String dept,
                               String description, List<String> keywords) {
        for (String keyword : keywords) {
            if (description.contains(keyword)) {
                scores.merge(dept, 1, Integer::sum);
            }
        }
    }

    private void applyBigramBoosts(Map<String, Integer> scores, String d) {
        // High-confidence phrase → department mappings (weight = 2)
        Map<String, String> bigrams = new HashMap<>();
        bigrams.put("water pipe",     "Municipal");
        bigrams.put("pipe burst",     "Municipal");
        bigrams.put("pipe cut",       "Municipal");
        bigrams.put("water coming",   "Municipal");
        bigrams.put("water leaking",  "Municipal");
        bigrams.put("drain blocked",  "Municipal");
        bigrams.put("road damaged",   "Municipal");
        bigrams.put("power outage",   "Electrical");
        bigrams.put("power cut",      "Electrical");
        bigrams.put("live wire",      "Electrical");
        bigrams.put("short circuit",  "Electrical");
        bigrams.put("gas leak",       "Fire");
        bigrams.put("fire broke",     "Fire");
        bigrams.put("fire spread",    "Fire");
        bigrams.put("heart attack",   "Medical");
        bigrams.put("road accident",  "Traffic");
        bigrams.put("signal fault",   "Traffic");

        for (Map.Entry<String, String> entry : bigrams.entrySet()) {
            if (d.contains(entry.getKey())) {
                scores.merge(entry.getValue(), 2, Integer::sum);
            }
        }
    }

    private boolean apiKeyPresent() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("gsk_xxxxxxxxxxxxxx") && !apiKey.equals("your-actual-key-here");
    }
}
