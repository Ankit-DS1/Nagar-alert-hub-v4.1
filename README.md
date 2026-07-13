# 🚨 Nagar Alert Hub — v4.1

> **Real-time civic engagement platform** that closes the loop between citizens and government departments — built for transparency, trust, and speed.

---

## 🎯 Problem Statement

Current municipal reporting systems feel like **black holes** — citizens file complaints and never hear back. This destroys civic trust.

**Nagar Alert Hub** rebuilds that trust through:
- ⚡ Instant AI-powered triage
- 🗺️ Live public map showing every active incident
- 📲 WhatsApp status updates at every step
- 🔍 End-to-end complaint tracking

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CITIZEN (Browser)                        │
│                                                                 │
│   [🎙️ Voice Input]  [📝 Text Form]  [📍 Map Pin]  [📷 Photo]   │
└────────────────────────────┬────────────────────────────────────┘
                             │  HTTP POST /report
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   SPRING BOOT BACKEND                           │
│                                                                 │
│  ┌──────────────┐   ┌───────────────┐   ┌──────────────────┐   │
│  │  Groq Whisper│   │  Groq AI NLP  │   │ Severity Detector│   │
│  │  (voice→text)│──▶│  (dept classify│──▶│ (Critical/High/  │   │
│  └──────────────┘   │   fallback:    │   │  Medium/Low)     │   │
│                     │   keyword+     │   └──────────────────┘   │
│                     │   bigram ML)   │                           │
│                     └───────────────┘                           │
│                             │                                   │
│                             ▼                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │               MONGODB (nagaralertdb)                     │   │
│  │   alerts collection  │  users collection                │   │
│  └──────────────────────────────────────────────────────────┘   │
│                             │                                   │
│            ┌────────────────┴────────────────┐                  │
│            ▼                                 ▼                  │
│  ┌──────────────────┐             ┌────────────────────┐        │
│  │  Twilio WhatsApp │             │  Leaflet.js Map    │        │
│  │  Notification    │             │  (live public feed)│        │
│  └──────────────────┘             └────────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                     ADMIN PORTAL                                │
│                                                                 │
│   [📊 Analytics Dashboard]  [✅ Update Status]  [🗑️ Delete]    │
│   [🔍 Search & Filter]      [📸 View Evidence]  [📋 Dept View] │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🖥️ Homepage — UI Sections Explained

### Section 1 — Navigation Bar

```
┌──────────────────────────────────────────────────────────────────┐
│ 📢 Nagar Alert Hub │ Home  Report  Live Map  Track  │  Login 🔐  │
│    City Safety     │                               │            │
└──────────────────────────────────────────────────────────────────┘
   ▲ Logo + tagline     ▲ Scroll-spy nav links           ▲ Auth CTA
   Sticky, glass-blur,  Active link underlines on scroll
   scrolled = dark bg
```

**Features:**
- Sticky glassmorphism navbar that darkens on scroll
- Scroll-spy highlights active section (Home / Report / Live Map)
- Hindi ⇄ English toggle (arcade lever UI)
- Mobile hamburger menu with animated X transition
- Red shimmer banner when critical alerts are active

---

### Section 2 — Hero

```
┌──────────────────────────────────────────────────────────────────┐
│                                                                  │
│  🟢 Live City Monitoring                                         │
│                                                                  │
│  Report. Track.                    ┌────────┐ ┌────────┐        │
│  Stay Safe Together.|              │ 🔴     │ │ 🟢     │        │
│   ▲ typing animation               │ Active │ │Resolved│        │
│     cycles phrases                 │  12    │ │  47    │        │
│                                    └────────┘ └────────┘        │
│  [✍️ Report Now]  [🗺️ View Live Map]  ┌────────┐               │
│                                        │ 📡     │               │
│                                        │ Status │               │
│                                        │All Clear│              │
│                                        └────────┘               │
└──────────────────────────────────────────────────────────────────┘
```

**Features:**
- Typing animation cycles through: *"Stay Safe Together."*, *"Report. Get Help."*, *"Your City. Your Voice."*
- Animated stat counters (count up on scroll-enter)
- Two CTAs: Report Now → scrolls to form, View Live Map → scrolls to map

---

### Section 3 — How It Works

