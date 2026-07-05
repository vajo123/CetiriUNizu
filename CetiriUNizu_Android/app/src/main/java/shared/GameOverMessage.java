package shared;

import java.io.Serializable;

/** Kraj partije: pobednik 1/2, ili nereseno. */
public class GameOverMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int winner;   // 1/2, 0 = nereseno
    public final boolean draw;
    public GameOverMessage(int winner, boolean draw) { this.winner = winner; this.draw = draw; }
}
