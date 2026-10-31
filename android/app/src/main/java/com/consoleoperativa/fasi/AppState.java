package com.consoleoperativa.fasi;

/** Stato condiviso a livello di processo. */
final class AppState {
    /** true mentre l'activity e' visibile: in quel caso gli avvisi sonori li gestisce la web app. */
    static volatile boolean foreground = false;

    private AppState() {}
}
