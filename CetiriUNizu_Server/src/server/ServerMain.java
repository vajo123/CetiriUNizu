package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Osnovni visenitni server za igru "4 u nizu".
 * Slusa na portu 5000 i za svaku novu konekciju pokrece zasebnu nit (ClientHandler).
 */
public class ServerMain {

    public static final int PORT = 5000;

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
}
