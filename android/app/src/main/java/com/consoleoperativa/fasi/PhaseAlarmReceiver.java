package com.consoleoperativa.fasi;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Avvisa che la fase e' scaduta quando l'app non e' in primo piano. */
public class PhaseAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        // in primo piano ci pensa la web app con il suo suono
        if (AppState.foreground) return;

        SessionState s = SessionState.load(ctx);
        if (s == null) return;

        Channels.ensure(ctx);
        ctx.getSystemService(NotificationManager.class)
                .notify(Channels.ID_ALERT, NotificationFactory.alert(ctx, s));
    }
}
