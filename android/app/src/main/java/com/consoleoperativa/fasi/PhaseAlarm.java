package com.consoleoperativa.fasi;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Allarme esatto a fine fase: suona anche con schermo spento o app chiusa. */
final class PhaseAlarm {
    private static final String ACTION = "com.consoleoperativa.fasi.PHASE_END";
    private static final int REQUEST_CODE = 7;

    private PhaseAlarm() {}

    private static PendingIntent intent(Context c) {
        Intent i = new Intent(c, PhaseAlarmReceiver.class).setAction(ACTION);
        return PendingIntent.getBroadcast(
                c, REQUEST_CODE, i, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static void schedule(Context c, long atMs) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (atMs <= System.currentTimeMillis()) {
            // fase gia' scaduta: nessun nuovo avviso
            am.cancel(intent(c));
            return;
        }
        try {
            if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMs, intent(c));
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMs, intent(c));
            }
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMs, intent(c));
        }
    }

    static void cancel(Context c) {
        c.getSystemService(AlarmManager.class).cancel(intent(c));
    }
}
