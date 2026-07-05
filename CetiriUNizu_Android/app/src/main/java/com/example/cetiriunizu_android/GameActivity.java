package com.example.cetiriunizu_android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import shared.GameStartMessage;

/**
 * Aktivnost 2 - tok igre.
 */
public class GameActivity extends Activity {

    /** Prosledjeno iz LobbyActivity. */
    public static GameStartMessage pendingStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        String opponent = pendingStart != null ? pendingStart.opponent : "?";
        tv.setText("Partija protiv " + opponent + " počinje...\n(tok igre se implementira u sledećem koraku)");
        tv.setPadding(48, 48, 48, 48);
        setContentView(tv);
    }
}
