# Changelog

Formato basato su [Keep a Changelog](https://keepachangelog.com/it/1.1.0/). Versionamento semantico.

## [1.1.0] - 2026-10-30

### Aggiunto
- Swipe verso sinistra con sblocco per tornare alla fase precedente.
- Interruttore per attivare o disattivare l'avvio programmato; con avvio manuale la sessione parte dal momento in cui si preme "Avvia sessione". L'impostazione e l'orario vengono ricordati.
- Schermo sempre acceso durante la sessione (Wake Lock sul web, `FLAG_KEEP_SCREEN_ON` su Android).
- Layout adattato allo schermo del telefono.
- App Android (WebView) generata dalla CI.

### Modificato
- L'avanzamento alla fase successiva avviene con swipe verso destra al posto del tasto.
- Riorganizzazione del repository: sorgente web in `src/web`, progetto Android in `android/`, documentazione in `docs/`, pipeline CI con verifica dei sorgenti.

### Rimosso
- Tasto "Indietro", sostituito dallo swipe.

## [1.0.0] - 2026-10-28

### Aggiunto
- Prima versione: sequenza di fasi configurabile, codici `FASE-NN-XXXs`, timer con orario di avvio, progress bar per fase, extra time e barra di stato con anticipo, pari o ritardo.
