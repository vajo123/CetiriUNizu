package server;

import shared.*;

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

    private String username;
    private volatile PlayerStatus status = PlayerStatus.AVAILABLE;
    private GameSession session;

    public ClientHandler(Socket socket) { this.socket = socket; }

    public String getUsername() { return username; }
    public PlayerStatus getStatus() { return status; }
    public void setStatus(PlayerStatus status) { this.status = status; }
    public GameSession getSession() { return session; }
    public void setSession(GameSession session) { this.session = session; }

    /** Slanje objekta klijentu. synchronized da se tok ne pokvari iz vise niti. */
    public synchronized void send(Object msg) {
        try {
            out.writeObject(msg);
            out.flush();
            out.reset();
        } catch (IOException e) {
            System.out.println("Greska pri slanju ka " + username);
        }
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            Object first = in.readObject();
            if (first instanceof RegisterMessage) {
                handleRegister((RegisterMessage) first);
            }

            Object msg;
            while ((msg = in.readObject()) != null) {
                handleMessage(msg);
            }
        } catch (Exception e) {
            // klijent se diskonektovao
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Object msg) {
        if (msg instanceof InviteMessage) {
            handleInvite((InviteMessage) msg);
        } else if (msg instanceof InviteResponseMessage) {
            handleInviteResponse((InviteResponseMessage) msg);
        } else if (msg instanceof MoveMessage) {
            if (session != null) session.handleMove(this, ((MoveMessage) msg).column);
        } else if (msg instanceof RematchMessage) {
            if (session != null) session.handleRematch(this, ((RematchMessage) msg).accepted);
        }
    }

    private void handleRegister(RegisterMessage m) {
        this.username = m.username;
        this.status = PlayerStatus.AVAILABLE;
        ServerMain.clients.put(username, this);
        System.out.println("Registrovan igrac: " + username);
        ServerMain.broadcastPlayerList();
    }

    private void handleInvite(InviteMessage m) {
        ClientHandler target = ServerMain.clients.get(m.to);
        if (target == null || target.getStatus() != PlayerStatus.AVAILABLE) {
            send(new ErrorMessage("Igrac " + m.to + " trenutno nije dostupan."));
            return;
        }
        target.send(new InviteMessage(username, m.to));
    }

    private void handleInviteResponse(InviteResponseMessage m) {
        ClientHandler inviter = ServerMain.clients.get(m.to);
        if (inviter == null) return;

        if (m.accepted) {
            GameSession gs = new GameSession(inviter, this);
            inviter.setSession(gs);
            this.setSession(gs);
            inviter.setStatus(PlayerStatus.BUSY);
            this.setStatus(PlayerStatus.BUSY);
            gs.start();
            ServerMain.broadcastPlayerList();
        } else {
            inviter.send(new InviteResponseMessage(username, m.to, false));
        }
    }

    private void cleanup() {
        if (username != null) {
            ServerMain.clients.remove(username);
            if (session != null) session.handleDisconnect(this);
            ServerMain.broadcastPlayerList();
            System.out.println("Igrac napustio: " + username);
        }
        try { socket.close(); } catch (IOException ignored) {}
    }
}
