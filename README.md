# Console Operativa — Timing Fasi

[![CI Android](https://github.com/Dieguz013/TimerWorkflow/actions/workflows/ci-android.yml/badge.svg)](https://github.com/Dieguz013/TimerWorkflow/actions/workflows/ci-android.yml)

Console per il controllo dei tempi di una sequenza di fasi operative. Ogni fase ha una durata pianificata, un codice univoco e una barra di avanzamento. La barra di stato mostra lo scostamento complessivo dal piano: anticipo, in pari o ritardo.

L'applicazione è una pagina web autonoma (`src/web/index.html`) distribuita anche come app Android (WebView) per l'uso su telefono.

## Funzionalità

- Sequenza di fasi configurabile (nome, durata in minuti e secondi), numero di fasi variabile.
- Codice fase univoco nel formato `FASE-NN-XXXs`, dove `XXX` è la durata pianificata in secondi.
- Avvio **programmato** a un orario prestabilito (il tempo già trascorso viene conteggiato) oppure **manuale**, con interruttore.
- Progress bar per fase; oltre il tempo previsto si entra in extra time.
- Avanzamento con swipe verso destra, ritorno alla fase precedente con swipe verso sinistra (nessun passaggio accidentale).
- Scostamento cumulato con simboli: ▲ anticipo, ● in pari, ▼ ritardo.
- Schermo sempre acceso durante la sessione, interfaccia adattata allo schermo del telefono.

## Struttura del repository

```
.
├── .github/
│   ├── workflows/ci-android.yml      Pipeline CI: verifica sorgenti + build APK
│   └── PULL_REQUEST_TEMPLATE.md
├── android/                          Progetto Android (Gradle, wrapper incluso)
│   ├── app/                          Modulo applicazione (WebView)
│   └── gradlew, gradle/              Gradle wrapper 8.7
├── docs/
│   ├── ARCHITETTURA.md               Modello di calcolo dei tempi e componenti
│   └── CONTRIBUIRE.md                Flusso di lavoro, convenzioni, test
├── scripts/
│   └── check-web.mjs                 Verifica statica della web app
├── src/
│   └── web/index.html                Sorgente unico dell'applicazione
├── CHANGELOG.md
└── README.md
```

Il file `src/web/index.html` è l'unica copia dell'interfaccia: il progetto Android lo include come asset durante la build, senza duplicarlo.

## Scaricare l'APK

1. Apri la scheda **Actions** del repository e scegli l'ultima esecuzione di **CI Android** con esito positivo.
2. In fondo alla pagina, sotto **Artifacts**, scarica `console-operativa-apk` (archivio zip).
3. Estrai l'archivio: contiene `console-operativa-<versione>-<commit>-debug.apk`.
4. Copia il file sul telefono, aprilo e consenti l'installazione da fonti sconosciute.

L'APK è firmato con la chiave di debug: adatto all'uso interno e al test, non alla pubblicazione su store.

## Sviluppo

Requisiti: Node.js 20 o successivo (verifiche), JDK 17 e Android SDK 34 (build Android).

```bash
# Verifica statica della web app
node scripts/check-web.mjs

# Build dell'APK (da android/)
cd android
./gradlew assembleDebug
# -> android/app/build/outputs/apk/debug/app-debug.apk
```

La web app si può provare aprendo `src/web/index.html` in un browser, senza build. Per il flusso di lavoro e le convenzioni vedi [docs/CONTRIBUIRE.md](docs/CONTRIBUIRE.md); per il funzionamento interno [docs/ARCHITETTURA.md](docs/ARCHITETTURA.md).

## Versioni

Le modifiche sono registrate in [CHANGELOG.md](CHANGELOG.md). La versione dell'app Android si trova in `android/app/build.gradle.kts` (`versionName`, `versionCode`).
