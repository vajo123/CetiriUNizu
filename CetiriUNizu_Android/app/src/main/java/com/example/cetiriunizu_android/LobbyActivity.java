package com.example.cetiriunizu_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import shared.GameStartMessage;
import shared.InviteMessage;
import shared.InviteResponseMessage;
import shared.PlayerListMessage;
import shared.RegisterMessage;

/**
 * Aktivnost 1 - povezivanje sa serverom i lobi.
 * Unos IP/port/ime -> konekcija -> registracija -> spisak igraca ->
 * pozivanje/primanje poziva -> pocetak igre (Aktivnost 2).
 */
public class LobbyActivity extends Activity implements NetworkManager.Listener {

    private EditText etIp, etPort, etUsername;
    private Button btnConnect;
    private LinearLayout playersContainer;
    private TextView tvStatus, tvEmpty;

    private final ArrayList<String> players = new ArrayList<>();
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        etUsername = findViewById(R.id.etUsername);
        btnConnect = findViewById(R.id.btnConnect);
        playersContainer = findViewById(R.id.playersContainer);
        tvStatus = findViewById(R.id.tvStatus);
        tvEmpty = findViewById(R.id.tvEmpty);

        btnConnect.setOnClickListener(v -> connect());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (connected) {
            NetworkManager.getInstance().setListener(this);
            PlayerListMessage last = NetworkManager.getInstance().getLastPlayerList();
            if (last != null) updatePlayers(last.players);
        }
    }

    private void connect() {
        String ip = etIp.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (ip.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Popuni IP, port i korisnicko ime.", Toast.LENGTH_SHORT).show();
            return;
        }
        final int port;
        try { port = Integer.parseInt(portStr); }
        catch (NumberFormatException e) {
            Toast.makeText(this, "Port mora biti broj.", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("Povezivanje...");
        btnConnect.setEnabled(false);   // odmah onemoguci da dupli klik ne otvori vise konekcija
        new Thread(() -> {
            boolean ok = NetworkManager.getInstance().connect(ip, port, username);
            runOnUiThread(() -> {
                if (ok) {
                    connected = true;
                    NetworkManager.getInstance().setListener(this);
                    NetworkManager.getInstance().send(new RegisterMessage(username));
                    tvStatus.setText("Povezan kao: " + username);
                    etIp.setEnabled(false);
                    etPort.setEnabled(false);
                    etUsername.setEnabled(false);
                } else {
                    tvStatus.setText("Neuspesno povezivanje.");
                    btnConnect.setEnabled(true);   // vrati mogucnost pokusaja na neuspeh
                    Toast.makeText(this, "Ne mogu da se povezem na server.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    @Override
    public void onMessage(Object message) {
        runOnUiThread(() -> handle(message));
    }

    private void handle(Object message) {
        if (message instanceof PlayerListMessage) {
            updatePlayers(((PlayerListMessage) message).players);
        } else if (message instanceof InviteMessage) {
            showInviteDialog((InviteMessage) message);
        } else if (message instanceof InviteResponseMessage) {
            InviteResponseMessage r = (InviteResponseMessage) message;
            if (!r.accepted) {
                Toast.makeText(this, r.from + " je odbio poziv. Izaberi drugog igraca.",
                        Toast.LENGTH_LONG).show();
            }
        } else if (message instanceof GameStartMessage) {
            GameActivity.pendingStart = (GameStartMessage) message;
            startActivity(new Intent(this, GameActivity.class));
        }
    }

    private void updatePlayers(ArrayList<String> list) {
        players.clear();
        players.addAll(list);
        renderPlayers();
    }

    /** Crta dostupne igrace kao kartice u kontejneru (radi i unutar ScrollView-a). */
    private void renderPlayers() {
        playersContainer.removeAllViews();
        tvEmpty.setVisibility(players.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = getLayoutInflater();
        int gap = (int) (10 * getResources().getDisplayMetrics().density);
        for (String name : players) {
            final String opponent = name;
            View row = inflater.inflate(R.layout.row_player, playersContainer, false);
            ((TextView) row.findViewById(R.id.tvPlayerName)).setText(name);

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) row.getLayoutParams();
            lp.bottomMargin = gap;
            row.setLayoutParams(lp);

            row.setOnClickListener(v -> {
                NetworkManager.getInstance().send(
                        new InviteMessage(NetworkManager.getInstance().getUsername(), opponent));
                Toast.makeText(this, "Poziv poslat igraču: " + opponent, Toast.LENGTH_SHORT).show();
            });
            playersContainer.addView(row);
        }
    }

    private void showInviteDialog(InviteMessage m) {
        new AlertDialog.Builder(this)
                .setTitle("Poziv za igru")
                .setMessage(m.from + " te poziva na partiju. Prihvatas?")
                .setCancelable(false)
                .setPositiveButton("Prihvati", (d, w) ->
                        NetworkManager.getInstance().send(new InviteResponseMessage(
                                NetworkManager.getInstance().getUsername(), m.from, true)))
                .setNegativeButton("Odbij", (d, w) ->
                        NetworkManager.getInstance().send(new InviteResponseMessage(
                                NetworkManager.getInstance().getUsername(), m.from, false)))
                .show();
    }
}
