# Narratix Assistant


![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)


A **modular, cinematic dialogue assistant** for Android, designed to overlay scripted dialogues with **typewriter-style animation, dynamic avatars, and keyword highlighting**. Built with **Jetpack Compose**, **MVVM**, and Kotlin.

---

## Features

- Typewriter-style text animation with skip and per-word highlighting  
- Dynamic avatars via `[AVATAR=N]` meta-tags  
- Two-line chunk pagination for consistent UI  
- Full-screen overlay with gradient background and breathing avatars  
- MVVM-friendly architecture (ViewModel → Repository → Service → UI)  
- JSON-based dialogue scripts for easy extension  
- Multiple demo screens supported  

---

## Design Decisions

- **Raw vs Domain models**: Decouples JSON parsing from UI rendering
- **Segmentation**: Enables precise typewriter control and mid-message avatar changes
- **Repository pattern**: Prepares for future data sources (API, cache, etc.)

---


--

| Cinematique Demo 1 | Sensor Parallax Demo 2 | 
|:---:|:---:|
| ![P1](screenshots/demo1.gif) | ![P2](screenshots/demo2.gif) |

---

## Architecture Overview

The system processes dialogue in a clear pipeline:

1. JSON Script (raw data)  
2. Android resource mapping  
3. Domain model construction  
4. Message segmentation  
5. ViewModel state management  
6. Compose UI rendering  

Each stage has a single responsibility to maintain testability and modularity.

---

## Models

### Raw Models (JSON)

These types reflect the JSON structure without any transformation:

- `RawAssistantMessage`
  - `text` (may contain `[AVATAR=N]`)
  - `highlightMap` (keyword → hex color)
  - `speed`
  - `avatars` (drawable resource names)

- `RawDialogueScript`
  - `scriptId`
  - `startAvatarResName`
  - `messages`

### Domain Models (UI-Ready)

Converted and typed for runtime usage:

- `AssistantMessage` (typed colors, resolved drawable IDs)
- `DialogueScript`
- `AssistantState`
- `DialogueSegment` (either `Text` or `AvatarChange`)
- `DisplayChunk` (two-line pagination unit)

Segmentation enables mid-message avatar changes, accurate cursor control, and precise highlight management.

---

## DialogueService

Responsible for all Android-specific operations:

- Loads JSON from `res/raw`
- Maps hex colors to `Color`
- Resolves avatar names to drawable resource IDs
- Constructs `DialogueScript`
- Exposes data through `Flow<DialogueScript>`

This layer fully isolates platform-specific logic and ensures the UI never processes raw strings or resource names.

---

## DialogueRepository

A thin abstraction on top of `DialogueService`:

- Delegates script loading
- Preserves MVVM separation of concerns
- Can be extended for caching or alternative data sources

---

## Preprocessing and Segmentation

`preprocessMessage(message: AssistantMessage)` returns a list of `DialogueSegment`:

- Detects `[AVATAR=N]` tags
- Splits text into `Text` segments
- Injects `AvatarChange` segments
- Retains highlight rules
- Guarantees at least one `Text` segment per message

This stage ensures the typewriter engine can react to mid-message avatar switches and highlight rendering.

---

## AssistantViewModel

Central state and flow manager:

- Holds `AssistantState` (visibility, avatar, current message)
- Loads scripts through the repository
- Applies segmentation results
- Controls overlay lifecycle
- Exposes state via `StateFlow` for Compose UI

---

## AssistantViewModelFactory

Simple, explicit DI helper:

- Instantiates the ViewModel with its repository and service dependencies
- Provides safe casting with `IllegalArgumentException` fallback

---

## UI Components

### WaitingCursor
- Small animated indicator suggesting progression or interaction

### DialogueCrawler
- Typewriter engine
- Skip support
- Two-line pagination
- Keyword highlighting
- Callbacks for chunk computation and completion events

### AssistantOverlay
- Full-screen composable managing the entire narrative flow
- Handles chunk progression and typewriter logic
- Applies gradient background and animated avatar presentation
- Click-to-skip or advance behavior

---
## Dialogue Script JSON Structure

```json
{
  "scriptId": "STRING_UNIQUE_ID",
  "startAvatarResName": "STRING_RESOURCE_NAME",
  "messages": [
    {
      "text": "TEXT_WITH_[AVATAR=N]_TAGS",
      "highlightMap": {
        "KEYWORD_1": "#HEXCOLOR",
        "KEYWORD_2": "#HEXCOLOR"
      },
      "speed": LONG_MS_PER_CHARACTER,
      "avatars": [
        "AVATAR_RESOURCE_NAME_0",
        "AVATAR_RESOURCE_NAME_1",
        "AVATAR_RESOURCE_NAME_2"
      ]
    }
  ]
}
```

- **scriptId** → unique identifier
- **startAvatarResName** → default avatar at start
- **text** → message content with optional `[AVATAR=N]` tags
- **highlightMap** → keyword → color
- **speed** → ms per character
- **avatars** → drawable names for `[AVATAR=N]`

---
