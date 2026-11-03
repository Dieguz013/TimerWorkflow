# Changelog

Formato basato su [Keep a Changelog](https://keepachangelog.com/it/1.1.0/). Versionamento semantico.

## [1.3.0] - 2026-11-03

### Aggiunto
- Allarmi in stile impianti industriali e aeronautici: annunciatore NOM / CAUTION / WARNING, cornice a schermo intero lampeggiante (ambra negli ultimi secondi, rossa a fase scaduta), doppio bip di avviso e sirena in extra time, vibrazione dove disponibile. Il pulsante TACITA silenzia e ferma il lampeggio; in extra time l'allarme si riarma ogni minuto. Con "riduci movimento" attivo nel sistema i segnali restano fissi.
- Pulsanti "Reset fase" (il timer della fase riparte da zero) e "Annulla sessione", entrambi con finestra di conferma.

### Modificato
- Layout a schermo fisso: nessuno scorrimento in nessuna schermata, in verticale e in orizzontale, da 320 px di larghezza in su. Le fasi sono al massimo 8; il campo "Nome sessione" (non usato) è stato rimosso.
- I link "nuova sessione / modifica config" a doppio tocco sono sostituiti dai pulsanti con conferma.

## [1.2.0] - 2026-10-31

### Aggiunto
- Notifica fissa con il timer della fase: conto alla rovescia, barra di avanzamento, posizione nella sequenza e anticipo/ritardo complessivo.
- Stile strutturato delle notifiche: titolo con fase e posizione, riga di stato con la fase successiva, colore in base allo scostamento (azzurro anticipo, ambra in pari, rosso ritardo o extra time). La vista espansa mostra residuo, previsto, scostamento, prossima fase e la sequenza completa con l'esito di ogni fase.
- Su Android 16 la barra della notifica live è divisa in un segmento per fase, colorato in base all'esito.
- Avviso di fase scaduta con dettaglio della fase conclusa e di quella successiva.
- Notifica "Live" per Android 16 (Samsung Now Bar con One UI 8). Sui dispositivi precedenti resta la notifica normale.
- Servizio in primo piano che mantiene viva la sessione in background.
- Avviso di fase scaduta con suono e vibrazione anche a schermo spento o app chiusa (allarme esatto).
- Suoni nell'app: avvio, avanti, indietro, ultimi 5 secondi, scadenza e promemoria ogni minuto di extra time. Interruttore "Suoni" nella barra superiore.
- La sessione si salva a ogni cambio di stato e si ripristina alla riapertura, anche dopo ore. Le fasi configurate vengono ricordate.

### Modificato
- "Config" e "nuova sessione" richiedono un secondo tocco per terminare una sessione in corso.
- Build con Android Gradle Plugin 8.10.1, Gradle 8.11.1 e compileSdk 36.
- Funzionamento offline completo: font JetBrains Mono e IBM Plex Sans inclusi nell'app (licenza SIL OFL 1.1) al posto di Google Fonts, rimosso il permesso `INTERNET`. La verifica dei sorgenti blocca risorse e API di rete.

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
