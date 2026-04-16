public class GameOfLife {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameOfLifeGUI gui = new GameOfLifeGUI();
            gui.setVisible(true);
        });
    }
}
