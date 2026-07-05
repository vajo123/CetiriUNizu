package com.example.cetiriunizu_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import shared.BoardUpdateMessage;
import shared.ErrorMessage;
import shared.GameBoard;
import shared.GameOverMessage;
import shared.GameStartMessage;
import shared.MoveMessage;
import shared.PlayerListMessage;
import shared.RematchMessage;

/**
 * Aktivnost 2 - tok igre.
 * Tabla 7x6 se gradi dinamicki. Klik na kolonu salje potez serveru,
 * ali samo ako je trenutno red ovog igraca (myTurn). Server je autoritativan.
 */
public class GameActivity extends Activity implements NetworkManager.Listener {

    /** Prosledjeno iz LobbyActivity. */
    public static GameStartMessage pendingStart;

    private static final int ROWS = GameBoard.ROWS;   // 6
    private static final int COLS = GameBoard.COLS;   // 7

    private int myPlayerNumber;
    private boolean myTurn;
    private String opponent;

    private final GameBoard board = new GameBoard();
    private final ImageView[][] cells = new ImageView[ROWS][COLS];

    private TextView tvTurn;
    private GridLayout boardGrid;
    private LinearLayout columnButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvTurn = findViewById(R.id.tvTurn);
        boardGrid = findViewById(R.id.boardGrid);
        columnButtons = findViewById(R.id.columnButtons);

        GameStartMessage start = pendingStart;
        if (start != null) {
            myPlayerNumber = start.myPlayerNumber;
            myTurn = start.yourTurn;
            opponent = start.opponent;
        }

        buildColumnButtons();
        buildBoard();
        updateTurnLabel();

        NetworkManager.getInstance().setListener(this);
    }

    private void buildColumnButtons() {
        columnButtons.removeAllViews();
        for (int c = 0; c < COLS; c++) {
            final int col = c;
            Button b = new Button(this);
            b.setText("▼");
            b.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            b.setOnClickListener(v -> onColumnClick(col));
            columnButtons.addView(b);
        }
    }

    private void buildBoard() {
        boardGrid.removeAllViews();
        boardGrid.setColumnCount(COLS);
        boardGrid.setRowCount(ROWS);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                ImageView cell = new ImageView(this);
                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.width = 0;
                lp.height = 0;
                lp.columnSpec = GridLayout.spec(c, 1f);
                lp.rowSpec = GridLayout.spec(r, 1f);
                lp.setMargins(6, 6, 6, 6);
                cell.setLayoutParams(lp);
                cell.setImageResource(R.drawable.disc_empty);
                final int col = c;
                cell.setOnClickListener(v -> onColumnClick(col));
                cells[r][c] = cell;
                boardGrid.addView(cell);
            }
        }
    }

    private void onColumnClick(int col) {
        if (!myTurn) {
            Toast.makeText(this, "Nije tvoj red - sacekaj protivnika.", Toast.LENGTH_SHORT).show();
            return;
        }
        myTurn = false;
        updateTurnLabel();
        NetworkManager.getInstance().send(new MoveMessage(col));
    }

    @Override
    public void onMessage(Object message) {
        runOnUiThread(() -> handle(message));
    }

    private void handle(Object message) {
        if (message instanceof BoardUpdateMessage) {
            BoardUpdateMessage b = (BoardUpdateMessage) message;
            board.set(b.row, b.column, b.player);
            cells[b.row][b.column].setImageResource(
                    b.player == 1 ? R.drawable.disc_red : R.drawable.disc_blue);
            myTurn = (b.nextPlayerNumber == myPlayerNumber);
            updateTurnLabel();

        } else if (message instanceof GameOverMessage) {
            showGameOver((GameOverMessage) message);

        } else if (message instanceof GameStartMessage) {
            GameStartMessage g = (GameStartMessage) message;
            myPlayerNumber = g.myPlayerNumber;
            myTurn = g.yourTurn;
            opponent = g.opponent;
            board.reset();
            resetBoardUI();
            updateTurnLabel();
            Toast.makeText(this, "Nova partija!", Toast.LENGTH_SHORT).show();

        } else if (message instanceof ErrorMessage) {
            String t = ((ErrorMessage) message).text;
            Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
            if (t != null && t.toLowerCase().contains("puna")) {
                myTurn = true;
                updateTurnLabel();
            }

        } else if (message instanceof PlayerListMessage) {
            finish();   // partija zavrsena bez rematcha -> nazad u lobi
        }
    }

    private void resetBoardUI() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                cells[r][c].setImageResource(R.drawable.disc_empty);
            }
        }
    }

    private void updateTurnLabel() {
        String color = (myPlayerNumber == 1) ? "CRVENI" : "PLAVI";
        tvTurn.setText("Ti (" + NetworkManager.getInstance().getUsername() + ") - " + color
                + "  |  Protivnik: " + opponent + "\n"
                + (myTurn ? ">>> TVOJ RED <<<" : "Red protivnika..."));
    }

    private void showGameOver(GameOverMessage g) {
        String msg;
        if (g.draw) msg = "Nereseno!";
        else if (g.winner == myPlayerNumber) msg = "Pobedio si!";
        else msg = "Izgubio si.";

        new AlertDialog.Builder(this)
                .setTitle("Kraj igre")
                .setMessage(msg + "\n\nDa li zelis da igras ponovo?")
                .setCancelable(false)
                .setPositiveButton("Da", (d, w) ->
                        NetworkManager.getInstance().send(new RematchMessage(true)))
                .setNegativeButton("Ne", (d, w) -> {
                    NetworkManager.getInstance().send(new RematchMessage(false));
                    finish();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkManager.getInstance().setListener(this);
    }
}
