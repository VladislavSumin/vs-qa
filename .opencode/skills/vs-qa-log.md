# vs-qa Log Analysis

Tool for querying device logs via CLI. Supports Embedded Anime and Logcat formats.

## Invocation

```bash
./gradlew :app:jvm:jvmRun --args "--mcp --log <path> --filter '<expression>' [--offset 0] [--limit 100]"
```

Or with the built JAR:

```bash
java -jar app/jvm/build/libs/vs-qa.jar --mcp --log <path> --filter '<expression>'
```

## Options

| Option | Default | Description |
|--------|---------|-------------|
| `--log <path>` | *required* | Path to log file |
| `--filter <expr>` | `""` | Filter expression in DSL (empty = all records) |
| `--offset <int>` | `0` | Pagination offset |
| `--limit <int>` | `100` | Max records to return |

## Filter DSL

Filter expressions combine field filters with boolean logic.

### Fields

| Field | Description | Example |
|-------|-------------|---------|
| `tag` | Log tag | `tag=MyTag` |
| `pid` | Process ID | `pid=1234` |
| `tid` | Thread ID | `tid=5678` |
| `thread` | Thread name | `thread=main` |
| `message` | Log message | `message="some text"` |
| `level` | Minimum log level | `level>=ERROR` |
| `runNumber` | Run/launch index (1-based) | `runNumber=3` |
| `timeAfter` | Time lower bound | `timeAfter="14:00:00"` |
| `timeBefore` | Time upper bound | `timeBefore="15:00:00"` |
| *(no field)* | Full line search | `crash` or `"exact phrase"` |

### Operators

| Operator | Meaning |
|----------|---------|
| `=` | Contains (case-insensitive) |
| `:=` | Exact match |
| `>=` | At least (used with `level`) |

### Logic

| Symbol | Meaning |
|--------|---------|
| `&` | AND |
| `\|` | OR |
| `!` or `-` | NOT |
| `()` | Grouping |

### Examples

```
# All ERROR and FATAL logs
level>=ERROR

# Errors from a specific tag
tag=Bluetooth & level>=ERROR

# Search for crashes in message
message=crashed

# Full line search
NullPointerException

# Errors in run #3
runNumber=3 & level>=ERROR

# Time range
timeAfter="14:00:00" & timeBefore="15:00:00"

# Complex: errors OR warnings from Bluetooth or WiFi
(tag=Bluetooth | tag=WiFi) & level>=WARN
```

## JSON Output

```json
{
  "total": 5230,
  "filtered": 47,
  "offset": 0,
  "limit": 100,
  "records": [
    {
      "order": 42,
      "raw": "2025-01-15T+03:00 14:32:10.123  1234: 5678 D/MyTag Some message",
      "time": "14:32:10.123",
      "level": "DEBUG",
      "pid": "1234",
      "tid": null,
      "tag": "MyTag",
      "message": "Some message"
    }
  ]
}
```

- `total` — total records in the log file (before filtering)
- `filtered` — records matching the filter (before offset/limit)
- If `filtered > limit`, use `--offset` to page through results

## Analysis Patterns

### Find all errors in a specific run

```
--filter "runNumber=3 & level>=ERROR"
```

### Find crashes

```
--filter "message=FATAL | message=crashed"
```

### Find errors related to a subsystem

```
--filter "tag=Bluetooth & level>=ERROR"
```

### Search for a specific error

```
--filter "NullPointerException"
```
