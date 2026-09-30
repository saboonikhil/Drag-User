# Drag User

Android **rider** client for the Drag ride-sharing stack.

Riders sign up / log in, book shared rides (pickup, drop, seats), confirm a cab, track trips, and pay with **Paytm** (merchant details come from the API). Talks to **[Drag-API](https://github.com/saboonikhil/Drag-API)** over HTTPS.

Companion operator app: **[Drag-Partner](https://github.com/saboonikhil/Drag-Partner)**.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-Android-orange.svg)](#stack)
[![AndroidX](https://img.shields.io/badge/UI-AndroidX-green.svg)](#stack)

## What you can do

| Area | Capabilities |
|---|---|
| **Auth** | Login and sign up |
| **Book** | Pickup / drop, date & time, seats; browse available rides |
| **Confirm** | Select cab / ride and confirm booking |
| **Trips** | Trip list and trip details |
| **Pay** | Paytm staging (debug) / production (release); checksum from Drag-API |
| **Account** | Profile, notifications, help / FAQ, terms |

## Stack

| | |
|---|---|
| Language | Java |
| UI | AndroidX (Material, RecyclerView, ConstraintLayout, CardView) |
| HTTP | Retrofit 2 + Gson |
| Payments | Paytm PG SDK (`pgplussdk`) |
| Extras | Facebook Shimmer |
| SDK | min 19 · compile/target 28 |
| App ID | `com.drag.user` |
| Version | 1.1.8 (`versionCode` 19) |

## Layout

```text
app/src/main/java/com/drag/user/
  SplashActivity · LoginActivity · SignUpActivity · MainActivity · PaymentActivity
  *Fragment.java     # book, select ride/cab, confirm, trips, account, …
  network/           # Retrofit + EndPointInterface
  model/             # User, Cab, Paytm, Notification, …
  adapter/           # list adapters
  data/              # local FAQ helper
  util/
```

## Setup

**Need:** Android Studio (JDK 8+), SDK 28 (or bump yourself), and a running [Drag-API](https://github.com/saboonikhil/Drag-API).

### 1. API base URL

In `app/build.gradle`:

```gradle
release {
    buildConfigField "String", "BASE_URL", "\"https://api.example.com\""
}
debug {
    buildConfigField "String", "BASE_URL", "\"https://localhost:8443\""
}
```

Point both at your API host before running.

### 2. Build

```bash
./gradlew assembleDebug
# or open in Android Studio and Run
```

```bash
./gradlew assembleRelease
```

Paytm uses staging in debug and production in release; MID / checksum / callback are returned by the backend (`generateChecksum`).

## Related

| Repo | Role |
|---|---|
| [Drag-API](https://github.com/saboonikhil/Drag-API) | Express + MongoDB API (auth, rides, payments, OTP) |
| [Drag-Partner](https://github.com/saboonikhil/Drag-Partner) | Android partner / admin app |
| **Drag-User** | This Android rider app |

## Status

Public portfolio project from the AndroidX / AGP 3.5 era. Fine for reading and forking; plan on newer AGP, dependencies, and target SDK before Play shipping.

## License

[Apache License 2.0](LICENSE)
