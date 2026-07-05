package shared;

import java.io.Serializable;

/** Poruka o gresci koju server salje klijentu (npr. igrac nedostupan). */
public class ErrorMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String text;
    public ErrorMessage(String text) { this.text = text; }
}
