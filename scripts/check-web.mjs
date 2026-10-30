// Verifica statica della web app (src/web/index.html):
//  - sintassi dello script inline (compilazione senza esecuzione)
//  - presenza degli elementi richiesti dalla logica dell'app
// Uso: node scripts/check-web.mjs [percorso/index.html]

import { readFileSync } from 'node:fs';
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
  'swipe', 'swipeHandle', 'swipeBack', 'swipeBackHandle', 'timeline',
];
for (const id of requiredIds) {
  if (!new RegExp(`id=["']${id}["']`).test(html)) errors.push(`manca l'elemento id="${id}"`);
}

if (!/name=["']viewport["']/.test(html)) errors.push('manca il meta viewport');

if (errors.length > 0) {
  console.error(`KO  ${file}`);
  for (const e of errors) console.error(`  - ${e}`);
  process.exit(1);
}
console.log(`OK  ${file}  (${scripts.length} script, ${requiredIds.length} elementi verificati)`);
