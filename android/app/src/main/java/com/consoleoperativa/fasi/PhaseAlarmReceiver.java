package com.consoleoperativa.fasi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

        String next = s.isLast()
                ? "Ultima fase completata"
                : "Prossima: " + s.nextName + " (" + s.nextCode + ")";

        PendingIntent open = PendingIntent.getActivity(
                ctx, 0,
                new Intent(ctx, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n = new Notification.Builder(ctx, Channels.ALERT)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setContentTitle("Fase scaduta: " + s.phaseName)
                .setContentText(next)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(open)
                .build();

        ctx.getSystemService(NotificationManager.class).notify(Channels.ID_ALERT, n);
    }
}
