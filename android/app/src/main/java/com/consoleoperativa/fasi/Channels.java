package com.consoleoperativa.fasi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.RingtoneManager;

/** Canali di notifica e identificatori. */
final class Channels {
    /** Notifica fissa con il timer della fase (silenziosa). */
    static final String LIVE = "timer_live";
    /** Avviso di fase scaduta (suono e vibrazione). */
    static final String ALERT = "phase_alert";

    static final int ID_LIVE = 1001;
    static final int ID_ALERT = 1002;

    private Channels() {}

    static void ensure(Context c) {
        NotificationManager nm = c.getSystemService(NotificationManager.class);

        NotificationChannel live = new NotificationChannel(
                LIVE, "Timer sessione", NotificationManager.IMPORTANCE_LOW);
        live.setDescription("Fase in corso con conto alla rovescia e avanzamento");
        live.setShowBadge(false);
        live.setSound(null, null);
        nm.createNotificationChannel(live);

        NotificationChannel alert = new NotificationChannel(
                ALERT, "Avvisi di fase", NotificationManager.IMPORTANCE_HIGH);
        alert.setDescription("Suono e vibrazione quando una fase scade");
        alert.enableVibration(true);
        alert.setVibrationPattern(new long[]{0, 400, 200, 400, 200, 800});
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        alert.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), attrs);
        nm.createNotificationChannel(alert);
    }
}
