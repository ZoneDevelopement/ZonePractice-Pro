# Java Integration Spec: PvP Inference API

## Endpoint and Tick Timing

The inference server exposes:

- `POST http://127.0.0.1:8000/predict`

Your Java bot integration should send **one request every 50 ms** (one Minecraft tick) per bot.

The Python server performs preprocessing internally (relative coordinates, Euclidean distance,
yaw wrapping for frame-to-frame deltas, and vocabulary mapping), so Java should send raw Bukkit state.

## Request Schema (`GameState`)

Send JSON with the exact fields below.

```json
{
  "type": "object",
  "required": [
    "bot_id",
    "bot",
    "target",
    "inventory"
  ],
  "properties": {
    "bot_id": { "type": "string" },

    "bot": {
      "type": "object",
      "required": [
        "x",
        "y",
        "z",
        "yaw",
        "pitch",
        "vel_x",
        "vel_y",
        "vel_z",
        "health",
        "food",
        "is_on_ground"
      ],
      "properties": {
        "x": { "type": "number" },
        "y": { "type": "number" },
        "z": { "type": "number" },
        "yaw": { "type": "number" },
        "pitch": { "type": "number" },
        "vel_x": { "type": "number" },
        "vel_y": { "type": "number" },
        "vel_z": { "type": "number" },
        "health": { "type": "number" },
        "food": { "type": "number" },
        "is_on_ground": { "type": "boolean" }
      },
      "additionalProperties": false
    },

    "target": {
      "type": "object",
      "required": [
        "x",
        "y",
        "z",
        "yaw",
        "pitch",
        "vel_x",
        "vel_y",
        "vel_z",
        "health"
      ],
      "properties": {
        "x": { "type": "number" },
        "y": { "type": "number" },
        "z": { "type": "number" },
        "yaw": { "type": "number" },
        "pitch": { "type": "number" },
        "vel_x": { "type": "number" },
        "vel_y": { "type": "number" },
        "vel_z": { "type": "number" },
        "health": { "type": "number" },
        "food": { "type": "number" },
        "is_on_ground": { "type": "boolean" }
      },
      "additionalProperties": false
    },

    "inventory": {
      "type": "object",
      "required": ["main_hand", "off_hand", "hotbar"],
      "properties": {
        "main_hand": { "type": "string" },
        "off_hand": { "type": "string" },
        "hotbar": {
          "type": "array",
          "items": { "type": "string" },
          "minItems": 9,
          "maxItems": 9
        }
      },
      "additionalProperties": false
    }
  },
  "additionalProperties": false
}
```

## Response Schema (`BotPrediction`)

The API returns JSON with the exact fields below.

```json
{
  "type": "object",
  "required": [
    "deltaYaw",
    "deltaPitch",
    "inputForward",
    "inputBackward",
    "inputLeft",
    "inputRight",
    "inputJump",
    "inputSneak",
    "inputSprint",
    "inputLmb",
    "inputRmb",
    "inputSlot"
  ],
  "properties": {
    "deltaYaw": { "type": "number" },
    "deltaPitch": { "type": "number" },
    "inputForward": { "type": "number" },
    "inputBackward": { "type": "number" },
    "inputLeft": { "type": "number" },
    "inputRight": { "type": "number" },
    "inputJump": { "type": "number" },
    "inputSneak": { "type": "number" },
    "inputSprint": { "type": "number" },
    "inputLmb": { "type": "number" },
    "inputRmb": { "type": "number" },
    "inputSlot": { "type": "integer", "minimum": 0, "maximum": 8 }
  },
  "additionalProperties": false
}
```

## Mandatory Java Concurrency Requirement

Use `java.net.http.HttpClient` (Java 11+) **asynchronously**.

- Do **not** block the Minecraft main server thread with a synchronous HTTP call.
- Use `sendAsync(...)` and `thenAccept(...)`.
- In `thenAccept(...)`, schedule application of the returned prediction onto the Citizens NPC on the **next available tick** via Bukkit scheduler (for example, `Bukkit.getScheduler().runTask(...)`).

This is required to avoid tick lag and server thread stalls.

