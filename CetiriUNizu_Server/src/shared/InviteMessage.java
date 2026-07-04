package shared;

import java.io.Serializable;

/** Poziv na partiju: 'from' poziva igraca 'to'. */
public class InviteMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String from;
    public final String to;
    public InviteMessage(String from, String to) { this.from = from; this.to = to; }
}
