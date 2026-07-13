# Nagar Alert Hub — Project Summary (v4.1)

## Overview

**Nagar Alert Hub** is a Spring Boot civic-tech web application that empowers citizens to report public disruptions and emergencies (fires, accidents, potholes, power outages) in real time. It uses AI for automatic classification, Leaflet.js for live maps, and Twilio for WhatsApp feedback — ensuring every complaint is visible, trackable, and resolved.

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     FRONTEND (Thymeleaf)                        │
│  TailwindCSS  |  Leaflet.js  |  Chart.js  |  Accessibility UI  │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP / REST
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                  SPRING BOOT APPLICATION                        │
│                                                                 │
│  Controllers: Home, Auth, Admin, API, Profile                   │
│  Services:    AlertService, AlertManager, WhatsAppNotification, │
│               GroqWhisperService                                │
│  Utils:       AiDepartmentClassifier, SeverityDetector,        │
│               DepartmentClassifier (fallback), GeoUtils         │
│  Threads:     AutoCleanup (@Scheduled hourly)                   │
│  Security:    Spring Security + OAuth2 (Google/Facebook)        │
└──────────────┬───────────────────────────┬──────────────────────┘
               │                           │
               ▼                           ▼
  ┌────────────────────┐       ┌───────────────────────┐
  │   MongoDB Atlas    │       │   External APIs        │
  │   nagaralertdb     │       │                        │
  │   - alerts         │       │  Groq API (NLP + STT)  │
  │   - users          │       │  Twilio (WhatsApp)     │
  └────────────────────┘       │  OSM Nominatim (Geo)   │
                               └───────────────────────┘
```

---

## Feature Breakdown

### 1. Alert Reporting — Voice, Text, Photo

**Flow diagram:**
```
Citizen Input
     │
     ├── 🎙️ Voice ──▶ MediaRecorder (WebM/OGG)
     │                     │
     │              POST /api/transcribe
     │                     │
     │              Groq Whisper large-v3
     │                     │
     │              text ──▶ Description field (auto-fill)
     │
     ├── 📝 Text ──▶ Textarea (manual entry)
     │
     ├── 📍 Location ──▶ GPS auto-detect  OR
     │                   Manual text entry  OR
     │                   Map modal pin pick
     │                         │
     │                   Nominatim geocoding
     │                   (lat/lng resolved)
     │
     └── 📷 Photo ──▶ Drag & drop / file picker
                       Stored in /uploads
                       Shown as thumbnail in Admin
```

**Voice recording states:**
```
  [IDLE]          [RECORDING]        [TRANSCRIBING]     [DONE]
   🎙️ icon    →   🔴 waveform    →    ⏳ spinner    →  ✅ text filled
                  |||||||||              
                  timer: 0:23           Groq Whisper
```

---

### 2. AI Classification Pipeline

```
Description Text (natural language, Hindi or English)
          │
          ▼
  ┌───────────────────┐
  │   Groq LLaMA API  │  ──▶ {"department": "Fire", "severity": "HIGH"}
  │   (primary)       │
  └─────────┬─────────┘
            │ (on timeout / failure)
            ▼
  ┌───────────────────┐
  │  Keyword + Bigram │  ──▶ Fallback classification (100% uptime)
  │  Classifier       │       e.g. "fire" → FIRE dept
  └───────────────────┘

  Severity scale:  CRITICAL ▶ HIGH ▶ MEDIUM ▶ LOW
  Departments:     Police  Fire  Medical  Electrical  Municipal  Traffic
```

---

### 3. Live Map & Alert Feed

```
  ┌────────────────────────────────────────────────────┐
  │              Leaflet.js Interactive Map             │
  │                                                    │
  │   👮  ──── single alert pin (blue = LOW)           │
  │   🚒  ──── single alert pin (orange = HIGH)        │
  │   🔴  ──── single alert pin (red = CRITICAL)       │
  │   🟢  ──── resolved pin (green)                    │
  │                                                    │
  │   ③  ──── cluster bubble (3 alerts in same area)  │
  │        auto-expands on zoom                        │
  └────────────────────────────────────────────────────┘

  Alert Feed (below map):
  ┌─────────────────────────────────────────────────┐
  │ Status filter: [All] [Critical] [Pending] [Resolved]│
  │ Dept filter:   [All] [👮] [🚒] [🚑] [⚡] [🏗️] [🚦] │
  │                 ▲ Combined filtering logic         │
  └─────────────────────────────────────────────────┘

  Alert Card (collapsed):
  ┌──────────────────────────────────────────┐
  │▌ ⏳ PENDING  HIGH  📍 Gandhi Maidan      │
  │  "Fire near the bakery on main road"     │
  │  🕒 13 Jul  🏢 Fire  [Me Too 3]  🚒  ⌄  │
  └──────────────────────────────────────────┘

  Alert Card (expanded — click to toggle):
  ┌──────────────────────────────────────────┐
  │▌ ⏳ PENDING  HIGH  📍 Gandhi Maidan      │
  │  "Fire near the bakery..."               │
  │  🕒 13 Jul  🏢 Fire  [Me Too 3]  🚒  ⌃  │
  │  ──────────────────────────────────────  │
  │  Alert ID: #A1042                        │
  │  📡 GPS: 25.6102, 85.1412               │
  │  👥 3 reporting this                    │
  │  [📸 incident photo thumbnail]          │
  │  [🗺️ View on Map] ← flies map to pin   │
  └──────────────────────────────────────────┘
