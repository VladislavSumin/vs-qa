#!/bin/bash
set -euo pipefail

if [ $# -ne 1 ]; then
    echo "Usage: $0 <url>"
    echo "Example: $0 https://fonts.gstatic.com/render/v1/Material+Symbols+Outlined/24dp/content_copy.kt?var=..."
    exit 1
fi

URL="$1"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ICONS_BASE="$SCRIPT_DIR/core/ui/icons/src/commonMain/kotlin/ru/vladislavsumin/core/ui/icons"
ICONS_DIR="$ICONS_BASE/icons"
QAICONS_FILE="$ICONS_BASE/QaIcons.kt"

ICON_NAME=$(echo "$URL" | sed -n 's|.*/24dp/\([^/?]*\)\.kt.*|\1|p')

if [ -z "$ICON_NAME" ]; then
    echo "ERROR: Could not extract icon name from URL"
    echo "URL must contain /24dp/<icon_name>.kt"
    exit 1
fi

PASCAL_NAME=$(echo "$ICON_NAME" | perl -pe 's/(^|_)([a-z])/\U$2/g')

echo "Icon name  : $ICON_NAME"
echo "PascalCase  : $PASCAL_NAME"
echo "Destination: $ICONS_DIR/${PASCAL_NAME}.kt"

mkdir -p "$ICONS_DIR"

TMP_FILE=$(mktemp)
trap 'rm -f "$TMP_FILE"' EXIT

echo "Downloading..."
curl -fsSL --compressed "$URL" -o "$TMP_FILE" || {
    echo "ERROR: Failed to download from $URL"
    exit 1
}

echo "Transforming..."
LC_ALL=C sed -i '' \
    -e 's/^package .*/package ru.vladislavsumin.core.ui.icons.icons/' \
    -e '/^@Suppress("CheckReturnValue")/d' \
    -e 's/^public val /internal val /' \
    -e '/^private var _/i\
@Suppress("BackingPropertyNaming")' \
    "$TMP_FILE"

DEST="$ICONS_DIR/${PASCAL_NAME}.kt"
if [ -f "$DEST" ]; then
    echo "WARNING: $DEST already exists, overwriting..."
fi
cp "$TMP_FILE" "$DEST"
echo "Saved: $DEST"

if grep -q "import ru.vladislavsumin.core.ui.icons.icons.${ICON_NAME}" "$QAICONS_FILE"; then
    echo "Import already exists in QaIcons.kt, skipping."
else
    echo "Updating QaIcons.kt..."
    python3 -c "
lines = open('$QAICONS_FILE').read().split('\n')

last_import_idx = max(i for i, l in enumerate(lines) if l.startswith('import '))
lines.insert(last_import_idx + 1, 'import ru.vladislavsumin.core.ui.icons.icons.$ICON_NAME')

last_brace_idx = max(i for i, l in enumerate(lines) if l.strip() == '}')
lines.insert(last_brace_idx, '    val $PASCAL_NAME = $ICON_NAME')

open('$QAICONS_FILE', 'w').write('\n'.join(lines))
"
    echo "Updated: $QAICONS_FILE"
fi

echo ""
echo "Done! New icon added: $PASCAL_NAME"
echo "  File    : $DEST"
echo "  Registry: $QAICONS_FILE"
