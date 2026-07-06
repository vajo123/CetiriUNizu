package com.example.cetiriunizu_android;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    /** Jedna pozadinska nit za SVA slanja - garantuje redosled i sprecava
     *  istovremeni upis vise niti u isti ObjectOutputStream (korupcija toka). */
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "net-send");
        t.setDaemon(true);
        return t;
    });

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

    /** Slanje poruke serveru - uvek u pozadinskoj niti, ali SERIJALIZOVANO
     *  (jedan executor) tako da se poruke salju jedna za drugom bez korupcije. */
    public void send(Object msg) {
        sendExecutor.execute(() -> {
            try {
                if (out != null) {
                    out.writeObject(msg);
                    out.flush();
                    out.reset();
                }
            } catch (IOException ignored) {}
        });
    }

    public void close() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        instance = null;
    }
}
