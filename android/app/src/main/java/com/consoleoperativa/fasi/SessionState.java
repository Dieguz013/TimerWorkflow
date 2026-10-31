package com.consoleoperativa.fasi;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * Istantanea della sessione inviata dalla web app. Resta salvata in SharedPreferences
 * cosi' il servizio puo' ricostruire la notifica anche se il processo viene riavviato.
 */
final class SessionState {
    private static final String PREFS = "session";
    private static final String KEY = "json";

    String code = "";
    String phaseName = "";
    String phaseCode = "";
    int idx = 1;
    int count = 1;
    long plannedSec = 0;
    long phaseStartMs = 0;
    long bankedSec = 0;
    String nextName = "";
    String nextCode = "";

    static SessionState parse(String json) {
        try {
            JSONObject o = new JSONObject(json);
            SessionState s = new SessionState();
            s.code = o.optString("code");
            s.phaseName = o.optString("phaseName");
            s.phaseCode = o.optString("phaseCode");
            s.idx = o.optInt("idx", 1);
            s.count = o.optInt("count", 1);
            s.plannedSec = o.optLong("plannedSec", 0);
            s.phaseStartMs = o.optLong("phaseStartMs", 0);
            s.bankedSec = o.optLong("bankedSec", 0);
            s.nextName = o.optString("nextName");
            s.nextCode = o.optString("nextCode");
            if (s.phaseStartMs <= 0 || s.plannedSec <= 0) return null;
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    static void save(Context c, String json) {
        prefs(c).edit().putString(KEY, json).apply();
    }

    static SessionState load(Context c) {
        String json = prefs(c).getString(KEY, null);
        return json == null ? null : parse(json);
    }

    static void clear(Context c) {
        prefs(c).edit().remove(KEY).apply();
    }

    long endMs() {
        return phaseStartMs + plannedSec * 1000L;
    }

    boolean isLast() {
        return idx >= count;
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
