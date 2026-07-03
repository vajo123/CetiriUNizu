package shared;

import java.io.Serializable;
import java.util.ArrayList;

/** Server salje spisak trenutno dostupnih (registrovanih a slobodnih) igraca. */
public class PlayerListMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    public final ArrayList<String> players;
    public PlayerListMessage(ArrayList<String> players) { this.players = players; }
}
