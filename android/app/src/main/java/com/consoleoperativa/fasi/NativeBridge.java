package com.consoleoperativa.fasi;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

/** Ponte JavaScript -> Android, esposta alla web app come window.AndroidBridge. */
class NativeBridge {
    private final Context ctx;

    NativeBridge(Context context) {
        this.ctx = context.getApplicationContext();
    }

    /** Stato della fase corrente: avvia o aggiorna servizio, notifica e allarme di fine fase. */
    @JavascriptInterface
    public void sessionUpdate(String json) {
        if (SessionState.parse(json) == null) return;
        SessionState.save(ctx, json);
        ctx.getSystemService(NotificationManager.class).cancel(Channels.ID_ALERT);
        try {
            ctx.startForegroundService(
                    new Intent(ctx, TimerService.class).setAction(TimerService.ACTION_UPDATE));
        } catch (RuntimeException e) {
            // avvio non consentito (app non in primo piano): la sessione resta comunque salvata
        }
    }

    /** Sessione terminata: rimuove notifica, allarme e dati salvati. */
    @JavascriptInterface
    public void sessionEnd() {
        SessionState.clear(ctx);
        PhaseAlarm.cancel(ctx);
        ctx.getSystemService(NotificationManager.class).cancel(Channels.ID_ALERT);
        ctx.stopService(new Intent(ctx, TimerService.class));
    }
}
