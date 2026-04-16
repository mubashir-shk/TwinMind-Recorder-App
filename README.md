# TwinMind Android App

A voice recording app with transcription and summary generation built with Kotlin, Jetpack Compose, MVVM, and Hilt.

## Features Implemented

### ✅ Core Features
- **Dashboard** - Lists all recorded sessions with status indicators
- **Foreground Recording Service** - Records audio in background with persistent notification and Stop button
- **30-second Audio Chunking** - Automatically splits recording into chunks
- **Room Database** - All sessions and chunks persisted locally (single source of truth)
- **Background Transcription Worker** - Mock transcription with retry logic (up to 3 attempts)
- **Background Summary Worker** - Mock summary generation that survives app kill
- **Summary Screen** - Shows Title, Summary, Action Items, Key Points with loading/error states
- **Retry Button** - Retry summary generation on failure
- **Clean MVVM + Hilt Architecture**

### Tech Stack
- Kotlin + Jetpack Compose
- MVVM: ViewModel → Repository → DAO
- Hilt (Dependency Injection)
- Room (Local Database)
- WorkManager (Background jobs)
- Coroutines + Flow
- Retrofit (configured, mock used)

## Edge Cases NOT Implemented (would add with more time)

| Edge Case | Notes |
|-----------|-------|
| Phone call interruption | Would use `PhoneStateListener` / `TelephonyCallback` to pause on CALL_STATE_OFFHOOK |
| Audio focus loss | Would use `AudioManager.OnAudioFocusChangeListener` to pause/resume |
| Bluetooth headset switching | Would use `BroadcastReceiver` for `ACTION_ACL_CONNECTED` / `ACTION_HEADSET_PLUG` |
| Silent audio detection | Would analyze MediaRecorder `getMaxAmplitude()` every 10s |
| Low storage check | Would check `StatFs` before starting and during recording |
| 2-second chunk overlap | Would save last 2s of each chunk and prepend to next |
| Android 16 Lock Screen Live Updates | Would use `LiveActivityManager` / `OngoingActivity` API |
| Real Whisper/Gemini API | Replace mock in `TranscriptionWorker` with actual API call |

## Setup

1. Clone the repo
2. Open in Android Studio Hedgehog or newer
3. Run on device/emulator with API 24+
4. Grant microphone and notification permissions when prompted

## Building APK

```bash
./gradlew assembleDebug
```

APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Architecture Diagram

```
MainActivity
    └── NavHost
        ├── DashboardScreen ← DashboardViewModel ← RecordingRepository
        ├── RecordingScreen ← RecordingViewModel ← RecordingRepository
        └── SummaryScreen   ← SummaryViewModel   ← RecordingRepository
                                                        ├── SessionDao (Room)
                                                        └── AudioChunkDao (Room)

RecordingService (Foreground)
    └── Saves chunks → enqueues TranscriptionWorker

TranscriptionWorker (WorkManager)
    └── On all chunks done → enqueues SummaryWorker

SummaryWorker (WorkManager)
    └── Updates session with summary in Room
```