```

---

### 4. Multilingual Support

```
  [EN ←──── toggle ────▶ हिं]
       Arcade lever UI
  
  - All UI labels translated (data-i18n attributes)
  - Placeholder text translated
  - Voice button label switches language
  - Typing animation cycles language-appropriate phrases
  - Groq AI processes Hindi descriptions natively
  - Stored in localStorage (persists across sessions)
```

---

### 5. Admin Portal

```
  /admin (Spring Security protected — ROLE_ADMIN only)
  │
  ├── 📊 Analytics Dashboard
  │     - Chart.js donut: alerts by department
  │     - Chart.js bar: alerts by severity
  │     - KPI cards: total, pending, resolved, critical
  │
  ├── 📋 Alert Table
  │     - Search by description / location
  │     - Filter by department, severity, status
  │     - View photo evidence
  │     - Change status: PENDING → IN_PROGRESS → RESOLVED
  │     - Delete alert
  │
  └── 👥 User Management (/admin/users)
        - View registered citizens
        - OAuth2 profile sync details
```

---

### 6. Notification Flow (Twilio WhatsApp)

```
  Event: Alert submitted
    └──▶ WhatsApp: "Your alert #A1042 has been received. Status: PENDING"

  Event: Admin sets IN_PROGRESS
    └──▶ WhatsApp: "Update: Your alert is now IN PROGRESS 👷"

  Event: Admin sets RESOLVED
    └──▶ WhatsApp: "Your alert has been RESOLVED ✅ Thank you for reporting!"

  Sandbox mode: Citizens scan Twilio QR code to opt in (bottom-right FAB)
```

---

### 7. Auto-Cleanup Background Thread

```
  @Scheduled (every 1 hour)
       │
       ▼
  Query: find alerts WHERE status = RESOLVED
                      AND timestamp < (now - 24 hours)
       │
       ▼
  Delete stale resolved alerts
  (keeps database lean and map clutter-free)
```

---

### 8. OAuth2 Authentication

```
  /login
    │
    ├── Google OAuth2 ──▶ Spring Security ──▶ MongoDB (AppUser)
    └── Facebook OAuth2 ─▶ Spring Security ──▶ MongoDB (AppUser)

  Profile photo auto-fetched from OAuth provider
  Shown in navbar avatar
  Phone number collected post-login (/require-phone) if not set
```

---

### 9. Accessibility

```
  Accessibility bar (top of every page):
  - Font size: A  A+  A++
  - High contrast mode toggle
  - Light mode toggle
  - Screen reader friendly labels (aria-label on all interactive elements)
  - Keyboard navigation support
```

---

## v4.1 New Homepage Features

### Typing Animation
```
  "Stay Safe Together.|"  →  delete  →  "Report. Get Help.|"
                                    ▲
                            blinking cursor (CSS)
                            60ms per char type, 35ms per delete
                            2.2s pause at full phrase
                            switches phrases per current language
```

### How It Works Section
```
  ┌─────────────┐    ──────    ┌─────────────┐    ──────    ┌─────────────┐
  │     📍      │             │     🎙️      │             │     🚀      │
  │  Step 1     │             │  Step 2     │             │  Step 3     │
  │  Locate     │             │  Describe   │             │  Submit &   │
  │  Incident   │             │  or Speak   │             │  Track      │
  └─────────────┘             └─────────────┘             └─────────────┘
   hover: blue glow            hover: purple glow           hover: green glow
   desktop connector lines between cards (gradient)
```

### Toast Notification
```
  Form submit ──▶  ┌──────────────────────────────────────┐
                   │ ✅ Report submitted! Authorities have │  ← bottom-center
                   │    been notified.                     │     animated in
                   └──────────────────────────────────────┘     auto-dismiss 3.5s
```

### Scroll-to-Top Button
```
  scroll > 400px  ──▶  [⬆] appears bottom-left
  click           ──▶  smooth scroll to top
```

### Emergency Footer Bar
```
  👮 Police  🚒 Fire  🚑 Ambulance  ⚡ Electricity  🆘 Emergency
     100        101        108           1912              112
  Tap on mobile = direct call (tel: links)
  Hover = lift + glow animation
```

---

## Technical Stack Summary

| Component | Technology | Purpose |
|---|---|---|
| Backend framework | Spring Boot 3.5 | REST API + MVC |
| Language | Java 17 | Core logic |
| Security | Spring Security + OAuth2 | Auth, role-based access |
| Database | MongoDB Atlas | Alert + user storage |
| Template engine | Thymeleaf | Server-side HTML rendering |
| CSS framework | TailwindCSS | Glassmorphism UI |
| Maps | Leaflet.js + MarkerCluster | Interactive city map |
| Charts | Chart.js | Admin analytics |
| AI classification | Groq API (LLaMA) | Dept + severity detection |
| Voice transcription | Groq Whisper large-v3 | Speech-to-text |
| Notifications | Twilio API | WhatsApp alerts |
| Geocoding | OSM Nominatim | Address ↔ lat/lng |
| Containerization | Docker | Deployment ready |
