#!/bin/bash

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

    echo "📤 Загрузка файла: $file_name"

    local response
    response=$(curl -s -w "\n%{http_code}" \
        -X POST \
        -H "Authorization: $TOKEN" \
        -F "file=@\"$file_path\"" \
        "$API_BASE/uploads")

    local http_code
    http_code=$(echo "$response" | tail -n1)
    local body
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" -ge 400 ]; then
        echo "❌ Ошибка загрузки файла (HTTP $http_code): $body" >&2
        exit 1
    fi

    # Извлекаем URL файла из ответа
    # Предполагаемый формат ответа: {"url": "https://...", "file_id": "..."}
    local file_url
    file_url=$(echo "$body" | grep -o '"url"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/"url"[[:space:]]*:[[:space:]]*"\([^"]*\)"/\1/')

    if [ -z "$file_url" ]; then
        # Пробуем получить file_id если url нет
        file_url=$(echo "$body" | grep -o '"file_id"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/"file_id"[[:space:]]*:[[:space:]]*"\([^"]*\)"/\1/')
    fi

    if [ -z "$file_url" ]; then
        echo "⚠️ Не удалось извлечь URL файла из ответа: $body" >&2
        exit 1
    fi

    echo "✅ Файл загружен: $file_url"
    echo "$file_url"
}

# Основной скрипт
echo "🚀 Отправка сообщения в чат MAX"
echo "   Чат: $CHAT_ID"
echo "   Сообщение: $MESSAGE"

# Массив для attachments
ATTACHMENTS="[]"

# Если есть файлы, загружаем их
if [ ${#FILES[@]} -gt 0 ]; then
    echo ""
    echo "📎 Файлов для отправки: ${#FILES[@]}"
    echo ""

    ATTACHMENTS="["
    first=true

    for file in "${FILES[@]}"; do
        file_url=$(upload_file "$file")

        if [ "$first" = true ]; then
            first=false
        else
            ATTACHMENTS+=","
        fi

        # Добавляем файл в attachments как document
        ATTACHMENTS+="{\"type\":\"document\",\"payload\":{\"url\":\"$file_url\"}}"
    done

    ATTACHMENTS+="]"
fi

# Формируем тело запроса
if [ "$ATTACHMENTS" == "[]" ]; then
    # Только текст
    REQUEST_BODY=$(cat <<EOF
{
  "text": "$MESSAGE"
}
EOF
)
else
    # Текст с вложениями
    REQUEST_BODY=$(cat <<EOF
{
  "text": "$MESSAGE",
  "attachments": $ATTACHMENTS
}
EOF
)
fi

echo ""
echo "📤 Отправка сообщения..."
response=$(send_request "POST" "$API_BASE/messages?chat_id=$CHAT_ID" "$REQUEST_BODY")

echo "✅ Сообщение успешно отправлено!"
echo ""
echo "Ответ сервера:"
echo "$response"
