'use strict';

/**
 * Локальный шлюз расписания iubip.ru (без зависимостей, только `node:http` + `fetch`,
 * Node.js 18+).
 *
 * Список групп берётся напрямую из DBF-файла institute (cp1251, dBase III): при старте
 * шлюз загружает его в память и обновляет каждые GROUPS_REFRESH_MS (по умолчанию 30 мин).
 * GET /api/groups отдаёт готовый список мгновенно, без обращения к сайту.
 *
 * Расписание по-прежнему запрашивается через AJAX-эндпоинт сайта и отдаётся приложению
 * как есть — парсер живёт в Android (JsonScheduleParser.parseAll).
 */

const http = require('node:http');
const { TextDecoder } = require('node:util');

const DBF_URL = process.env.GROUPS_DBF_URL ||
  'https://iubip.ru/upload/iblock/ab7/vr7vuzn1u41ly0mmcs6tah16c2ytu59k.DBF';
const SCHEDULE_UPSTREAM =
  'http://iubip.ru/local/templates/univer/include/schedule/ajax/read-file-groups.php';
const WARMUP_PAGE = 'http://iubip.ru/schedule/?special_version=N';
const FALLBACK_COOKIE = 'beget=begetok';
const TIMEOUT_MS = 15_000;

/** Интервал обновления списка групп (мс). Переопределяется env для проверки таймера. */
const GROUPS_REFRESH_MS = Number.parseInt(
  process.env.GROUPS_REFRESH_MS || String(30 * 60 * 1000),
  10,
);
const PORT = Number.parseInt(process.env.PORT || '3000', 10);

/** Индекс поля GROUP в dBase-заголовке (подтверждено по файлу института). */
const GROUP_FIELD_INDEX = 0;
const DELETED_FLAG = 0x2a;

/** Простой логгер с временной меткой — всё в stdout. */
function log(level, message) {
  console.log(`[${new Date().toISOString()}] [${level}] ${message}`);
}

// --------------------------------------------------------------------- DBF reader

/**
 * Минимальный reader dBase III/IV: 32-байтовый заголовок, 32-байтовые дескрипторы
 * полей, записи фиксированной длины, флаг удаления `0x2A` в первом байте записи.
 * Читает только символьные поля (C) — остальные типы пропускаются с позиционным
 * сдвигом. Никаких npm-зависимостей.
 *
 * @param {Buffer} buffer содержимое DBF-файла
 * @param {number} fieldIndex индекс поля, значения которого нужно вернуть
 * @returns {string[]} значения поля (сырые, с пробелами)
 */
function readDbfField(buffer, fieldIndex = GROUP_FIELD_INDEX) {
  if (!Buffer.isBuffer(buffer) || buffer.length < 32) {
    throw new Error('DBF: файл слишком короткий');
  }

  const version = buffer[0] & 0x7f;
  if (version !== 3 && version !== 4) {
    throw new Error(`DBF: неподдерживаемая версия 0x${buffer[0].toString(16)}`);
  }

  const recordCount = buffer.readUInt32LE(4);
  const headerLength = buffer.readUInt16LE(8);
  const recordLength = buffer.readUInt16LE(10);
  if (headerLength < 33 || headerLength > buffer.length) {
    throw new Error(`DBF: некорректная длина заголовка ${headerLength}`);
  }

  const fieldCount = Math.floor((headerLength - 32) / 32);
  const fields = [];
  let expectedRecordLength = 1; // байт флага deleted
  for (let i = 0; i < fieldCount; i += 1) {
    const descriptor = buffer.subarray(32 + i * 32, 64 + i * 32);
    if (descriptor[0] === 0x0d) break; // раньше времени дошли до терминатора
    const name = descriptor.subarray(0, 11).toString('ascii').split('\0')[0].trim();
    const type = String.fromCharCode(descriptor[11]);
    const length = descriptor[16];
    if (!length) throw new Error(`DBF: поле ${name} имеет нулевую длину`);
    fields.push({ name, type, length, offset: expectedRecordLength });
    expectedRecordLength += length;
  }
  if (expectedRecordLength !== recordLength) {
    throw new Error(
      `DBF: длина записи ${recordLength} не совпадает с суммой полей ${expectedRecordLength}`,
    );
  }
  if (buffer.length < headerLength + recordCount * recordLength) {
    throw new Error('DBF: файл обрезан');
  }

  const field = fields[fieldIndex];
  if (!field) throw new Error(`DBF: нет поля с индексом ${fieldIndex}`);
  if (field.type !== 'C') throw new Error(`DBF: поле ${field.name} не символьное (${field.type})`);

  const decoder = new TextDecoder('windows-1251');
  const values = [];
  for (let i = 0; i < recordCount; i += 1) {
    const recordStart = headerLength + i * recordLength;
    if (buffer[recordStart] === DELETED_FLAG) continue; // удалённая запись
    const start = recordStart + field.offset;
    values.push(decoder.decode(buffer.subarray(start, start + field.length)));
  }
  log('info', `DBF: ${recordCount} записей, поле ${field.name}, прочитано ${values.length}`);
  return values;
}

/**
 * trim() + схлопывание последовательностей пробелов (включая табуляции и NBSP).
 */
