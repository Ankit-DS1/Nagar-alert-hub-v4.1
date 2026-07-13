package com.nagaralert.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Transcribes audio via Groq's Whisper API (whisper-large-v3).
 */
@Service
public class GroqWhisperService {

    private static final Logger log = LoggerFactory.getLogger(GroqWhisperService.class);
    private static final String API_URL = "https://api.groq.com/openai/v1/audio/transcriptions";
    private static final String MODEL = "whisper-large-v3";

    @Value("${groq.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String transcribe(MultipartFile audio) throws Exception {
        if (!apiKeyPresent()) {
            throw new IllegalStateException("Groq API key not configured");
        }
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("No audio file provided");
        }

        String boundary = "----NagarAlert" + UUID.randomUUID();
        byte[] body = buildMultipartBody(boundary, audio);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.error("Groq Whisper error: {}", response.body());
            throw new RuntimeException("Transcription failed (HTTP " + response.statusCode() + ")");
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("text").asText("").trim();
    }

    private byte[] buildMultipartBody(String boundary, MultipartFile audio) throws Exception {
        String filename = audio.getOriginalFilename() != null ? audio.getOriginalFilename() : "recording.webm";
        String contentType = audio.getContentType() != null ? audio.getContentType() : "audio/webm";

        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
        sb.append(MODEL).append("\r\n");

        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(filename).append("\"\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n\r\n");

        byte[] header = sb.toString().getBytes(StandardCharsets.UTF_8);
        byte[] audioBytes = audio.getBytes();
        byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        byte[] result = new byte[header.length + audioBytes.length + footer.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(audioBytes, 0, result, header.length, audioBytes.length);
        System.arraycopy(footer, 0, result, header.length + audioBytes.length, footer.length);
        return result;
    }

    private boolean apiKeyPresent() {
        return apiKey != null && !apiKey.isBlank()
                && !apiKey.equals("YOUR_GROQ_API_KEY_HERE")
                && !apiKey.equals("gsk_xxxxxxxxxxxxxx")
                && !apiKey.equals("demo_key");
    }
}
