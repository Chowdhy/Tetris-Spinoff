package uk.ac.soton.comp1206.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.javatuples.Triplet;
import uk.ac.soton.comp1206.network.Communicator;

/**
 * The multiplayer implementation of the Game that communicates with the server
 */
public class MultiplayerGame extends Game {
  private final ListProperty<Triplet<String, Integer, String>> playerStats = new SimpleListProperty<>(
      FXCollections.observableList(new ArrayList<>()));
  private final Communicator communicator;
  private boolean started = false;
  private int generatedPieces;
  private final CyclicQueue pieceQueue;
  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   *
   * @param cols number of columns
   * @param rows number of rows
   * @param communicator the current communicator
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
    pieceQueue = new CyclicQueue(3);
  }

  @Override
  public void start() {
    communicator.addListener(message -> {
      Platform.runLater(() -> {
        if (message.startsWith("PIECE ")) {
          generatedPieces++;
          pieceQueue.enqueue(Integer.parseInt(message.substring(6)));
          if (!started && generatedPieces > 1) {
            started = true;
            initialiseGame();
          }
        } else if (message.startsWith("SCORES ")) {
          var playerStatistics = message.substring(7).split("\n");

          playerStats.clear();

          //Loops through player statistics and adds to list property
          for (var playerStat : playerStatistics) {
            var statArray = playerStat.split(":");
            var name = statArray[0];
            var playerScore = statArray[1];
            var playerLives = statArray[2];

            var triplet = new Triplet<>(name, Integer.parseInt(playerScore), playerLives);
            playerStats.add(triplet);
          }
          playerStats.sort(Collections.reverseOrder(
              Comparator.comparing(Triplet<String, Integer, String>::getValue1)));
        } else if (message.startsWith("SCORE ")) {
          communicator.send("SCORES");
        }
      });
    });

    for (int i = 0; i < pieceQueue.getSize(); i++) {
      communicator.send("PIECE");
    }
  }
  @Override
  public void initialiseGame() {
    logger.info("Initialising game");
    score.set(0);
    level.set(0);
    lives.set(3);
    multiplier.set(1);
    followingPiece = spawnPiece();
    nextPiece();
  }

  @Override
  protected GamePiece spawnPiece() {
    int number = pieceQueue.dequeue();
    logger.info("Spawning new piece: " + number);
    communicator.send("PIECE");
    return GamePiece.createPiece(number);
  }

  /**
   * Returns the player stats list property
   * @return The player stats property
   */
  public ListProperty<Triplet<String, Integer, String>> playerStatsProperty() {
    return playerStats;
  }
}
