package server;

import shared.GameStartMessage;

/**
 * Jedna partija izmedju dva igraca. Odredjuje se ko je koji igrac
 * i ko je prvi na potezu, i salje obojici GameStartMessage.
 */
public class GameSession {

    private final ClientHandler playerA;   // inicijator -> igrac 1 (crveni)
    private final ClientHandler playerB;   // pozvani    -> igrac 2 (plavi)

    private int currentPlayerNumber;
    private int firstPlayerNumber;

    public GameSession(ClientHandler playerA, ClientHandler playerB) {
        this.playerA = playerA;
        this.playerB = playerB;
    }

    private int numberFor(ClientHandler h) { return h == playerA ? 1 : 2; }

    public synchronized void start() {
        firstPlayerNumber = 1;                 // prvi igra onaj ko je inicirao (playerA)
        currentPlayerNumber = firstPlayerNumber;
        sendStart();
    }

    private void sendStart() {
        playerA.send(new GameStartMessage(playerB.getUsername(), 1, currentPlayerNumber == 1));
        playerB.send(new GameStartMessage(playerA.getUsername(), 2, currentPlayerNumber == 2));
    }
}
