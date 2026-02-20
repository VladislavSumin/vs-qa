#!/bin/bash

# Вайбкод помойка, мне стыдно честно, но этот ваш qwen бесполезный кусок говна
# Скрипт для отправки сообщения и файлов в чат MAX
# Использование: MAX_BOT_TOKEN=<token> ./send-max-message.sh <chat_id> <message> [file1 file2 ...]

set -e

# Проверка токена
if [ -z "$MAX_BOT_TOKEN" ]; then
    echo "❌ Ошибка: переменная окружения MAX_BOT_TOKEN не установлена"
    echo ""
    echo "Использование:"
    echo "  MAX_BOT_TOKEN=<token> $0 <chat_id> <message> [file1 file2 ...]"
    echo ""
    echo "Или экспортируйте переменную:"
    echo "  export MAX_BOT_TOKEN=<token>"
    echo "  $0 <chat_id> <message> [file1 file2 ...]"
    echo ""
    echo "Аргументы:"
    echo "  chat_id   - ID чата для отправки"
    echo "  message   - текст сообщения"
    echo "  files     - опционально: список файлов для отправки"
    echo ""
    echo "Пример:"
    echo "  MAX_BOT_TOKEN='your-token' $0 '12345' 'Привет!' file1.pdf file2.jpg"
    exit 1
fi

# Проверка количества аргументов
if [ "$#" -lt 2 ]; then
    echo "Использование: MAX_BOT_TOKEN=<token> $0 <chat_id> <message> [file1 file2 ...]"
    echo ""
    echo "Аргументы:"
    echo "  chat_id   - ID чата для отправки"
    echo "  message   - текст сообщения"
    echo "  files     - опционально: список файлов для отправки"
    exit 1
fi

TOKEN="$MAX_BOT_TOKEN"
CHAT_ID="$1"
MESSAGE="$2"
shift 2
FILES=("$@")

API_BASE="https://platform-api.max.ru"

# Функция для отправки запроса с проверкой ответа
send_request() {
    local method="$1"
    local url="$2"
    local data="$3"
    local response
    local http_code

    if [ "$method" == "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" \
            -X POST \
            -H "Authorization: $TOKEN" \
            -H "Content-Type: application/json" \
            -d "$data" \
            "$url")
    else
        response=$(curl -s -w "\n%{http_code}" \
            -X GET \
            -H "Authorization: $TOKEN" \
            "$url")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" -ge 400 ]; then
        echo "❌ Ошибка API (HTTP $http_code): $body" >&2
        exit 1
    fi

    echo "$body"
}

# Функция для загрузки файла
upload_file() {
    local file_path="$1"
    local file_name
    file_name=$(basename "$file_path")

    if [ ! -f "$file_path" ]; then
        echo "❌ Файл не найден: $file_path" >&2
        exit 1
    fi

    # Определяем тип файла для API
    local file_type="file"
    local extension="${file_name##*.}"
    extension=$(echo "$extension" | tr '[:upper:]' '[:lower:]')
    
    case "$extension" in
        jpg|jpeg|png|gif|tiff|bmp|heic)
            file_type="image"
            ;;
        mp4|mov|mkv|webm|matroska)
            file_type="video"
            ;;
        mp3|wav|m4a)
            file_type="audio"
            ;;
    esac

    echo "📤 Загрузка файла: $file_name (тип: $file_type)" >&2

    # Шаг 1: Получаем URL для загрузки
    local upload_response
    upload_response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Authorization: $TOKEN" \
        "$API_BASE/uploads?type=$file_type")

    local upload_http_code
    upload_http_code=$(echo "$upload_response" | tail -n1)
    local upload_body
    upload_body=$(echo "$upload_response" | sed '$d')

    if [ "$upload_http_code" -ge 400 ]; then
        echo "❌ Ошибка получения URL загрузки (HTTP $upload_http_code): $upload_body" >&2
        exit 1
    fi

    # Извлекаем URL и token из ответа
    local upload_url
    upload_url=$(echo "$upload_body" | grep -o '"url"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed 's/"url"[[:space:]]*:[[:space:]]*"\([^"]*\)"/\1/')

    local file_token
    file_token=$(echo "$upload_body" | grep -o '"token"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed 's/"token"[[:space:]]*:[[:space:]]*"\([^"]*\)"/\1/')

    if [ -z "$upload_url" ]; then
        echo "⚠️ Не удалось извлечь URL из ответа: $upload_body" >&2
        exit 1
    fi

    # Шаг 2: Загружаем файл по полученному URL
    local file_response
    file_response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Authorization: $TOKEN" \
        -F "data=@\"$file_path\"" \
        "$upload_url")

    local file_http_code
    file_http_code=$(echo "$file_response" | tail -n1)
    local file_body
    file_body=$(echo "$file_response" | sed '$d')

    if [ "$file_http_code" -ge 400 ]; then
        echo "❌ Ошибка загрузки файла (HTTP $file_http_code): $file_body" >&2
        exit 1
    fi

    # Извлекаем token из ответа после загрузки (если не получили на шаге 1)
    if [ -z "$file_token" ]; then
        file_token=$(echo "$file_body" | grep -o '"token"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed 's/"token"[[:space:]]*:[[:space:]]*"\([^"]*\)"/\1/')
    fi

    if [ -z "$file_token" ]; then
        echo "⚠️ Не удалось извлечь token из ответа: $file_body" >&2
        exit 1
    fi

    echo "✅ Файл загружен, $file_name" >&2
    echo "$file_token"
}

# Основной скрипт
echo "🚀 Отправка сообщения в чат MAX"
echo "   Чат: $CHAT_ID"
echo "   Сообщение: $MESSAGE"

# Массив для хранения токенов загруженных файлов
declare -a FILE_TOKENS

# Если есть файлы, сначала загружаем их все
if [ ${#FILES[@]} -gt 0 ]; then
    echo ""
    echo "📎 Файлов для отправки: ${#FILES[@]}"
    echo ""

    for file in "${FILES[@]}"; do
        token=$(upload_file "$file")
        FILE_TOKENS+=("$token")
    done
fi

# Отправляем текстовое сообщение
REQUEST_BODY=$(cat <<EOF
{
  "text": "$MESSAGE"
}
EOF
)

# Костыль что бы файлы обработались.
sleep 20

echo ""
echo "📤 Отправка сообщения..."
response=$(send_request "POST" "$API_BASE/messages?chat_id=$CHAT_ID" "$REQUEST_BODY")

echo "✅ Сообщение успешно отправлено!"
echo ""
echo "Ответ сервера:"
echo "$response"

# Если есть файлы, отправляем каждый отдельным сообщением
if [ ${#FILE_TOKENS[@]} -gt 0 ]; then
    echo ""
    echo "📤 Отправка файлов отдельными сообщениями..."
    
    for token in "${FILE_TOKENS[@]}"; do
        file_request=$(cat <<EOF
{
  "attachments": [
    {
      "type": "file",
      "payload": {
        "token": "$token"
      }
    }
  ]
}
EOF
)
        echo "   Отправка файла с token: $token"
        response=$(send_request "POST" "$API_BASE/messages?chat_id=$CHAT_ID" "$file_request")
        echo "   ✅ Файл отправлен"
    done
fi