function normalizeGroup(value) {
  return value.replace(/\s+/gu, ' ').trim();
}

/** Скачивает DBF с таймаутом и собирает уникальный отсортированный список групп. */
async function loadGroups() {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  let buffer;
  try {
    const response = await fetch(DBF_URL, { signal: controller.signal });
    if (!response.ok) throw new Error(`DBF URL вернул HTTP ${response.status}`);
    buffer = Buffer.from(await response.arrayBuffer());
  } finally {
    clearTimeout(timer);
  }

  const groups = new Set();
  for (const value of readDbfField(buffer)) {
    const group = normalizeGroup(value);
    if (group) groups.add(group);
  }
  if (groups.size === 0) throw new Error('DBF: список групп пуст');

  const sorted = [...groups].sort((a, b) => a.localeCompare(b, 'ru'));
  log('info', `DBF: уникальных групп ${sorted.length}, первые 5: ${sorted.slice(0, 5).join(' | ')}`);
  return sorted;
}

// ----------------------------------------------------------- groups cache + timer

/** Состояние списка групп в памяти процесса. `stale` = обновление не удалось. */
let groupsCache = null;
let groupsStale = false;
let refreshTimer = null;

async function refreshGroups(reason) {
  try {
    const groups = await loadGroups();
    groupsCache = groups;
    groupsStale = false;
    log('info', `Обновление списка групп (${reason}): OK, ${groups.length} групп`);
  } catch (error) {
    // При неудачной попытке сохраняем предыдущий список и помечаем его устаревшим.
    groupsStale = true;
    log('error', `Обновление списка групп (${reason}) не удалось: ${error.message}`);
  }
}

async function startGroupsCache() {
  await refreshGroups('старт');
  if (!groupsCache) {
    log('error', 'Первоначальная загрузка списка групп не удалась — /api/groups вернёт 502');
  } else {
    refreshTimer = setInterval(() => {
      refreshGroups(`таймер ${GROUPS_REFRESH_MS} мс`);
    }, GROUPS_REFRESH_MS);
    refreshTimer.unref?.(); // таймер не должен мешать остановке процесса
    log('info', `Таймер обновления групп запущен: ${GROUPS_REFRESH_MS} мс`);
  }
}

// -------------------------------------------------------------- schedule upstream

/**
 * GET-запрос «прогрева»: сайт отдаёт JS, который ставит beget-куку.
 * Ошибка прогрева НЕ фатальна: основные POST-запросы всё равно пробуют cookie.
 */
async function obtainCookie() {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  try {
    const response = await fetch(WARMUP_PAGE, {
      signal: controller.signal,
      headers: { 'User-Agent': 'IubipApp-Gateway/1.0' },
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

let cachedCookie = null;
async function cookie() {
  if (!cachedCookie) cachedCookie = await obtainCookie();
  return cachedCookie;
}

/** POST на сайт с таймаутом. Возвращает текст ответа; проверка JSON — в вызывающем. */
async function postUpstream(body, cookieHeader) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);
  try {
    const response = await fetch(SCHEDULE_UPSTREAM, {
      method: 'POST',
      signal: controller.signal,
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
        'X-Requested-With': 'XMLHttpRequest',
        Cookie: cookieHeader,
        'User-Agent': 'IubipApp-Gateway/1.0',
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

function tryParseJson(text) {
  if (!text || !text.trim()) return null;
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

/** Выполняет POST и, если ответ пустой/не JSON, повторяет с запасной cookie. */
async function fetchSchedule(body) {
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
  return text;
}

// --------------------------------------------------------------------------- HTTP

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

function sendError(res, status, message) {
  log('error', message);
  sendJson(res, status, { error: message });
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);

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
      if (!groupsCache) {
        sendError(res, 502, 'Список групп ещё не загружен (первоначальная загрузка не удалась)');
        return;
      }
      // Отдаём из памяти мгновенно; дополнительное поле stale не ломает контракт.
      const payload = {
        academies: [{ name: 'Все группы', groups: groupsCache }],
        stale: groupsStale,
      };
      log('info', `GET /api/groups — из кеша, ${groupsCache.length} групп, stale=${groupsStale}`);
      sendJson(res, 200, payload);
      return;
    }

    if (url.pathname === '/api/schedule') {
      const group = url.searchParams.get('group');
      if (!group) {
        sendError(res, 400, 'Не задан параметр group');
        return;
      }
      log('info', `GET /api/schedule?group=${group} — запрашиваем расписание`);
      const text = await fetchSchedule(`do=schedule&group=${encodeURIComponent(group)}`);
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

server.listen(PORT, async () => {
  log('info', `Шлюз слушает http://localhost:${PORT} (Node ${process.version})`);
  log('info', `Источник групп: ${DBF_URL}`);
  // Сервер уже слушает: даже при долгой/неудачной загрузке /health отвечает сразу.
  await startGroupsCache();
});

/** Остановка таймера нужна только для тестов/аккуратного завершения процесса. */
process.on('SIGTERM', () => {
  if (refreshTimer) clearInterval(refreshTimer);
  server.close(() => process.exit(0));
});
process.on('SIGINT', () => {
  if (refreshTimer) clearInterval(refreshTimer);
  server.close(() => process.exit(0));
});
