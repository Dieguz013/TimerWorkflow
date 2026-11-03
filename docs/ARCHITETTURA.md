# Architettura

## Componenti

| Componente | Percorso | Ruolo |
|---|---|---|
| Web app | `src/web/` | Interfaccia e logica dei tempi: `index.html` (HTML, CSS e JavaScript, senza dipendenze) e font locali in `fonts/` |
| App Android | `android/app/` | Contenitore WebView a schermo intero che carica la web app come asset |
| Pipeline CI | `.github/workflows/ci-android.yml` | Verifica i sorgenti e compila l'APK |
| Verifica statica | `scripts/check-web.mjs` | Controlla sintassi dello script e presenza degli elementi richiesti |

La web app non usa server né rete e funziona interamente offline: i font (JetBrains Mono e IBM Plex Sans, solo sottoinsieme latino, licenza SIL OFL 1.1) sono file locali in `src/web/fonts/`. `scripts/check-web.mjs` fa fallire la CI se compare una risorsa esterna, una chiamata di rete (`fetch`, `XMLHttpRequest`, `WebSocket`...) o un file locale mancante.

## Modello dei dati

- **Fase**: nome e durata pianificata in secondi.
- **Codice fase**: `FASE-NN-XXXs`, con `NN` posizione nella sequenza (due cifre) e `XXX` durata pianificata in secondi. Esempio: `FASE-02-1200s`.
- **Codice sessione**: `OP-AAAAMMGG-HHMM`, costruito con data e ora di avvio (programmato o manuale).
- **Registro fasi completate**: per ogni fase chiusa, durata pianificata ed effettiva.

## Calcolo dei tempi

**Avvio programmato.** All'avvio si calcola il tempo trascorso dall'orario impostato e si assume che le fasi precedenti siano state rispettate: la fase corrente è quella che comprende quell'istante, con il suo tempo già avviato. Se l'orario impostato è ancora nel futuro, la prima fase parte subito.

**Avvio manuale.** Con l'interruttore spento la sessione parte da zero nel momento in cui si preme "Avvia sessione".

**Extra time.** Superata la durata pianificata, il timer prosegue in rosso e la barra di avanzamento resta piena con tratteggio pulsante.

**Scostamento complessivo.**

```
scostamento = somma(pianificato - effettivo) delle fasi chiuse
              - extra time della fase corrente
```

Un valore positivo indica anticipo, negativo ritardo. Entro ±5 secondi lo stato è "in pari". Il tempo guadagnato in una fase compensa il ritardo di un'altra, perché il valore è cumulato.

| Stato | Simbolo | Condizione |
|---|---|---|
| Anticipo | ▲ | scostamento > 5 s |
| In pari | ● | tra -5 s e +5 s |
| Ritardo | ▼ | scostamento < -5 s |

## Navigazione tra le fasi

- **Avanti**: swipe verso destra. Il movimento va completato per almeno l'85% della corsa, altrimenti la maniglia torna all'inizio. Chiude la fase registrando il tempo effettivo.
- **Indietro**: swipe verso sinistra, stessa soglia. Riapre la fase precedente ripristinando il tempo effettivo già accumulato e cancella il suo esito dal registro.
- Da tastiera: freccia nella direzione dello swipe, ripetuta fino a fine corsa; `Esc` annulla.

## Schermo sempre acceso

- Web: Screen Wake Lock API, riacquisita quando la pagina torna in primo piano. L'indicatore in alto mostra lo stato e permette di riprovare con un tocco.
- Android: `FLAG_KEEP_SCREEN_ON` sulla finestra, attivo finché l'app è in primo piano.

## Persistenza della sessione

I tempi sono timestamp assoluti (inizio fase, tempi effettivi delle fasi chiuse), quindi lo stato si ricostruisce correttamente anche dopo ore. A ogni cambio (avvio, avanti, indietro, fine) la web app salva in `localStorage` la sessione (`cfSession`), le fasi configurate (`cfPhases`), l'orario programmato (`cfSchedule`) e la preferenza suoni (`cfSound`). All'apertura, se esiste una sessione salvata, viene ripristinata la console.

## Suoni

Sintetizzati con WebAudio, senza file. Eventi: avvio, avanti, indietro, un beep a ogni secondo negli ultimi 5, scadenza fase e promemoria ogni minuto di extra time. I suoni arretrati non vengono recuperati dopo una pausa in background. L'audio si abilita dopo il primo tocco dell'utente.

## Allarmi