```
┌───────────────┐  ────────  ┌───────────────┐  ────────  ┌───────────────┐
│               │            │               │            │               │
│      📍       │            │      🎙️       │            │      🚀       │
│               │            │               │            │               │
│  Step 1       │            │  Step 2       │            │  Step 3       │
│  Locate the   │            │  Describe     │            │  Submit &     │
│  Incident     │            │  or Speak     │            │  Track        │
│               │            │               │            │               │
│ GPS auto-     │            │ Type or use   │            │ Department    │
│ detect or     │            │ AI voice      │            │ notified      │
│ map pin       │            │ transcription │            │ automatically │
└───────────────┘            └───────────────┘            └───────────────┘
        ▲                            ▲                            ▲
   Hover: scale up             Hover: scale up             Hover: scale up
   Blue glow                   Purple glow                 Emerald glow

  ✓ AI classification   ✓ WhatsApp updates   ✓ Voice (Hindi+EN)   ✓ Live map
```

---

### Section 4 — Report Form (Right Column)

```
┌──────────────────────────────────┐
│ ✍️ Report Incident    Quick Form │
│──────────────────────────────────│
│                                  │
│  ┌────────────────────────────┐  │
│  │ 🎙️ Tap to speak your      │  │
│  │    emergency               │  │  ← Voice button
│  └────────────────────────────┘  │    States: idle → recording
│  [recording] 🔴 |||||||  0:23    │    (waveform + timer) →
│  [transcribing] ⏳ spinning      │    transcribing (spinner)
│                                  │
│  Description                     │
│  ┌────────────────────────────┐  │
│  │ Auto-filled from voice OR  │  │
│  │ type manually here...      │  │
│  └────────────────────────────┘  │
│                                  │
│  Location                        │
│  [ 📍 Detect My Location  ]      │  ← GPS auto-fill
│  [ Enter landmark...      ]      │
│  [ 🗺️ Pick on Map         ]      │  ← Opens map modal
│                                  │
│  Photo (optional)                │
│  ┌────────────────────────────┐  │
│  │   📷  Drag & drop or       │  │
│  │       click to browse      │  │
│  └────────────────────────────┘  │
│                                  │
│  [ Submit Report ✅ ]            │
│                                  │
│  Toast: ✅ Report submitted!      │  ← Bottom-center toast
└──────────────────────────────────┘
```

---

### Section 5 — Live Map & Alert Feed (Left Column)

```
┌──────────────────────────────────────────────────────────┐
│  Live City Map & Feed          🟢 Real-time updates      │
│──────────────────────────────────────────────────────────│
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │         📍 Interactive Map (Leaflet.js)            │  │
│  │                                                    │  │
│  │    👮        🚒③        ⚡                         │  │
│  │        📢                   🚑                     │  │
│  │                  🚦                                │  │
│  │  ③ = cluster bubble (3 alerts nearby)             │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  Status: [All ●] [Critical] [Pending] [Resolved]        │
│  Dept:   [All]  [👮 Police] [🚒 Fire] [🚑 Medical]      │
│          [⚡ Electrical] [🏗️ Municipal] [🚦 Traffic]     │
│                 ▲ Both filter rows work together         │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │▌ ⏳ PENDING  HIGH  📍 Gandhi Maidan               │  │  ← Alert card
│  │  Fire near the bakery on main road...              │  │    (collapsed)
│  │  🕒 13 Jul 2026  🏢 Fire  [Me Too 3]   👮  ⌄     │  │
│  └────────────────────────────────────────────────────┘  │
│  ┌────────────────────────────────────────────────────┐  │
│  │▌ ✅ RESOLVED  LOW  📍 Patna Junction              │  │  ← Expanded card
│  │  Pothole on the service road...                    │  │
│  │  🕒 12 Jul 2026  🏢 Municipal  [Me Too 1]  🏗️  ⌃  │  │
│  │  ────────────────────────────────────────────────  │  │
│  │  Alert ID: #A1023   📡 GPS: 25.6102, 85.1412      │  │
│  │  👥 1 reporting this                              │  │
│  │  [🗺️ View on Map] ← flies map to this location   │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

### Section 6 — Footer

```
┌──────────────────────────────────────────────────────────┐
│           Emergency Helplines                            │
│  👮 Police  🚒 Fire  🚑 Ambulance  ⚡ Electricity  🆘    │
│    100        101        108           1912           112 │
│  (tap to call directly on mobile)                        │
│──────────────────────────────────────────────────────────│
│  📢 Nagar Alert Hub    Quick Links     Departments       │
│  City Safety Platform  ✍️ Report       👮 Police         │
│  Real-time civic       🗺️ Live Map     🚒 Fire Service   │
│  incident reporting    🔍 Track Alert  🚑 Medical        │
│  for a safer city      🔐 Login        ⚡ Electrical     │
│                                        🏗️ Municipal      │
│                                        🚦 Traffic        │
│──────────────────────────────────────────────────────────│
│  © 2026 Nagar Alert Hub. Protecting the City.           │
│                              🟢 All systems operational  │
└──────────────────────────────────────────────────────────┘
```

---

## 🔄 Complete User Journey

```
CITIZEN                          SYSTEM                        ADMIN
   │                                │                             │
   │── Submit Report ──────────────▶│                             │
   │   (voice or text)              │── Groq Whisper ─────────▶  │
   │                                │   (voice → text)            │
   │                                │── Groq NLP ──────────────▶ │
   │                                │   (text → dept + severity)  │
   │                                │── Save to MongoDB ───────▶  │
   │                                │── Drop pin on Live Map ──▶  │
   │◀──────────── WhatsApp: "Received" ──────────────────────────│
   │                                │                             │
   │── Track on /my-alerts ────────▶│                             │
   │◀─────────── Status: PENDING ───│                             │
   │                                │                         Admin logs in
   │                                │◀────────── Views alert ─────│
   │                                │◀────────── Sets IN PROGRESS─│
   │◀──────────── WhatsApp: "In Progress" ───────────────────────│
   │                                │◀────────── Sets RESOLVED ───│
   │◀──────────── WhatsApp: "Resolved ✅" ───────────────────────│
   │                                │                             │
   │── Track on /my-alerts ────────▶│                             │
   │◀─────────── Status: RESOLVED ──│                             │
   │                           (Auto-cleanup after 24h)           │
