package com.consoleoperativa.fasi;

import android.app.Notification;
import android.graphics.Color;

import java.util.List;

/**
 * Notifica "Live" di Android 16 (API 36), mostrata dai Samsung con One UI 8 nella Now Bar
 * e come chip nella barra di stato. Va usata solo con SDK_INT >= 36: la classe e' separata
 * per non caricare API assenti sui dispositivi piu' vecchi.
 */
final class LiveUpdates {
    private static final int AMBER = Color.rgb(0xE3, 0xA2, 0x3D);
    private static final int RED = Color.rgb(0xE2, 0x55, 0x4A);
    private static final String EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing";

    private LiveUpdates() {}

    static void apply(Notification.Builder b, SessionState s, long elapsedSec, boolean overtime) {
        // richiesta di notifica "promossa" (Live): nell'SDK 36 si imposta tramite l'extra dedicato
        b.getExtras().putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true);
        if (overtime) {
            b.setShortCriticalText("EXTRA");
        }

        int planned = (int) Math.max(1, s.plannedSec);
        int progress = (int) Math.min(planned, Math.max(0, elapsedSec));

        Notification.ProgressStyle style = new Notification.ProgressStyle()
                .setStyledByProgress(false)
                .setProgressSegments(List.of(
                        new Notification.ProgressStyle.Segment(planned).setColor(overtime ? RED : AMBER)))
                .setProgress(progress);
        b.setStyle(style);
    }
}
