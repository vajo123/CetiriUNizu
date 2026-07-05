package shared;

import java.io.Serializable;

/**
 * Logika table za igru "4 u nizu" (7 kolona x 6 redova).
 * Deli se izmedju servera i Android klijenta - MORA biti identicna (isti paket "shared").
 * 0 = prazno, 1 = igrac 1 (crveni), 2 = igrac 2 (plavi).
 */
public class GameBoard implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int ROWS = 6;
    public static final int COLS = 7;

    private final int[][] grid = new int[ROWS][COLS];

    public int dropDisc(int col, int player) {
        if (col < 0 || col >= COLS) return -1;
        for (int row = ROWS - 1; row >= 0; row--) {
            if (grid[row][col] == 0) {
                grid[row][col] = player;
                return row;
            }
        }
        return -1;
    }

    public void set(int row, int col, int player) { grid[row][col] = player; }

    public int get(int row, int col) { return grid[row][col]; }

    public boolean checkWin(int row, int col) {
        int player = grid[row][col];
        if (player == 0) return false;
        return line(row, col, 0, 1, player) >= 4
            || line(row, col, 1, 0, player) >= 4
            || line(row, col, 1, 1, player) >= 4
            || line(row, col, 1, -1, player) >= 4;
    }

    private int line(int row, int col, int dRow, int dCol, int player) {
        return 1 + dir(row, col, dRow, dCol, player) + dir(row, col, -dRow, -dCol, player);
    }

    private int dir(int row, int col, int dRow, int dCol, int player) {
        int r = row + dRow, c = col + dCol, n = 0;
        while (r >= 0 && r < ROWS && c >= 0 && c < COLS && grid[r][c] == player) {
            n++; r += dRow; c += dCol;
        }
        return n;
    }

    public boolean isFull() {
        for (int c = 0; c < COLS; c++) if (grid[0][c] == 0) return false;
        return true;
    }

    public void reset() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                grid[r][c] = 0;
    }
}