| Livello | Condizione | Segnali |
|---|---|---|
| NOM | oltre la soglia di preavviso | nessuno |
| CAUTION | ultimi `min(30, max(5, 20% della durata))` secondi | cornice ambra lampeggiante, doppio bip ogni 5 s, tick negli ultimi 5 s |
| WARNING | fase scaduta (extra time) | cornice rossa lampeggiante, sirena, vibrazione |

TACITA silenzia e ferma il lampeggio del livello corrente; un cambio di livello o un nuovo minuto di extra time riarma l'allarme. Alla riapertura di una fase scaduta da più di 3 secondi l'allarme parte già tacitato. Lo stato è in `body[data-alarm]` e `body.ack`, il resto è CSS.

## Layout

La pagina non scorre mai: `html, body` hanno `overflow:hidden` e `100dvh`, gli spazi derivano dall'altezza dello schermo, le righe fase si comprimono (max 8 fasi) e in orizzontale basso la console passa a due colonne.

## Notifiche e background (Android)

```
web app (JS)  --AndroidBridge.sessionUpdate(json)-->  NativeBridge
                                                        |-- SessionState (SharedPreferences)
                                                        |-- TimerService (servizio in primo piano)
                                                        |     notifica fissa: cronometro a ritroso, avanzamento, stato
                                                        |     LiveUpdates (API 36): notifica promossa / Now Bar
                                                        '-- PhaseAlarm (allarme esatto a fine fase)
                                                              '-- PhaseAlarmReceiver: avviso sonoro se l'app non e' in primo piano
```

- La web app invia lo stato della fase a ogni cambio: codice, nome, posizione, durata, inizio e scostamento cumulato delle fasi chiuse. `sessionEnd` rimuove tutto.
- `TimerService` mostra la notifica con cronometro a ritroso gestito dal sistema (continua anche a schermo spento) e aggiorna avanzamento e scostamento ogni secondo, ogni 30 secondi a schermo spento. Dopo la fine della fase il cronometro prosegue in negativo (extra time).
- Lo stile delle notifiche è in `NotificationFactory`. Collassata: titolo `Fase  NN/NN`, riga di stato (▲ anticipo, ● in pari, ▼ ritardo, più la fase successiva), sottotitolo con il codice fase, cronometro a ritroso e colore d'accento in base allo scostamento. Espansa (Android < 16): residuo o extra time, previsto, scostamento, prossima fase e la sequenza completa con ✓ in pari, ▲ anticipo, ▼ ritardo, ▸ corrente, ○ da fare. Su Android 16 `LiveUpdates` usa lo stile di avanzamento con un segmento per fase (azzurro anticipo, grigio in pari, rosso ritardo, ambra corrente, scuro da fare). Le notifiche promosse non ammettono viste personalizzate, per questo si usano solo stili standard.
- Il payload include l'elenco delle fasi (`phases`) e lo scostamento in secondi delle fasi già chiuse (`done`).
- Se il processo viene chiuso, il servizio riparte dai dati salvati (`START_STICKY`) e ricostruisce la notifica.
- A fine fase `PhaseAlarm` programma un allarme esatto. Il ricevitore pubblica l'avviso sul canale "Avvisi di fase" (suono e vibrazione) solo se l'app non è visibile; in primo piano suona la web app.
- Su Android 16 (API 36) la notifica viene richiesta come "promossa" con stile di avanzamento: è il formato che la Samsung Now Bar con One UI 8 mostra come notifica live. La classe `LiveUpdates` è separata per non caricare API assenti sui dispositivi più vecchi.
- Permessi: notifiche, vibrazione, servizio in primo piano (tipo `specialUse`), allarme esatto (`USE_EXACT_ALARM`) e notifiche promosse.

## App Android

- Activity unica con `WebView` che carica `file:///android_asset/index.html`.
- La cartella `src/web` è aggiunta come sorgente degli asset in `android/app/build.gradle.kts`, quindi non esiste una seconda copia dell'interfaccia.
- Il tasto indietro minimizza l'app per non perdere la sessione; la rotazione non ricrea l'activity; toccando la notifica si riapre l'app già in esecuzione (`singleTask`).
- L'APK non dichiara il permesso `INTERNET`: l'app non può accedere alla rete, e tutto (font compresi) è incluso negli asset.

## Pipeline CI

`CI Android` parte a ogni push su `main`, su ogni pull request e a richiesta.

1. **Verifica sorgenti web**: esegue `scripts/check-web.mjs`.
2. **Build APK (debug)**: valida il Gradle wrapper, prepara l'SDK, compila con `./gradlew assembleDebug` e pubblica l'artefatto `console-operativa-apk` (conservato 90 giorni).

Le esecuzioni concorrenti sullo stesso ramo vengono annullate a favore dell'ultima.
