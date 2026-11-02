package com.consoleoperativa.fasi;

import android.app.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Notifica "Live" di Android 16 (API 36), mostrata dai Samsung con One UI 8 nella Now Bar
 * e come chip nella barra di stato. La barra e' divisa in un segmento per fase:
 * azzurro anticipo, grigio in pari, rosso ritardo, ambra fase corrente, scuro da fare.
 *
 * Va usata solo con SDK_INT >= 36: la classe e' separata per non caricare API assenti
 * sui dispositivi piu' vecchi.
 */
final class LiveUpdates {
    private static final String EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing";

    private LiveUpdates() {}

    static void apply(Notification.Builder b, SessionState s, long elapsedSec, boolean overtime) {
        // richiesta di notifica "promossa" (Live): nell'SDK 36 si imposta tramite l'extra dedicato
        b.getExtras().putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true);
        if (overtime) {
            b.setShortCriticalText("EXTRA");
        }

        List<Notification.ProgressStyle.Segment> segments = new ArrayList<>();
        long total = 0;
        long doneSum = 0;

        if (s.plannedAll.length > 0) {
            for (int i = 0; i < s.plannedAll.length; i++) {
                int len = (int) Math.max(1, s.plannedAll[i]);
                int color;
                if (i < s.idx - 1) {
                    long d = i < s.done.length ? s.done[i] : 0;
                    color = d > 5 ? NotificationFactory.CYAN : (d < -5 ? NotificationFactory.RED : NotificationFactory.GREY);
                    doneSum += len;
                } else if (i == s.idx - 1) {
                    color = overtime ? NotificationFactory.RED : NotificationFactory.AMBER;
                } else {
                    color = NotificationFactory.DARK;
                }
                segments.add(new Notification.ProgressStyle.Segment(len).setColor(color));
                total += len;
            }
        } else {
            // payload senza elenco fasi: un solo segmento per la fase corrente
            int len = (int) Math.max(1, s.plannedSec);
            segments.add(new Notification.ProgressStyle.Segment(len)
                    .setColor(overtime ? NotificationFactory.RED : NotificationFactory.AMBER));
            total = len;
        }

        long inPhase = Math.min(Math.max(1, s.plannedSec), Math.max(0, elapsedSec));
        int progress = (int) Math.min(total, doneSum + inPhase);

        Notification.ProgressStyle style = new Notification.ProgressStyle()
                .setStyledByProgress(true)
                .setProgressSegments(segments)
                .setProgress(progress);
        b.setStyle(style);
    }
}
