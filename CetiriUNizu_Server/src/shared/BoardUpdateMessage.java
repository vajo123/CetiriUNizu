package shared;

import java.io.Serializable;

/** Server javlja gde je dodat disk i ko je sledeci na potezu. */
public class BoardUpdateMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int column;
    public final int row;
    public final int player;
    public final int nextPlayerNumber;
    public BoardUpdateMessage(int column, int row, int player, int nextPlayerNumber) {
        this.column = column; this.row = row; this.player = player; this.nextPlayerNumber = nextPlayerNumber;
    }
}
