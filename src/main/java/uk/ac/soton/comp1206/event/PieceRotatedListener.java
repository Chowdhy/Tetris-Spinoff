package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Piece Rotated listener is used to handle the event when a Piece in Game is rotated. It passes the
 * GamePiece rotated.
 */
public interface PieceRotatedListener {

  /**
   * Handles a piece rotated event
   * @param piece The piece being rotated
   */
  void pieceRotated(GamePiece piece);
}
