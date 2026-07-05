package shared;

import java.io.Serializable;

/** Server obavestava oba igraca da partija pocinje i ko je koji igrac / ko je prvi. */
public class GameStartMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String opponent;
    public final int myPlayerNumber;   // 1 = crveni, 2 = plavi
    public final boolean yourTurn;
    public GameStartMessage(String opponent, int myPlayerNumber, boolean yourTurn) {
        this.opponent = opponent; this.myPlayerNumber = myPlayerNumber; this.yourTurn = yourTurn;
    }
}
