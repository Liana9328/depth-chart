# Depth Chart API Testing with curl

This guide covers all available APIs and all required use cases:

- `addPlayerToDepthChart`
- `removePlayerFromDepthChart`
- `getBackups`
- `getFullDepthChart`
- `bulk-add` bulk add players to depth chart

## 0) Start the app

```bash
./mvnw spring-boot:run
```

Expected:
- App starts successfully
- Server listening on `http://localhost:8080`

---

## 1) Useful shell variables

```bash
BASE="http://localhost:8080/api/v1/sports/NFL/teams/TB/depth-charts"
JSON='Content-Type: application/json'
```

---

## 2) Add player at specific depth

```bash
curl -i -X POST "$BASE/QB/players" -H "$JSON" \
  -d '{"player":{"number":12,"name":"Tom Brady"},"depth":0}'
```

Expected:
- Status: `201 Created`
- Empty response body

---

## 3) Add player without depth (append to end)

```bash
curl -i -X POST "$BASE/QB/players" -H "$JSON" \
  -d '{"player":{"number":11,"name":"Blaine Gabbert"}}'
```

Expected:
- Status: `201 Created`
- Player `11` is appended after existing QB entries

---

## 4) Add player with priority (insert and shift others down)

```bash
curl -i -X POST "$BASE/QB/players" -H "$JSON" \
  -d '{"player":{"number":2,"name":"Kyle Trask"},"depth":1}'
```

Expected:
- Status: `201 Created`
- QB order becomes: `12`, `2`, `11`

Verify:
```bash
curl -s "$BASE"
```

Expected JSON shape:
```json
{
  "QB": [
    {"number": 12, "name": "Tom Brady"},
    {"number": 2, "name": "Kyle Trask"},
    {"number": 11, "name": "Blaine Gabbert"}
  ]
}
```

---

## 5) Bulk add players (available API)

```bash
curl -i -X POST "$BASE/bulk-add" -H "$JSON" -d '[
  {"position":"LWR","player":{"number":13,"name":"Mike Evans"},"depth":0},
  {"position":"LWR","player":{"number":10,"name":"Scott Miller"},"depth":1},
  {"position":"RWR","player":{"number":14,"name":"Chris Godwin"},"depth":0}
]'
```

Expected:
- Status: `201 Created`
- Empty response body
- Players added in each position according to provided depth

---

## 6) Get backups for a player with backups

```bash
curl -s "$BASE/QB/players/12/backups"
```

Expected:
```json
[
  {"number": 2, "name": "Kyle Trask"},
  {"number": 11, "name": "Blaine Gabbert"}
]
```

---

## 7) Get backups when player has no backups

```bash
curl -s "$BASE/RWR/players/14/backups"
```

Expected:
```json
[]
```

---

## 8) Get backups when player is not listed at that position

```bash
curl -s "$BASE/QB/players/99/backups"
```

Expected:
```json
[]
```

---

## 9) Remove existing player from depth chart

```bash
curl -s -X DELETE "$BASE/QB/players/2"
```

Expected:
```json
[
  {"number": 2, "name": "Kyle Trask"}
]
```

And verify reindex/order:
```bash
curl -s "$BASE"
```

Expected QB section:
```json
"QB": [
  {"number": 12, "name": "Tom Brady"},
  {"number": 11, "name": "Blaine Gabbert"}
]
```

---

## 10) Remove player not listed at that position

```bash
curl -s -X DELETE "$BASE/QB/players/99"
```

Expected:
```json
[]
```

---

## 11) Get full depth chart (required use case)

```bash
curl -s "$BASE"
```

Expected:
- Status: `200 OK`
- JSON object grouped by position, each value ordered by depth

Example shape:
```json
{
  "LWR": [
    {"number": 13, "name": "Mike Evans"},
    {"number": 10, "name": "Scott Miller"}
  ],
  "QB": [
    {"number": 12, "name": "Tom Brady"},
    {"number": 11, "name": "Blaine Gabbert"}
  ],
  "RWR": [
    {"number": 14, "name": "Chris Godwin"}
  ]
}
```

---

## 12) Validation error example (bad request)

Missing `player`:
```bash
curl -i -X POST "$BASE/QB/players" -H "$JSON" -d '{"depth":0}'
```

Expected:
- Status: `400 Bad Request`
- Error payload from global exception handler

---

## 13) Optional reset between runs

Stop and restart app to reset in-memory H2 data.

