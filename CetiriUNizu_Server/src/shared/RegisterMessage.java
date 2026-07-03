package shared;

import java.io.Serializable;

/** Klijent salje svoje korisnicko ime da bi se registrovao na server. */
public class RegisterMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String username;
    public RegisterMessage(String username) { this.username = username; }
}
