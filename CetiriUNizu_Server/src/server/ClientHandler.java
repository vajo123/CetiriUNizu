package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Nit koja opsluzuje jednog klijenta.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private volatile PlayerStatus status = PlayerStatus.AVAILABLE;

    public ClientHandler(Socket socket) { this.socket = socket; }

    public PlayerStatus getStatus() { return status; }
    public void setStatus(PlayerStatus status) { this.status = status; }

    /** Slanje objekta klijentu. synchronized da se tok ne pokvari iz vise niti. */
    public synchronized void send(Object msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.out.println("Greska pri slanju klijentu.");
        }
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            Object msg;
            while ((msg = in.readObject()) != null) {
                System.out.println("Primljeno: " + msg);
            }
        } catch (Exception e) {
            // klijent se diskonektovao
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            System.out.println("Klijent se diskonektovao.");
        }
    }
}
