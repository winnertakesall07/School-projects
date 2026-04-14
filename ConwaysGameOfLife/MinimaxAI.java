public class MinimaxAI {
    private static final int WIN_SCORE  =  10;
    private static final int LOSE_SCORE = -10;

    public int[] getBestMove(TicTacToeBoard board) {
        int[][] state = board.getBoardCopy();
        int bestScore = Integer.MIN_VALUE;
        int bestRow = -1, bestCol = -1;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (state[r][c] == TicTacToeBoard.EMPTY) {
                    state[r][c] = TicTacToeBoard.AI;
                    int score = minimax(state, 0, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    state[r][c] = TicTacToeBoard.EMPTY;
                    if (score > bestScore) {
                        bestScore = score;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }
        return new int[]{bestRow, bestCol};
    }

    private int minimax(int[][] state, int depth, boolean isMaximizing, int alpha, int beta) {
        int winner = evalWinner(state);
        if (winner == TicTacToeBoard.AI)     return WIN_SCORE  - depth;
        if (winner == TicTacToeBoard.PLAYER) return LOSE_SCORE + depth;
        if (isFull(state)) return 0;

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (state[r][c] == TicTacToeBoard.EMPTY) {
                        state[r][c] = TicTacToeBoard.AI;
                        best = Math.max(best, minimax(state, depth + 1, false, alpha, beta));
                        state[r][c] = TicTacToeBoard.EMPTY;
                        alpha = Math.max(alpha, best);
                        if (beta <= alpha) return best;
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (state[r][c] == TicTacToeBoard.EMPTY) {
                        state[r][c] = TicTacToeBoard.PLAYER;
                        best = Math.min(best, minimax(state, depth + 1, true, alpha, beta));
                        state[r][c] = TicTacToeBoard.EMPTY;
                        beta = Math.min(beta, best);
                        if (beta <= alpha) return best;
                    }
                }
            }
            return best;
        }
    }

    private int evalWinner(int[][] state) {
        for (int i = 0; i < 3; i++) {
            if (state[i][0] != TicTacToeBoard.EMPTY &&
                state[i][0] == state[i][1] && state[i][1] == state[i][2]) return state[i][0];
            if (state[0][i] != TicTacToeBoard.EMPTY &&
                state[0][i] == state[1][i] && state[1][i] == state[2][i]) return state[0][i];
        }
        if (state[0][0] != TicTacToeBoard.EMPTY &&
            state[0][0] == state[1][1] && state[1][1] == state[2][2]) return state[0][0];
        if (state[0][2] != TicTacToeBoard.EMPTY &&
            state[0][2] == state[1][1] && state[1][1] == state[2][0]) return state[0][2];
        return TicTacToeBoard.EMPTY;
    }

    private boolean isFull(int[][] state) {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (state[r][c] == TicTacToeBoard.EMPTY) return false;
        return true;
    }
}
