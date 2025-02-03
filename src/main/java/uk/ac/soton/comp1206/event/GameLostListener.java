package uk.ac.soton.comp1206.event;

/**
 * The Game Lost listener is used to handle the event when the challenge's lives have decreased to 0. It passes the
 * score that the player gained.
 */
public interface GameLostListener {

  /**
   * Handles a game lost event
   * @param score The score that was reached
   */
  void gameLost(int score);
}
