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

    public GameSession(ClientHandler playerA, ClientHandler playerB) {
        this.playerA = playerA;
        this.playerB = playerB;
    }

    private int numberFor(ClientHandler h) { return h == playerA ? 1 : 2; }

    public synchronized void start() {
        board.reset();
        firstPlayerNumber = 1;                 // prvi igra onaj ko je inicirao (playerA)
        currentPlayerNumber = firstPlayerNumber;
        sendStart();
    }

    private void sendStart() {
        playerA.send(new GameStartMessage(playerB.getUsername(), 1, currentPlayerNumber == 1));
        playerB.send(new GameStartMessage(playerA.getUsername(), 2, currentPlayerNumber == 2));
    }

    public synchronized void handleMove(ClientHandler from, int column) {
        int playerNum = numberFor(from);

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
            playerA.send(new GameOverMessage(playerNum, false));
            playerB.send(new GameOverMessage(playerNum, false));
        } else if (draw) {
            playerA.send(new GameOverMessage(0, true));
            playerB.send(new GameOverMessage(0, true));
        }
    }
}
