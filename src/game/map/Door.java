package game.map;

public class Door {
    private int row, col;
    private boolean open;

    public Door(int row, int col) {
        this.row = row;
        this.col = col;
        this.open = false;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public boolean isOpen() { return open; }
    public void toggle() { open = !open; }
}
