package server;

import shared.PlayerListMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Osnovni visenitni server za igru "4 u nizu".
 * Slusa na portu 5000 i za svaku novu konekciju pokrece zasebnu nit (ClientHandler).
 */
public class ServerMain {

    public static final int PORT = 5000;

    /** Svi registrovani korisnici: ime -> handler. Thread-safe. */
    public static final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("=== Server '4 u nizu' pokrenut na portu " + PORT + " ===");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nova konekcija sa: " + socket.getInetAddress());
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Salje azuriran spisak dostupnih igraca svim dostupnim igracima (svako bez sebe). */
    public static synchronized void broadcastPlayerList() {
        ArrayList<String> available = new ArrayList<>();
        for (ClientHandler h : clients.values()) {
            if (h.getUsername() != null && h.getStatus() == PlayerStatus.AVAILABLE) {
                available.add(h.getUsername());
            }
        }
        for (ClientHandler h : clients.values()) {
            if (h.getUsername() != null && h.getStatus() == PlayerStatus.AVAILABLE) {
                ArrayList<String> copy = new ArrayList<>(available);
                copy.remove(h.getUsername());
                h.send(new PlayerListMessage(copy));
            }
        }
    }
}
