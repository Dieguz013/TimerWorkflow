# Architettura

## Componenti

| Componente | Percorso | Ruolo |
|---|---|---|
| Web app | `src/web/index.html` | Interfaccia e logica dei tempi (HTML, CSS e JavaScript in un unico file, senza dipendenze) |
| App Android | `android/app/` | Contenitore WebView a schermo intero che carica la web app come asset |
| Pipeline CI | `.github/workflows/ci-android.yml` | Verifica i sorgenti e compila l'APK |
| Verifica statica | `scripts/check-web.mjs` | Controlla sintassi dello script e presenza degli elementi richiesti |

La web app non usa server né rete: lo stato vive in memoria durante la sessione. Solo l'interruttore di avvio programmato e l'orario impostato vengono ricordati (`localStorage`).

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

## App Android

- Activity unica con `WebView` che carica `file:///android_asset/index.html`.
- La cartella `src/web` è aggiunta come sorgente degli asset in `android/app/build.gradle.kts`, quindi non esiste una seconda copia dell'interfaccia.
- Il tasto indietro minimizza l'app per non perdere la sessione; la rotazione non ricrea l'activity.
- Permesso `INTERNET` richiesto solo per i font online; senza rete si usano i font di sistema.

## Pipeline CI

`CI Android` parte a ogni push su `main`, su ogni pull request e a richiesta.

1. **Verifica sorgenti web**: esegue `scripts/check-web.mjs`.
2. **Build APK (debug)**: valida il Gradle wrapper, prepara l'SDK, compila con `./gradlew assembleDebug` e pubblica l'artefatto `console-operativa-apk` (conservato 90 giorni).

Le esecuzioni concorrenti sullo stesso ramo vengono annullate a favore dell'ultima.
