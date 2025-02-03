package uk.ac.soton.comp1206.event;

import java.util.Set;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The Line Clear listener is used to handle the event when a line is cleared in Game. It passes the
 * number of lines cleard and the coordinates of the blocks to clear.
 */
public interface LineClearListener {

  /**
   * Handles a line clear event
   * @param linesCleared The number of lines cleared
   * @param coordinates The coordinates of the blocksb eing cleared
   */
  void clearLine(int linesCleared, Set<GameBlockCoordinate> coordinates);
}
