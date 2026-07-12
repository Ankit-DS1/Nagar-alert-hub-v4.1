package com.nagaralert.util;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DepartmentClassifier {

    // "Memory" of the AI (Word -> Map<Department, Count>)
    private Map<String, Map<String, Integer>> wordCounts = new HashMap<>();
    private Map<String, Integer> departmentCounts = new HashMap<>();
    private List<String> departments = Arrays.asList("POLICE", "FIRE", "MEDICAL", "ELECTRICAL", "MUNICIPAL", "TRAFFIC");

    // Constructor: This "Trains" the model instantly when the app starts
    public DepartmentClassifier() {
        train();
    }

    private void train() {
        // Training Data: (Sentence, Department)
        // In a real app, you would load this from a CSV file
        learn("Theft robbery fight gun crime murder", "POLICE");
        learn("Fire smoke burning blast flames explosion", "FIRE");
        learn("Ambulance heart attack injury blood accident sick", "MEDICAL");
        learn("Wire shock light pole power cut fuse voltage", "ELECTRICAL");
        learn("Water pothole garbage road drain sewage dog park", "MUNICIPAL");
        learn("Traffic jam signal congestion vehicle blocked car bus", "TRAFFIC");
    }

    // Helper to teach the model
    private void learn(String text, String department) {
        String[] words = text.toLowerCase().split("\\s+");
        departmentCounts.put(department, departmentCounts.getOrDefault(department, 0) + 1);

        for (String word : words) {
            wordCounts.putIfAbsent(word, new HashMap<>());
            Map<String, Integer> deptMap = wordCounts.get(word);
            deptMap.put(department, deptMap.getOrDefault(department, 0) + 1);
        }
    }

    // The Prediction Logic
    public String predict(String text) {
        String[] words = text.toLowerCase().split("\\s+");
        String bestDept = "MUNICIPAL"; // Default
        double maxScore = -1.0;

        for (String dept : departments) {
            double score = 0.0;
            // Calculate probability score
            for (String word : words) {
                if (wordCounts.containsKey(word)) {
                    int count = wordCounts.get(word).getOrDefault(dept, 0);
                    score += count; // Simple frequency scoring
                }
            }
            if (score > maxScore) {
                maxScore = score;
                bestDept = dept;
            }
        }
        return bestDept;
    }
}