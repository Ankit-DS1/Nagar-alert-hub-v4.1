# Nagar Alert Hub - Project Summary

## Overview
**Nagar Alert Hub** is a Spring Boot web application designed to empower citizens to report public disruptions and emergencies (e.g., accidents, fires, potholes). The system streamlines civic reporting by automatically categorizing incidents, tracking their status, and allowing the community to stay informed with real-time updates and secure logins.

## Key Features & Architecture

### 1. Alert Reporting & Media Uploads
- **Submission:** Citizens can submit alerts with a description, location, and optionally a phone number.
- **Photo Attachments:** Citizens can attach photos of incidents (e.g., broken pipes, hazards). Photos are securely stored locally in an `/uploads` directory and presented as thumbnails in the Admin Panel for visceral, credible evidence.
- **Geocoding:** If a location is provided manually without map coordinates, the system dynamically geocodes it using OpenStreetMap Nominatim before submission.

### 2. Live Map Clustering & Feed
- **Interactive Map:** Uses **Leaflet.js** and OpenStreetMap tiles to visualize all active alerts.
- **Hotspot Clustering:** Integrates the **Leaflet.markercluster** plugin. When multiple alerts occur in the same area, they aggregate into numbered cluster bubbles to prevent UI clutter. The clusters auto-expand dynamically on zoom.
- **Live Feed:** The map feed displays real-time department emojis to visually indicate routing, and alerts are color-coded by severity.

### 3. Inclusive Multilingual Support
- **UI Translation:** Features a premium, arcade-style language toggle across all citizen and admin pages, allowing seamless switching between **English (EN)** and **Hindi (हिं)**.
- **Native Hindi Processing:** The AI backend natively understands and categorizes Hindi descriptions, requiring no extra translation layer for the classifier.

### 4. Automated Classification (Groq AI / ML)
- **Department Prediction:** Powered by the **Groq API**, the system uses an AI classifier to intelligently route alerts to the correct department (Police, Fire, Medical, Electrical, Municipal, Traffic) based on natural language understanding. 
- **Keyword Fallback (Resilience):** If the API times out or is unreachable, the system automatically and silently falls back to a custom, heavily weighted keyword & bigram heuristic classifier, ensuring zero downtime during emergencies.
- **Severity Detection:** Automatically assigns a severity level (Critical, High, Medium, Low) based on critical keyword and domain phrase matching.

### 5. Admin Portal, Analytics & Security
- **Spring Security Integration:** The application uses **Spring Security** to securely isolate the `/admin/**` endpoints. 
- **Analytics Dashboard:** Admins can access a dedicated dashboard powered by **Chart.js**, featuring donut and bar charts for key metrics like Total Alerts, Resolution Rates, and Alerts by Severity/Department.
- **Status Management:** Admins can securely view, search, verify, change status, and delete alerts. 

### 6. OAuth2 Authentication
- **Social Login:** Citizens authenticate securely using Google or Facebook via Spring Security OAuth2.
- **Profile Management:** User profiles and authentication details are automatically synced and stored in MongoDB.

### 7. Real-time WhatsApp Notifications
- **Twilio Integration:** Citizens receive instantaneous WhatsApp notifications when their alert is registered and whenever the admin updates the status (e.g., Pending -> In Progress -> Resolved), ensuring a closed trust loop.

### 8. Auto-Cleanup Thread
- A `@Scheduled` background task runs every hour. It automatically sweeps the database and removes `RESOLVED` alerts that are older than 24 hours. This keeps the database clean and relevant.

### 9. Technical Stack
- **Backend:** Java 17, Spring Boot, Spring Security, OAuth2 Client
- **Database:** MongoDB mapped via Spring Data MongoDB
- **Frontend:** Thymeleaf templates, TailwindCSS, Leaflet.js (maps), Chart.js (analytics)
- **Integrations:** Twilio API (WhatsApp Notifications), Groq API (AI Classification), OpenStreetMap Nominatim (Geocoding)
