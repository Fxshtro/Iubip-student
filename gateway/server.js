'use strict';

/**
 * Локальный шлюз расписания iubip.ru для «пультика» выбора группы.
 *
 * Без зависимостей: только встроенные `http` и `fetch` (Node.js 18+).
 * Ничего не парсит в расписании — структура ответа сайта разбирается в приложении
 * (JsonScheduleParser.parseAll). На сервере только валидация «это JSON или нет»
 * и превращение списка групп в Convenient-контракт для пульта.
 */

const http = require('node:http');

const UPSTREAM =
  'http://iubip.ru/local/templates/univer/include/schedule/ajax/read-file-groups.php';
const WARMUP_PAGE = 'http://iubip.ru/schedule/?special_version=N';
const FALLBACK_COOKIE = 'beget=begetok';
const TIMEOUT_MS = 15_000;
const PORT = Number.parseInt(process.env.PORT || '3000', 10);

/** Простой логгер с временной меткой — всё в stdout. */
function log(level, message) {
  console.log(`[${new Date().toISOString()}] [${level}] ${message}`);
}

/**
 * GET-запрос «прогрева»: сервер отдаёт 274-байтовый JS, который ставит beget-куку.
 * Забираем Set-Cookie (или, если его нет, используем проверенный beget=begetok).
 * Ошибка прогрева НЕ фатальна: основные POST-запросы всё равно пробуют cookie.
 */
async function obtainCookie() {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  try {
    const response = await fetch(WARMUP_PAGE, {
      signal: controller.signal,
      headers: { 'User-Agent': 'IubipApp-Pult-Gateway/1.0' },
    });
    const raw = typeof response.headers.getSetCookie === 'function'
      ? response.headers.getSetCookie()
      : [response.headers.get('set-cookie')].filter(Boolean);
    const cookies = raw
      .map((header) => String(header).split(';')[0].trim())
      .filter((pair) => pair.length > 0);
    const beget = cookies.find((pair) => pair.startsWith('beget='));
    const cookie = beget || (cookies.length ? cookies.join('; ') : FALLBACK_COOKIE);
    log('info', `Прогрев страницы: HTTP ${response.status}, cookie = ${cookie}`);
    return cookie;
  } catch (error) {
    log('warn', `Прогрев страницы не удался (${error.message}); используем ${FALLBACK_COOKIE}`);
    return FALLBACK_COOKIE;
  } finally {
    clearTimeout(timer);
  }
}

/** Кэшируем куку на процесс: сайт выставляет её один раз. */
let cachedCookie = null;
async function cookie() {
  if (!cachedCookie) cachedCookie = await obtainCookie();
  return cachedCookie;
}

/**
 * POST на сайт с таймаутом. Возвращает текст ответа; Проверка JSON — в вызывающем.
 */
async function postUpstream(body, cookieHeader) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  try {
    const response = await fetch(UPSTREAM, {
      method: 'POST',
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'X-Requested-With': 'XMLHttpRequest',
        Cookie: cookieHeader,
        'User-Agent': 'IubipApp-Pult-Gateway/1.0',
        Accept: 'application/json, text/plain, */*',
      },
      body,
    });
    const text = await response.text();
    if (!response.ok) {
      throw new Error(`Сайт вернул HTTP ${response.status}: ${text.slice(0, 200)}`);
    }
    return text;
  } finally {
    clearTimeout(timer);
  }
}

/** Выполняет POST и, если ответ пустой/не JSON, повторяет с запасной cookie. */
async function fetchJson(body) {
  const firstCookie = await cookie();
  let text = await postUpstream(body, firstCookie);
  let parsed = tryParseJson(text);

  if (parsed === null) {
    log('warn', `Ответ не JSON (${text.length} байт); повторяем с ${FALLBACK_COOKIE}`);
    cachedCookie = FALLBACK_COOKIE;
    text = await postUpstream(body, FALLBACK_COOKIE);
    parsed = tryParseJson(text);
  }
  if (parsed === null) {
    throw new Error(`Сайт вернул не JSON (${text.length} байт): ${text.slice(0, 200)}`);
  }
  return { parsed, text };
}

function tryParseJson(text) {
  if (!text || !text.trim()) return null;
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

/** Ответ сайта «академия → объект(group→1)» → контракт пульта. */
function toAcademies(raw) {
  if (Array.isArray(raw) || typeof raw !== 'object' || raw === null) {
    throw new Error('Неожиданный формат списка групп');
  }
  return Object.entries(raw).map(([name, groups]) => {
    const list = groups && typeof groups === 'object' && !Array.isArray(groups)
      ? Object.keys(groups)
      : Array.isArray(groups) ? groups.map(String) : [];
    return { name, groups: list.filter(Boolean) };
  });
}

function sendJson(res, status, payload) {
  const text = JSON.stringify(payload);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(text),
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Cache-Control': 'no-store',
  });
  res.end(text);
}

function sendError(res, status, message) {
  log('error', message);
  sendJson(res, status, { error: message });
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);

  // CORS preflight для локального эмулятора/браузера.
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type',
      'Access-Control-Max-Age': '86400',
    });
    res.end();
    return;
  }

  try {
    if (req.method !== 'GET') {
      sendError(res, 405, 'Метод не поддерживается');
      return;
    }

    if (url.pathname === '/api/groups') {
      log('info', 'GET /api/groups — запрашиваем список групп');
      const { parsed } = await fetchJson('do=groups');
      const academics = toAcademies(parsed);
      log('info', `Список групп: ${academics.length} академий`);
      sendJson(res, 200, { academies: academics });
      return;
    }

    if (url.pathname === '/api/schedule') {
      const group = url.searchParams.get('group');
      if (!group) {
        sendError(res, 400, 'Не задан параметр group');
        return;
      }
      log('info', `GET /api/schedule?group=${group} — запрашиваем расписание`);
      const body = `do=schedule&group=${encodeURIComponent(group)}`;
      const { text } = await fetchJson(body);
      log('info', `Расписание получено: ${Buffer.byteLength(text)} байт`);
      // Отдаём КАК ЕСТЬ — парсер живёт в приложении.
      sendJsonText(res, 200, text);
      return;
    }

    if (url.pathname === '/' || url.pathname === '/health') {
      sendJson(res, 200, { status: 'ok', endpoints: ['/api/groups', '/api/schedule?group=...'] });
      return;
    }

    sendError(res, 404, 'Неизвестный путь');
  } catch (error) {
    sendError(res, 502, `Ошибка шлюза: ${error.message}`);
  }
});

/** Отдаёт уже готовый JSON-текст без пере-сериализации. */
function sendJsonText(res, status, text) {
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(text),
    'Access-Control-Allow-Origin': '*',
    'Cache-Control': 'no-store',
  });
  res.end(text);
}

server.listen(PORT, () => {
  log('info', `Шлюз слушает http://localhost:${PORT} (Node ${process.version})`);
});
