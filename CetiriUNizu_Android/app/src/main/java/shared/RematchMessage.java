package shared;

import java.io.Serializable;

/** Odgovor igraca na ponudu za revans (nova partija). */
public class RematchMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final boolean accepted;
    public RematchMessage(boolean accepted) { this.accepted = accepted; }
}
