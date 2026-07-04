package shared;

import java.io.Serializable;

/** Igrac odigrava potez tako sto bira kolonu (0-6). */
public class MoveMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int column;
    public MoveMessage(int column) { this.column = column; }
}
