# Contribuire

## Flusso di lavoro

1. Parti sempre da `main` aggiornato e crea un ramo dedicato.
2. Fai commit piccoli e descrittivi.
3. Apri una pull request verso `main` usando il template e attendi la CI verde.
4. Unisci con **Squash and merge**, così `main` resta lineare.

## Nomi dei rami

| Prefisso | Uso | Esempio |
|---|---|---|
| `feat/` | nuova funzionalità | `feat/avvio-programmato` |
| `fix/` | correzione | `fix/timer-extra-time` |
| `chore/` | struttura, CI, manutenzione | `chore/struttura-repo` |
| `docs/` | solo documentazione | `docs/architettura` |

## Messaggi di commit

Formato `tipo: descrizione breve all'imperativo`, con gli stessi tipi dei rami (`feat`, `fix`, `chore`, `docs`).

```
feat: swipe indietro con sblocco
fix: ripristina il tempo della fase al ritorno indietro
```

## Verifiche in locale

```bash
node scripts/check-web.mjs          # sintassi e struttura della web app
cd android && ./gradlew assembleDebug   # build dell'APK
```

Per l'interfaccia apri `src/web/index.html` nel browser e prova anche a schermo stretto (simulazione telefono negli strumenti di sviluppo del browser).

## Regole per le modifiche

- L'interfaccia vive **solo** in `src/web/index.html`. Non copiarla dentro `android/`: la build la include da sola.
- Se un elemento richiesto dalla logica cambia id, aggiorna anche l'elenco in `scripts/check-web.mjs`.
- Ogni modifica visibile all'utente va annotata in `CHANGELOG.md`, sotto una nuova versione.
- Quando cambia la versione, aggiorna `versionName` e incrementa `versionCode` in `android/app/build.gradle.kts`.
- Non committare APK, cartelle `build/` o `local.properties` (sono già in `.gitignore`).