```

---

## 💻 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.5, Spring Security, Spring Data |
| Database | MongoDB Atlas (cloud) |
| Frontend | Thymeleaf, TailwindCSS, Leaflet.js, Chart.js |
| AI / NLP | Groq API (LLaMA) — dept classification |
| Voice | Groq Whisper large-v3 — speech-to-text |
| Notifications | Twilio API — WhatsApp |
| Auth | OAuth2 (Google / Facebook) |
| Geocoding | OpenStreetMap Nominatim |

---

## 🆕 v4.1 Homepage Upgrades

| Feature | Description |
|---|---|
| ✍️ Typing animation | Hero tagline cycles phrases in Hindi + English |
| 📋 How It Works | 3-step onboarding section with hover glow cards |
| 🏢 Dept filter chips | Filter live feed by department (Police, Fire, etc.) |
| 🔽 Expandable cards | Click any alert card to reveal GPS, photo, ID, map link |
| 🔔 Toast notifications | Bottom-center animated toasts on form submit |
| ⬆️ Scroll-to-top | Floating button appears after scrolling 400px |
| ⚠️ Shimmer banner | Critical alert banner has animated light sweep |
| 🦶 Rich footer | Emergency helplines + quick links + dept grid |

---

## 🎥 Demo Flow

1. **Homepage** — Point out the live typing animation and stat counters.
2. **Voice Report** — Click 🎙️, speak: *"There is a fire near the bakery."* Watch waveform bars animate, timer tick, then description auto-fill.
3. **AI Magic** — Submit. Show the system auto-detected **FIRE** dept + severity without any dropdowns.
4. **Live Map** — The alert drops onto the map immediately with the 🚒 emoji pin.
5. **Department Filter** — Click "🚒 Fire" chip to filter feed to only fire alerts.
6. **Card Expand** — Click the alert card to expand: see GPS coords, Alert ID, and "View on Map" button.
7. **Track Alert** — Go to `/my-alerts`, show status is **PENDING**, WhatsApp notification sent.
8. **Admin Portal** — Log in as Fire admin, update to **Resolved**.
9. **Trust Loop** — Back on `/my-alerts` — status is now ✅ **RESOLVED**.

---

## ⚙️ Running Locally

```bash
# Prerequisites: Java 17+, MongoDB Atlas URI in application.properties

git clone https://github.com/your-repo/nagar-alert-hub.git
cd nagar-alert-hub
./mvnw spring-boot:run
```

App starts at **http://localhost:8080**

---

## 📸 Screenshots

| Page | Description |
|---|---|
| Homepage | Hero + How It Works + Live Map + Report Form |
| Admin Dashboard | Chart.js analytics, alert management table |
| Track Alerts | Citizen complaint tracking with status timeline |

> Replace `screenshots/` placeholders with actual screenshots before submission.
