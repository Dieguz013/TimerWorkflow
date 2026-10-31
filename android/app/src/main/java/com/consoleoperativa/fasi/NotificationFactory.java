package com.consoleoperativa.fasi;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

/**
 * Costruisce le notifiche dell'app.
 *
 * <ul>
 *   <li>Timer fisso: titolo con fase e posizione, riga di stato (anticipo/ritardo e fase successiva),
 *       cronometro a ritroso, colore in base allo scostamento. Vista espansa con i dettagli
 *       e la sequenza completa delle fasi; su Android 16 la barra e' divisa in un segmento per fase.</li>
 *   <li>Avviso di fase scaduta: dettaglio della fase conclusa e di quella successiva.</li>
 * </ul>
 */
final class NotificationFactory {
    static final int CYAN = Color.rgb(0x4F, 0xC3, 0xD9);
    static final int AMBER = Color.rgb(0xE3, 0xA2, 0x3D);
    static final int RED = Color.rgb(0xE2, 0x55, 0x4A);
    static final int GREY = Color.rgb(0x7C, 0x88, 0x99);
    static final int DARK = Color.rgb(0x3A, 0x45, 0x52);

    private NotificationFactory() {}

    private static PendingIntent open(Context ctx) {
        return PendingIntent.getActivity(
                ctx, 0,
                new Intent(ctx, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /** Notifica fissa del timer. */
    static Notification live(Context ctx, SessionState s) {
        long elapsed = Math.max(0, (System.currentTimeMillis() - s.phaseStartMs) / 1000);
        long planned = s.plannedSec;
        boolean overtime = elapsed > planned;
        long extra = Math.max(0, elapsed - planned);
        long delta = s.bankedSec - extra;

        String status = status(delta);
        int accent = (overtime || delta < -5) ? RED : (delta > 5 ? CYAN : AMBER);

        String title = s.phaseName + "   " + two(s.idx) + "/" + two(s.count);
        String next = s.isLast() ? "ultima fase" : "poi " + s.nextName;
        String text = status + "  ·  " + next;
        String sub = s.phaseCode + (overtime ? "  ·  EXTRA TIME" : "");

        Notification.Builder b = new Notification.Builder(ctx, Channels.LIVE)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setContentTitle(title)
                .setContentText(text)
                .setSubText(sub)
                .setContentIntent(open(ctx))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setColor(accent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setShowWhen(true)
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(s.endMs());

        if (Build.VERSION.SDK_INT >= 31) {
            b.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        if (Build.VERSION.SDK_INT >= 36) {
            // stile di avanzamento: un segmento colorato per ogni fase
            LiveUpdates.apply(b, s, elapsed, overtime);
        } else {
            int p = (int) Math.max(1, planned);
            b.setProgress(p, (int) Math.min(p, elapsed), false);
            b.setStyle(new Notification.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(details(s, elapsed, planned, overtime, extra, status)));
        }
        return b.build();
    }

    /** Avviso di fase scaduta (suono e vibrazione). */
    static Notification alert(Context ctx, SessionState s) {
        String next = s.isLast()
                ? "Ultima fase completata"
                : "Prossima: " + s.nextName + " (" + s.nextCode + ")";

        String body = s.phaseCode + "  —  previsto " + format(s.plannedSec) + " raggiunto\n"
                + (s.isLast()
                ? "Ultima fase della sequenza: chiudi la sessione nell'app."
                : "Prossima fase:  " + s.nextName + "  ·  " + s.nextCode
                + "\nApri l'app e scorri per passare alla fase successiva.");

        return new Notification.Builder(ctx, Channels.ALERT)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setContentTitle("Fase scaduta  ·  " + s.phaseName)
                .setContentText(next)
                .setSubText(s.code)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setColor(RED)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(open(ctx))
                .build();
    }

    /** Segnaposto minimo, usato solo se il servizio parte senza dati di sessione. */
    static Notification placeholder(Context ctx) {
        return new Notification.Builder(ctx, Channels.LIVE)
                .setSmallIcon(R.drawable.ic_stat_timer)
                .setContentTitle("Console Operativa")
                .build();
    }

    private static String details(SessionState s, long elapsed, long planned,
                                  boolean overtime, long extra, String status) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fase ").append(s.idx).append(" di ").append(s.count)
                .append("   ·   ").append(s.phaseCode).append('\n');
        if (overtime) {
            sb.append("Extra time  +").append(format(extra));
        } else {
            sb.append("Residuo  ").append(format(planned - elapsed));
        }
        sb.append("   (previsto ").append(format(planned)).append(")\n");
        sb.append(status).append('\n');
        sb.append(s.isLast()
                ? "Ultima fase della sequenza"
                : "Prossima:  " + s.nextName + "  ·  " + s.nextCode);
        String seq = sequence(s);
        if (!seq.isEmpty()) {
            sb.append("\n\n").append(seq);
        }
        return sb.toString();
    }

    /** Riga con tutte le fasi: ✓ in pari, ▲ anticipo, ▼ ritardo, ▸ corrente, ○ da fare. */
    private static String sequence(SessionState s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.names.length; i++) {
            if (i > 0) sb.append("   ");
            String mark;
            if (i < s.idx - 1) {
                long d = i < s.done.length ? s.done[i] : 0;
                mark = d > 5 ? "▲" : (d < -5 ? "▼" : "✓");
            } else if (i == s.idx - 1) {
                mark = "▸";
            } else {
                mark = "○";
            }
            sb.append(mark).append(' ').append(s.names[i]);
        }
        return sb.toString();
    }

    private static String status(long delta) {
        if (delta > 5) return "▲ Anticipo +" + format(delta);
        if (delta < -5) return "▼ Ritardo −" + format(-delta);
        return "● In pari";
    }

    private static String two(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    static String format(long totalSeconds) {
        long t = Math.max(0, totalSeconds);
        long h = t / 3600;
        long m = (t % 3600) / 60;
        long sec = t % 60;
        String mm = m < 10 ? "0" + m : String.valueOf(m);
        String ss = sec < 10 ? "0" + sec : String.valueOf(sec);
        return h > 0 ? h + ":" + mm + ":" + ss : mm + ":" + ss;
    }
}
