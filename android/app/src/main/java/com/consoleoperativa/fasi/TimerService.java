package com.consoleoperativa.fasi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

/**
 * Servizio in primo piano: tiene viva la sessione in background e mantiene aggiornata
 * la notifica fissa del timer (vedi {@link NotificationFactory}).
 */
public class TimerService extends Service {
    static final String ACTION_UPDATE = "com.consoleoperativa.fasi.UPDATE";

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
            startForegroundCompat(NotificationFactory.placeholder(this));
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
        Notification n = NotificationFactory.live(this, s);
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
}
