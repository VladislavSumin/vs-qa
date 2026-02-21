#!/usr/bin/env bash
set -euo pipefail

readonly BASE_URL="https://platform-api.max.ru"
readonly SCRIPT_NAME="${0##*/}"

usage() {
  echo "Usage: $SCRIPT_NAME <chat_id> \"<message_text>\" [file1 file2 ...]" >&2
  echo "" >&2
  echo "  chat_id       Chat ID" >&2
  echo "  message_text  Message body; use \"markdown\" formatting" >&2
  echo "  file1, file2  Optional paths to files to attach" >&2
  echo "" >&2
  echo "Requires MAX_BOT_TOKEN in environment (bot token from MAX Integration)." >&2
  echo "  export MAX_BOT_TOKEN=your_bot_token" >&2
}

# --- 1. Dependencies ---
for cmd in curl jq; do
  if ! command -v "$cmd" &>/dev/null; then
    echo "Error: '$cmd' is required but not installed." >&2
    exit 1
  fi
done

# --- 2. Help / minimal args ---
if [[ $# -eq 0 ]]; then
  usage
  exit 1
fi
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
  usage
  exit 0
fi

if [[ $# -lt 2 ]]; then
  echo "Error: at least <chat_id> and <message_text> are required." >&2
  usage
  exit 1
fi

# --- 3. Token ---
if [[ -z "${MAX_BOT_TOKEN:-}" ]]; then
  echo "Error: MAX_BOT_TOKEN is not set." >&2
  echo "Set it with: export MAX_BOT_TOKEN=your_bot_token" >&2
  exit 1
fi

# --- 4. Parse args ---
CHAT_ID="$1"
TEXT="$2"
# Optional files (args 3+); empty when no files given (safe with set -u).
FILES=("${@:3}")

# --- 5. Validate files ---
for f in "${FILES[@]+"${FILES[@]}"}"; do
  if [[ ! -f "$f" ]]; then
    echo "Error: file not found or not a file: $f" >&2
    exit 1
  fi
  if [[ ! -r "$f" ]]; then
    echo "Error: file not readable: $f" >&2
    exit 1
  fi
done

# --- 6. Upload files and collect tokens ---
# MAX: POST /uploads?type=file → url; then POST file to url → response has token (for type=file/image).
TOKENS=()
for f in "${FILES[@]+"${FILES[@]}"}"; do
  upload_resp="$(curl -s -S -X POST "${BASE_URL}/uploads?type=file" \
    -H "Authorization: ${MAX_BOT_TOKEN}" \
    -H "Content-Type: application/json")"
  if ! upload_url="$(echo "$upload_resp" | jq -r '.url // empty')"; then
    echo "Error: failed to parse upload URL response for $f" >&2
    echo "$upload_resp" | jq . 2>/dev/null || echo "$upload_resp" >&2
    exit 1
  fi
  if [[ -z "$upload_url" || "$upload_url" == "null" ]]; then
    echo "Error: no upload URL in response for $f" >&2
    echo "$upload_resp" | jq . 2>/dev/null || echo "$upload_resp" >&2
    exit 1
  fi
  file_resp="$(curl -s -S -X POST "$upload_url" \
    -H "Authorization: ${MAX_BOT_TOKEN}" \
    -F "data=@${f}")"
  token="$(echo "$file_resp" | jq -r '.token // empty')"
  if [[ -z "$token" || "$token" == "null" ]]; then
    echo "Error: no token in upload response for $f" >&2
    echo "$file_resp" | jq . 2>/dev/null || echo "$file_resp" >&2
    exit 1
  fi
  TOKENS+=("$token")
  # Short pause to reduce attachment.not.ready (per MAX docs)
  sleep 1
done

# --- 7 & 8. Build body and send: MAX allows only one file attachment per message ---
# So we send one message per file (first message includes text; rest are file-only), or one message with just text if no files.
send_one() {
  local body="$1"
  local resp http_code body_only
  resp="$(curl -s -w "\n%{http_code}" -X POST \
    "${BASE_URL}/messages?chat_id=${CHAT_ID}" \
    -H "Authorization: ${MAX_BOT_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "$body")"
  http_code="${resp##*$'\n'}"
  body_only="${resp%$'\n'*}"
  # #region agent log
  _ts="$(date +%s)000"
  echo "$(jq -n --arg loc "send-max-message.sh:after_send" --arg msg "API response" --arg code "$http_code" --arg ts "$_ts" '{location:$loc,message:$msg,data:{http_code:$code},hypothesisId:"H1",runId:"post-fix",timestamp:($ts|tonumber)}')" >> /Users/vs/Documents/projects/vs-qa/.cursor/debug.log
  # #endregion
  if [[ "$http_code" != "200" ]]; then
    echo "Error: API returned HTTP $http_code" >&2
    echo "$body_only" | jq . 2>/dev/null || echo "$body_only" >&2
    return 1
  fi
  echo "$body_only"
  return 0
}

if [[ ${#TOKENS[@]} -eq 0 ]]; then
  # Zero files: one message with text only.
  body="$(jq -n --arg text "$TEXT" '{ text: $text, format: "markdown" }')"
  body_only="$(send_one "$body")" || exit 1
elif [[ ${#TOKENS[@]} -eq 1 ]]; then
  # One file: one message with text + file.
  body="$(jq -n --arg text "$TEXT" --arg t "${TOKENS[0]}" '{ text: $text, format: "markdown", attachments: [{ type: "file", payload: { token: $t } }] }')"
  body_only="$(send_one "$body")" || exit 1
else
  # More than one file: one message (text only), then N messages each with one file (API allows only one file per message).
  body="$(jq -n --arg text "$TEXT" '{ text: $text, format: "markdown" }')"
  body_only="$(send_one "$body")" || exit 1
  for token in "${TOKENS[@]}"; do
    body="$(jq -n --arg t "$token" '{ attachments: [{ type: "file", payload: { token: $t } }] }')"
    body_only="$(send_one "$body")" || exit 1
  done
fi

echo "Message sent."
echo "$body_only" | jq '.' 2>/dev/null || echo "$body_only"
