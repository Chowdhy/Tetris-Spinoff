package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Next Piece listener is used to handle the event when a new piece is generated in Game. It passes the
 * GamePiece(s) of the current and following pieces.
 */
public interface NextPieceListener {

  /**
   * Handles a next piece event
   * @param nextPiece The current piece
   * @param followingPiece The following piece
   */
  void nextPiece(GamePiece nextPiece, GamePiece followingPiece);
}
