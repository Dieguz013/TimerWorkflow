package com.consoleoperativa.fasi;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private static final int REQ_NOTIFICATIONS = 1;

    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Schermo sempre acceso finche' l'app e' in primo piano
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.parseColor("#141A22"));
        getWindow().setNavigationBarColor(Color.parseColor("#0C1016"));

        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0C1016"));

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);          // salvataggio sessione e impostazioni (localStorage)
        s.setMediaPlaybackRequiresUserGesture(false);

        // ponte verso notifica, servizio in background e allarme di fine fase
        web.addJavascriptInterface(new NativeBridge(this), "AndroidBridge");

        web.setWebViewClient(new WebViewClient());
        setContentView(web);

        if (savedInstanceState != null) {
            web.restoreState(savedInstanceState);
        } else {
            web.loadUrl("file:///android_asset/index.html");
        }

        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppState.foreground = true;
        // l'utente e' tornato nell'app: l'avviso di fase scaduta non serve piu'
        getSystemService(NotificationManager.class).cancel(Channels.ID_ALERT);
    }

    @Override
    protected void onPause() {
        AppState.foreground = false;
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    // Il tasto indietro mette l'app in background invece di chiuderla,
    // cosi' la sessione in corso non va persa per errore.
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
        }
        super.onDestroy();
    }
}
