# Шлюз расписания iubip.ru

Небольшое локальное Node.js-приложение (без зависимостей, только встроенные `http` и `fetch`,
Node.js 18+) для временного «пультика» выбора группы. Приложение ходить на iubip.ru через
него: так обходит CORS и не дублирует логику сайта в Android-коде.

## Запуск

```bash
node gateway/server.js
# Порт по умолчанию 3000, переопределяется: PORT=3001 node gateway/server.js
```

При старте шлюз делает «прогрев» `http://iubip.ru/schedule/?special_version=N` и получает
cookie `beget=begetok` (запасной вариант — отправить её сразу). Если первый ответ сайта
пустой или не JSON, запрос повторяется с cookie. Все действия логируются в stdout.

## Endpoints

| Метод | Путь | Что делает |
|---|---|---|
| GET | `/api/groups` | Список групп с сайта: `{ "academies": [ { "name": "Академия …", "groups": ["ПД101", …] } ] }` |
| GET | `/api/schedule?group=<название>` | Ответ сайта `do=schedule&group=…` **как есть**, без разбора (структура `{ "ГРУППА": [период, {неделя: […]}] }`) |
| GET | `/health` | `{ "status": "ok" }` |
| OPTIONS | любой | CORS preflight (`Access-Control-Allow-Origin: *`) |

## Примеры curl

```bash
# Список академий и групп
curl -s localhost:3000/api/groups | python3 -m json.tool | head -30

# Расписание группы (название кодируется автоматически)
curl -sG --data-urlencode 'group=ПД201' localhost:3000/api/schedule | python3 -m json.tool | head -30
```

## Технические детали

- Upstream: `POST http://iubip.ru/local/templates/univer/include/schedule/ajax/read-file-groups.php`
  с телом `do=groups` или `do=schedule&group=<urlencoded>`, заголовками
  `X-Requested-With: XMLHttpRequest` и `Cookie: beget=begetok`.
- Таймаут одного запроса — 15 с (через `AbortController`). Ошибки — HTTP 502 с JSON
  `{ "error": "..." }`, понятный лог в stdout.
- Зависимостей нет: `npm install` не требуется, `node_modules` не создаётся.
- Шлюз временный: не сохраняет выбор группы и не хранит никаких данных LMS.
