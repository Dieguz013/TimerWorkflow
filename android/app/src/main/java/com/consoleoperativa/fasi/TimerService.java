package com.consoleoperativa.fasi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

/**
 * Servizio in primo piano: tiene viva la sessione in background e mostra la notifica
 * fissa con il timer della fase (cronometro a ritroso, avanzamento, anticipo/ritardo).
 */
public class TimerService extends Service {
    static final String ACTION_UPDATE = "com.consoleoperativa.fasi.UPDATE";

    private static final int AMBER = Color.rgb(0xE3, 0xA2, 0x3D);
    private static final int RED = Color.rgb(0xE2, 0x55, 0x4A);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private PowerManager power;
    private SessionState state;
    private boolean inForeground = false;
    private BroadcastReceiver screenReceiver;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (state != null) {
                post(state);
            }
            // schermo spento: aggiornamenti rari (il cronometro continua da solo)
            handler.postDelayed(this, power.isInteractive() ? 1000 : 30000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        power = getSystemService(PowerManager.class);
        Channels.ensure(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // intent nullo = riavvio del sistema dopo la chiusura del processo: si riparte dai dati salvati
        state = SessionState.load(this);
        if (state == null) {
            startForegroundCompat(placeholder());
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
            return START_NOT_STICKY;
        }

        post(state);
        PhaseAlarm.schedule(this, state.endMs());

        handler.removeCallbacks(ticker);
        handler.postDelayed(ticker, 1000);
        registerScreenReceiver();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(ticker);
        if (screenReceiver != null) {
            try {
                unregisterReceiver(screenReceiver);
            } catch (IllegalArgumentException ignored) {
                // gia' rimosso
            }
            screenReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerScreenReceiver() {
        if (screenReceiver != null) return;
        screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                // schermo acceso: aggiorna subito la barra di avanzamento
                handler.removeCallbacks(ticker);
                handler.post(ticker);
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(screenReceiver, filter);
        }
    }

    private void post(SessionState s) {
        Notification n = build(s);
        if (!inForeground) {
            startForegroundCompat(n);
        } else {
            getSystemService(NotificationManager.class).notify(Channels.ID_LIVE, n);
        }
    }

    private void startForegroundCompat(Notification n) {
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(Channels.ID_LIVE, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(Channels.ID_LIVE, n);
        }
        inForeground = true;
    }

    private Notification placeholder() {
        return new Notification.Builder(this, Channels.LIVE)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setContentTitle("Console Operativa")
                .build();
    }

    private Notification build(SessionState s) {
        long elapsed = Math.max(0, (System.currentTimeMillis() - s.phaseStartMs) / 1000);
        boolean overtime = elapsed > s.plannedSec;
        long extra = Math.max(0, elapsed - s.plannedSec);
        long delta = s.bankedSec - extra;

        String status;
        if (delta > 5) {
            status = "▲ Anticipo +" + format(delta);
        } else if (delta < -5) {
            status = "▼ Ritardo −" + format(-delta);
        } else {
            status = "● In pari";
        }

        String title = s.phaseName + "   " + two(s.idx) + "/" + two(s.count);
        String text = s.phaseCode + "  ·  " + status;

        PendingIntent open = PendingIntent.getActivity(
                this, 0,
                new Intent(this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder b = new Notification.Builder(this, Channels.LIVE)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setContentTitle(title)
                .setContentText(text)
                .setSubText(overtime ? "EXTRA TIME" : s.code)
                .setContentIntent(open)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setColor(overtime ? RED : AMBER)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(s.endMs());

        if (Build.VERSION.SDK_INT >= 31) {
            b.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        if (Build.VERSION.SDK_INT >= 36) {
            LiveUpdates.apply(b, s, elapsed, overtime);
        } else {
            int planned = (int) Math.max(1, s.plannedSec);
            b.setProgress(planned, (int) Math.min(planned, elapsed), false);
        }
        return b.build();
    }

    private static String two(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    private static String format(long totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long sec = totalSeconds % 60;
        String mm = m < 10 ? "0" + m : String.valueOf(m);
        String ss = sec < 10 ? "0" + sec : String.valueOf(sec);
        return h > 0 ? h + ":" + mm + ":" + ss : mm + ":" + ss;
    }
}
