package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {
    private final static Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Determines if a piece can be played based on the block coordinates and the piece
     * @param piece The piece to be played
     * @param x The x coordinate of the aim
     * @param y The y coordinate of the aim
     * @return A boolean representing if the piece can be played
     */
    public boolean canPlayPiece(GamePiece piece, int x, int y) {
        logger.info("Checking if piece can be played from centre at: x = " + x + ", y = " + y);
        int[][] blocks = piece.getBlocks();
        for (int i = -1; i <= 1; i++) {
            int[] squares = new int[3];
            squares[0] = get(x - 1, y + i);
            squares[1] = get(x, y + i);
            squares[2] = get(x + 1, y + i);

            for (int j = 0; j < 3; j++) {
                logger.info("Checking: x = " + (x + j - 1) + ", y = " + (y + i));
                if (blocks[i + 1][j] > 0 && squares[j] != 0) {
                    logger.info("Piece cannot be played");
                    return false;
                }
            }
        }
        logger.info("Piece can be played");
        return true;
    }

    /**
     * Plays the passed piece at the coordinates
     * @param piece The piece being played
     * @param x The x coordinate of the aim
     * @param y The y coordinate of the aim
     */
    public void playPiece(GamePiece piece, int x, int y) {
        logger.info("Playing piece at: x = " + x + ", y = " + y);
        int[][] blocks = piece.getBlocks();
        for (int i = -1; i <= 1; i++) {
            int square0 = get(x - 1, y + i);
            int square1 = get(x, y + i);
            int square2 = get(x + 1, y + i);

            if (blocks[i + 1][0] != 0) set(x - 1, y + i, square0 + blocks[i + 1][0]);
            if (blocks[i + 1][1] != 0) set(x, y + i, square1 + blocks[i + 1][1]);
            if (blocks[i + 1][2] != 0) set(x + 1, y + i, square2 + blocks[i + 1][2]);
        }
    }
}
