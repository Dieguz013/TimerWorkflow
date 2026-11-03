// Verifica statica della web app (src/web/index.html):
//  - sintassi dello script inline (compilazione senza esecuzione)
//  - presenza degli elementi richiesti dalla logica dell'app
//  - funzionamento offline: nessuna risorsa/API di rete, file locali (font) presenti
// Uso: node scripts/check-web.mjs [percorso/index.html]

import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';
import vm from 'node:vm';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const file = resolve(process.argv[2] ?? `${root}/src/web/index.html`);
const html = readFileSync(file, 'utf8');

const errors = [];

const scripts = [...html.matchAll(/<script(?![^>]*\bsrc=)[^>]*>([\s\S]*?)<\/script>/gi)];
if (scripts.length === 0) errors.push('nessuno script inline trovato');

scripts.forEach((m, i) => {
  try {
    new vm.Script(m[1], { filename: `script-inline-${i + 1}` });
  } catch (e) {
    errors.push(`errore di sintassi nello script ${i + 1}: ${e.message}`);
  }
});

const requiredIds = [
  'startTime', 'useSchedule', 'startSessionBtn', 'phaseList',
  'bigTimer', 'progressFill', 'statusPill',
  'swipe', 'swipeHandle', 'swipeBack', 'swipeBackHandle', 'timeline', 'soundBtn', 'ackBtn', 'lampNom', 'lampCau', 'lampWar', 'resetPhaseBtn', 'cancelBtn', 'confirmModal', 'confirmOk', 'confirmCancel',
];
for (const id of requiredIds) {
  if (!new RegExp(`id=["']${id}["']`).test(html)) errors.push(`manca l'elemento id="${id}"`);
}

if (!/name=["']viewport["']/.test(html)) errors.push('manca il meta viewport');

// Funzionamento offline: nessuna risorsa o chiamata di rete, e i file locali referenziati devono esistere
const externalRefs = [
  ...html.matchAll(/(?:src|href)\s*=\s*["']\s*(?:https?:)?\/\/[^"']+["']/gi),
  ...html.matchAll(/url\(\s*["']?\s*(?:https?:)?\/\/[^)]+\)/gi),
  ...html.matchAll(/@import\s+(?:url\(\s*)?["']?\s*(?:https?:)?\/\//gi),
];
for (const m of externalRefs) errors.push(`risorsa esterna non ammessa (offline): ${m[0].slice(0, 80)}`);

const networkApis = [...html.matchAll(/\b(fetch|XMLHttpRequest|WebSocket|EventSource|sendBeacon)\b/g)];
for (const m of networkApis) errors.push(`uso di API di rete non ammesso (offline): ${m[1]}`);

const baseDir = dirname(file);
let localRefs = 0;
for (const m of html.matchAll(/url\(\s*["']?([^"')\s]+)["']?\s*\)/gi)) {
  const ref = m[1];
  if (/^(data:|#|https?:|\/\/)/i.test(ref)) continue;
  localRefs++;
  if (!existsSync(resolve(baseDir, ref))) errors.push(`file locale mancante: ${ref}`);
}

if (errors.length > 0) {
  console.error(`KO  ${file}`);
  for (const e of errors) console.error(`  - ${e}`);
  process.exit(1);
}
console.log(`OK  ${file}  (${scripts.length} script, ${requiredIds.length} elementi, ${localRefs} file locali, 0 risorse di rete)`);
