public class TicTacToe {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TicTacToeGUI gui = new TicTacToeGUI();
            gui.setVisible(true);
        });
    }
}
