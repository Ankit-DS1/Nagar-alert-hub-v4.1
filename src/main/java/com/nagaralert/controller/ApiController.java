package com.nagaralert.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nagaralert.service.GroqWhisperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@RestController
public class ApiController {

    private final GroqWhisperService whisperService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiController(GroqWhisperService whisperService) {
        this.whisperService = whisperService;
    }

    /**
     * Reverse-geocodes coordinates via OpenStreetMap Nominatim (server-side proxy).
     */
    @GetMapping("/api/reverse-geocode")
    public ResponseEntity<Map<String, String>> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lon) {
        try {
            String url = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%s&lon=%s",
                    lat, lon);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "NagarAlertHub/1.0")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return ResponseEntity.ok(Map.of("address", lat + ", " + lon));
            }
            JsonNode data = objectMapper.readTree(response.body());
            String address = data.path("display_name").asText(lat + ", " + lon);
            return ResponseEntity.ok(Map.of("address", address));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("address", lat + ", " + lon));
        }
    }

    /**
     * Transcribes voice audio via Groq Whisper and returns the text.
     */
    @PostMapping("/api/transcribe")
    public ResponseEntity<Map<String, String>> transcribe(@RequestParam("audio") MultipartFile audio) {
        try {
            String text = whisperService.transcribe(audio);
            return ResponseEntity.ok(Map.of("text", text));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("error", "Voice transcription unavailable — Groq API key not configured"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Transcription failed: " + e.getMessage()));
        }
    }
}
