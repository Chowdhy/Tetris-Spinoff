package uk.ac.soton.comp1206.event;

/**
 * The Game Loop listener is used to handle the event when a Timer in Game completes. It passes the
 * delay that was calculated in Game.
 */
public interface GameLoopListener {

  /**
   * Handle a game loop event
   * @param delay The timer duration
   */
  void timerStarted(int delay);
}
