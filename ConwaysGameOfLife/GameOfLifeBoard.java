import java.util.Random;

public class GameOfLifeBoard {
    private int width;
    private int height;
    private boolean[][] cells;
    private boolean[][] nextCells;
    private int generation;

    public GameOfLifeBoard(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new boolean[height][width];
        this.nextCells = new boolean[height][width];
        this.generation = 0;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getGeneration() { return generation; }

    public boolean getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return false;
        return cells[y][x];
    }

    public void setCell(int x, int y, boolean alive) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        cells[y][x] = alive;
    }

    public void clear() {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                cells[y][x] = false;
        generation = 0;
    }

    public void randomize() {
        Random rng = new Random();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                cells[y][x] = rng.nextDouble() < 0.3;
        generation = 0;
    }

    private int countNeighbors(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = (x + dx + width) % width;
                int ny = (y + dy + height) % height;
                if (cells[ny][nx]) count++;
            }
        }
        return count;
    }

    public void nextGeneration() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbors = countNeighbors(x, y);
                if (cells[y][x]) {
                    nextCells[y][x] = (neighbors == 2 || neighbors == 3);
                } else {
                    nextCells[y][x] = (neighbors == 3);
                }
            }
        }
        boolean[][] tmp = cells;
        cells = nextCells;
        nextCells = tmp;
        generation++;
    }

    public void setCellPattern(Pattern p, int offsetX, int offsetY) {
        int[][] coords = p.getCoordinates();
        for (int[] coord : coords) {
            int x = coord[0] + offsetX;
            int y = coord[1] + offsetY;
            setCell(x, y, true);
        }
    }
}
