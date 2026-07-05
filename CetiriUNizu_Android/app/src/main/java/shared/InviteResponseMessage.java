package shared;

import java.io.Serializable;

/** Odgovor na poziv: 'from' (pozvani) odgovara 'to' (onaj ko je pozvao). */
public class InviteResponseMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String from;     // pozvani
    public final String to;       // onaj ko je pozvao
    public final boolean accepted;
    public InviteResponseMessage(String from, String to, boolean accepted) {
        this.from = from; this.to = to; this.accepted = accepted;
    }
}
