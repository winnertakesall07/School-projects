public class TicTacToeBoard {
    public static final int EMPTY  = 0;
    public static final int PLAYER = 1; // X
    public static final int AI     = 2; // O

    private int[][] board;
    private int currentPlayer;
    private int moveCount;

    public TicTacToeBoard() {
        board = new int[3][3];
        currentPlayer = PLAYER;
        moveCount = 0;
    }

    public void reset() {
        board = new int[3][3];
        currentPlayer = PLAYER;
        moveCount = 0;
    }

    public int getCell(int row, int col) { return board[row][col]; }

    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < 3 && col >= 0 && col < 3 && board[row][col] == EMPTY;
    }

    public boolean makeMove(int row, int col, int player) {
        if (!isValidMove(row, col)) return false;
        board[row][col] = player;
        moveCount++;
        currentPlayer = (player == PLAYER) ? AI : PLAYER;
        return true;
    }

    public int getCurrentPlayer() { return currentPlayer; }
    public int getMoveCount() { return moveCount; }

    public int checkWinner() {
        // Rows and cols
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != EMPTY && board[i][0] == board[i][1] && board[i][1] == board[i][2])
                return board[i][0];
            if (board[0][i] != EMPTY && board[0][i] == board[1][i] && board[1][i] == board[2][i])
                return board[0][i];
        }
        // Diagonals
        if (board[0][0] != EMPTY && board[0][0] == board[1][1] && board[1][1] == board[2][2])
            return board[0][0];
        if (board[0][2] != EMPTY && board[0][2] == board[1][1] && board[1][1] == board[2][0])
            return board[0][2];
        return EMPTY;
    }

    public boolean isDraw() { return moveCount == 9 && checkWinner() == EMPTY; }

    public boolean isGameOver() { return checkWinner() != EMPTY || isDraw(); }

    public int[][] getBoardCopy() {
        int[][] copy = new int[3][3];
        for (int r = 0; r < 3; r++)
            System.arraycopy(board[r], 0, copy[r], 0, 3);
        return copy;
    }

    public void setBoardState(int[][] state) {
        for (int r = 0; r < 3; r++)
            System.arraycopy(state[r], 0, board[r], 0, 3);
    }
}
