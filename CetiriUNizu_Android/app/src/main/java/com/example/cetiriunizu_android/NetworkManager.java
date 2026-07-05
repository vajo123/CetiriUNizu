package com.example.cetiriunizu_android;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.PlayerListMessage;

/**
 * Singleton koji cuva JEDNU mreznu konekciju kroz obe aktivnosti.
 * Sva mrezna komunikacija ide u POZADINSKIM nitima (nikad na UI niti).
 */
public class NetworkManager {

    private static NetworkManager instance;

    public static synchronized NetworkManager getInstance() {
        if (instance == null) instance = new NetworkManager();
        return instance;
    }

    public interface Listener {
        void onMessage(Object message);
    }

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile Listener listener;
    private String username;
    private volatile PlayerListMessage lastPlayerList;

    public String getUsername() { return username; }
    public PlayerListMessage getLastPlayerList() { return lastPlayerList; }
    public void setListener(Listener l) { this.listener = l; }

    /** Povezivanje na server. MORA se pozvati iz pozadinske niti (blokira). */
    public boolean connect(String ip, int port, String username) {
        try {
            this.username = username;
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            startListening();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void startListening() {
        Thread t = new Thread(() -> {
            try {
                Object msg;
                while ((msg = in.readObject()) != null) {
                    if (msg instanceof PlayerListMessage) {
                        lastPlayerList = (PlayerListMessage) msg;
                    }
                    Listener l = listener;
                    if (l != null) l.onMessage(msg);
                }
            } catch (Exception e) {
                // konekcija zatvorena
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** Slanje poruke serveru - uvek iz nove pozadinske niti. */
    public void send(Object msg) {
        new Thread(() -> {
            try {
                if (out != null) {
                    out.writeObject(msg);
                    out.flush();
                    out.reset();
                }
            } catch (IOException ignored) {}
        }).start();
    }

    public void close() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        instance = null;
    }
}
