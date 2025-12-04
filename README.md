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

## Project Layers

### Models Layer

#### Raw DTOs
Represent JSON structure exactly; no runtime processing:

- `RawAssistantMessage`  
  - `text: String` → may contain `[AVATAR=N]` tags  
  - `highlightMap: Map<String, String>` → keywords → hex color codes  
  - `speed: Long` → ms per character  
  - `avatars: List<String>` → avatar resource names  

- `RawDialogueScript`  
  - `scriptId: String` → unique ID  
  - `startAvatarResName: String` → default avatar  
  - `messages: List<RawAssistantMessage>` → ordered dialogue messages  

#### Domain Models
Runtime-ready types for UI rendering:

- `AssistantMessage` → typed message with `avatarResIds: List<Int>` and `highlightMap: Map<String, Color>`  
- `AssistantState` → overlay visibility, current avatar, list of messages  
- `DialogueScript` → complete runtime script with default avatar  
- `DialogueSegment` *(sealed class)* → `Text(content, highlightMap)` or `AvatarChange(newAvatarResId)`  
- `DisplayChunk` → two-line block of text for pagination  

**Notes:**  
Segmentation allows precise typewriter control, avatar switching mid-message, and keyword highlighting. Domain models are fully Compose-ready.

---

### DialogueService (Service Layer)

Handles JSON loading, conversion to domain models, and Android resource mapping:

- Load JSON from `res/raw` using `scriptId`  
- Convert `RawAssistantMessage` → `AssistantMessage`  
  - Hex colors → `Color`  
  - Avatar names → drawable IDs  
- Map script to `DialogueScript`  
- Provides `Flow<DialogueScript>` for reactive consumption  

**Notes:**  
- Fully isolates Android-specific logic  
- Backward-compatible with scripts missing avatars or highlights  
- Type-safe mapping ensures UI never sees raw strings  

---

### DialogueRepository (Repository Layer)

Thin abstraction over `DialogueService`:

- Delegates `loadDialogue(context, scriptId)` to service  
- No extra business logic  
- Ensures **MVVM separation of concerns**  
- Can be extended for caching, multiple data sources, or testing  

---

### Preprocessing Utility

`preprocessMessage(message: AssistantMessage): List<DialogueSegment>`

- Detects `[AVATAR=N]` tags  
- Splits text into `Text` segments and inserts `AvatarChange` segments  
- Converts tag indices → valid avatar resource IDs  
- Ensures safe, at least one `Text` segment per message  

**Notes:**  
- Allows mid-message avatar switching  
- Preserves keyword highlights  
- Decouples parsing from UI  

---

### AssistantViewModel

Manages overlay state and dialogue playback:

- Maintains `AssistantState` (visibility, current avatar, messages)  
- Loads scripts via `DialogueRepository`  
- Processes `[AVATAR=N]` via `updateAvatar()`  
- Controls overlay via `closeAssistant()`  
- Exposes state via `StateFlow` for Compose UI  

---

###  AssistantViewModelFactory

- Instantiates `AssistantViewModel` with repository/service  
- Lightweight DI without external frameworks  
- Safe casting with `IllegalArgumentException` fallback  

---

## UI Components

### WaitingCursor
- Animated downward arrow signaling interaction  
- Bouncing animation with `rememberInfiniteTransition`  

### DialogueCrawler
- Typewriter animation with skip  
- Two-line chunk pagination  
- Per-word highlighting via `highlightMap`  
- Callback: `onTotalChunksCalculated` & `onChunkFinished`  

### AssistantOverlay
- Full-screen overlay composable  
- Handles message iteration, chunking, typewriter animation  
- Automatic avatar changes  
- Click handling to skip or advance text  
- Gradient background + breathing avatar + crawler + waiting cursor  

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
