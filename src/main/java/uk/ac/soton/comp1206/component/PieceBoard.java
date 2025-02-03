package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The visual component responsible for displaying an individual piece
 *
 * Extends the GameBoard
 */
public class PieceBoard extends GameBoard {

  /**
   * Constructs a piece board with the given height and width
   * @param width The width of the piece board
   * @param height The height of the piece board
   */
  public PieceBoard(double width, double height) {
    super(3, 3, width, height);
  }

  /**
   * Displays the piece passed to it
   * @param piece The piece to be displayed
   */
  public void displayPiece(GamePiece piece) {
    logger.info("Displaying new piece: " + piece.getValue());
    int[][] blocks = piece.getBlocks();

    for (int y = 0; y < 3; y++) {
      for (int x = 0; x < 3; x++) {
        grid.set(x, y, blocks[y][x]);
      }

      getBlock(1,1).showAim();
    }
  }
}
