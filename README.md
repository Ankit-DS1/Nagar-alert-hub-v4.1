# 🚨 Nagar Alert Hub

A real-time civic engagement platform that closes the loop between citizens and government departments.

### 🎯 Problem Statement Alignment
This project directly tackles the **"Inclusive Society & Digital Trust"** problem statement. Current municipal reporting systems feel like black holes—citizens report issues but never see action, destroying civic trust. **Nagar Alert Hub** rebuilds this trust through extreme transparency: live visual maps, AI-powered immediate triage, and a transparent feedback loop where citizens can track their exact complaint from "Pending" to "Resolved". 

### 💻 Tech Stack
* **Backend:** Java 17, Spring Boot, Spring Security, Spring Data MongoDB
* **AI / ML:** Groq API for instant NLP department classification (with a custom Bigram/Keyword fallback layer for 100% resilience).
* **Frontend:** Thymeleaf, TailwindCSS (Glassmorphism UI), Leaflet.js (OpenStreetMap)
* **APIs:** OpenStreetMap Nominatim for Geocoding, Twilio API for WhatsApp Notifications, OAuth2 (Google/Facebook).

### ⚙️ How It Works (in 5 Lines)
1. **Report:** A citizen logs in via Google/Facebook (OAuth2), spots a problem, and submits a 10-second report with their location and phone number.
2. **AI Triage:** Our Groq-powered AI instantly reads the natural language, predicts the correct department (e.g., *Municipal*), and assigns a severity.
3. **Public Map:** The incident instantly drops onto the live Leaflet.js city map, proving to the community that the report was registered.
4. **Admin Action:** The Municipal admin logs into their securely filtered dashboard, sees the new alert, and updates its status.
5. **Trust Loop:** The citizen receives real-time **WhatsApp notifications** via Twilio as the issue progresses, and can track it via `/my-alerts`.

---

### 🎥 Demo Flow (For Judges)

1. **The Hook (Homepage):** Open the app. Point out the sleek **Live Map**. Explain that this is what transparency looks like.
2. **The Submission:** Click "Report Issue". Type a natural phrase: *"There is a huge fire near the bakery"* and enter phone number `9999999999`. Submit.
3. **The Magic (AI):** Show that the system automatically detected the department as **FIRE** and mapped the coordinates without manual dropdowns. 
4. **The Citizen View:** Go to "Track My Alerts" (`/my-alerts`). Show the issue is currently **Pending**. Highlight that a WhatsApp notification was instantly sent.
5. **The Admin View:** Click Admin Login. Click on the **Fire Department**. Log in. (The system automatically filters to only show Fire emergencies).
6. **The Resolution:** Mark the fire alert as **In Progress**, then **Resolved**. 
7. **The Loop Closed:** Go back to the Citizen View to show it is now "Resolved", proving the government took action.

---

### 📸 Screenshot: Live Map Dashboard

![Live Map Dashboard](screenshots/live_map_placeholder.png)
*(Replace this placeholder with a screenshot of the Leaflet Map and Glassmorphism UI before submission)*
