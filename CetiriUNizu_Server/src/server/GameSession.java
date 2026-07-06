package server;

import shared.*;

/**
 * Jedna partija izmedju dva igraca. Server je autoritativan: cuva tablu,
 * ko je na potezu i ko je poceo, i validira svaki potez. Metode synchronized.
 */
public class GameSession {

    private final ClientHandler playerA;   // inicijator -> igrac 1 (crveni)
    private final ClientHandler playerB;   // pozvani    -> igrac 2 (plavi)

    private final GameBoard board = new GameBoard();
    private int currentPlayerNumber;
    private int firstPlayerNumber;
    private boolean gameOver = false;

    private Boolean aRematch = null;
    private Boolean bRematch = null;

    public GameSession(ClientHandler playerA, ClientHandler playerB) {
        this.playerA = playerA;
        this.playerB = playerB;
    }

    private int numberFor(ClientHandler h) { return h == playerA ? 1 : 2; }

    public synchronized void start() {
        board.reset();
        gameOver = false;
        firstPlayerNumber = 1;
        currentPlayerNumber = firstPlayerNumber;
        sendStart();
    }

    private void sendStart() {
        playerA.send(new GameStartMessage(playerB.getUsername(), 1, currentPlayerNumber == 1));
        playerB.send(new GameStartMessage(playerA.getUsername(), 2, currentPlayerNumber == 2));
    }

    public synchronized void handleMove(ClientHandler from, int column) {
        int playerNum = numberFor(from);

        if (gameOver) {
            from.send(new ErrorMessage("Igra je zavrsena - odluci da li zelis revans."));
            return;
        }

        if (playerNum != currentPlayerNumber) {
            from.send(new ErrorMessage("Nije tvoj red - sacekaj da protivnik odigra."));
            return;
        }

        int row = board.dropDisc(column, playerNum);
        if (row == -1) {
            from.send(new ErrorMessage("Kolona je puna, izaberi drugu."));
            return;
        }

        boolean win = board.checkWin(row, column);
        boolean draw = !win && board.isFull();

        if (!win && !draw) {
            currentPlayerNumber = (playerNum == 1) ? 2 : 1;
        }

        BoardUpdateMessage update = new BoardUpdateMessage(column, row, playerNum, currentPlayerNumber);
        playerA.send(update);
        playerB.send(update);

        if (win) {
            gameOver = true;
            playerA.send(new GameOverMessage(playerNum, false));
            playerB.send(new GameOverMessage(playerNum, false));
        } else if (draw) {
            gameOver = true;
            playerA.send(new GameOverMessage(0, true));
            playerB.send(new GameOverMessage(0, true));
        }
    }

    public synchronized void handleRematch(ClientHandler from, boolean accepted) {
        if (!accepted) {
            endToLobby();
            return;
        }
        if (from == playerA) aRematch = true; else bRematch = true;

        if (Boolean.TRUE.equals(aRematch) && Boolean.TRUE.equals(bRematch)) {
            aRematch = null;
            bRematch = null;
            board.reset();
            gameOver = false;
            firstPlayerNumber = (firstPlayerNumber == 1) ? 2 : 1;   // zamena ko pocinje
            currentPlayerNumber = firstPlayerNumber;
            sendStart();
        }
    }

    private void endToLobby() {
        playerA.setStatus(PlayerStatus.AVAILABLE);
        playerB.setStatus(PlayerStatus.AVAILABLE);
        playerA.setSession(null);
        playerB.setSession(null);
        ServerMain.broadcastPlayerList();
    }

    public synchronized void handleDisconnect(ClientHandler who) {
        ClientHandler other = (who == playerA) ? playerB : playerA;
        if (other != null && ServerMain.clients.containsKey(other.getUsername())) {
            other.send(new ErrorMessage("Protivnik je napustio igru."));
            other.setStatus(PlayerStatus.AVAILABLE);
            other.setSession(null);
        }
    }

    /** Igrac je svesno napustio partiju (dugme "Napusti igru"). Oba se vracaju u lobi. */
    public synchronized void handleLeave(ClientHandler who) {
        gameOver = true;
        ClientHandler other = (who == playerA) ? playerB : playerA;
        if (other != null && ServerMain.clients.containsKey(other.getUsername())) {
            other.send(new ErrorMessage("Protivnik je napustio igru."));
            other.setStatus(PlayerStatus.AVAILABLE);
            other.setSession(null);
        }
        who.setStatus(PlayerStatus.AVAILABLE);
        who.setSession(null);
        ServerMain.broadcastPlayerList();
    }
}
