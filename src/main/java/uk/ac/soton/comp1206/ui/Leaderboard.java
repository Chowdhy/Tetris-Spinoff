package uk.ac.soton.comp1206.ui;

import java.util.ArrayList;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javatuples.Triplet;


/**
 * Displays the leaderboard for a multiplayer game
 */
public class Leaderboard extends ScoresList {
  private final static Logger logger = LogManager.getLogger(Leaderboard.class);

  /**
   * The local scores property
   */
  protected final ListProperty<Triplet<String, Integer, String>> localScores;

  /**
   * Constructs the leaderboard
   */
  public Leaderboard() {
    this.localScores = new SimpleListProperty<>();

    getStyleClass().add("scorelist");

    this.localScores.addListener(
        (ChangeListener<? super ObservableList<Triplet<String, Integer, String>>>) (c, d, e) -> Platform.runLater(
            this::reveal));
  }

  @Override
  public void reveal() {
    logger.info("Updating list");
    getChildren().clear();

    for (Triplet<String, Integer, String> score : localScores) {
      logger.info("Inserting " + "<" + score.getValue0() + " : " + score.getValue1() + " : " + score.getValue2() + ">");

      HBox scoreBox = new HBox();
      scoreBox.getStyleClass().add("scoreitem");
      scoreBox.setAlignment(Pos.CENTER);
      scoreBox.setSpacing(10);

      var name = new Text(score.getValue0());
      var points = new Text(String.valueOf(score.getValue1()));
      var lives = new Text(String.valueOf(score.getValue2()));

      name.getStyleClass().add("label");
      name.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(name, Priority.ALWAYS);

      points.getStyleClass().add("label");
      points.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(points, Priority.ALWAYS);

      lives.getStyleClass().add("label");
      lives.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(lives, Priority.ALWAYS);

      if (lives.getText().equals("DEAD")) {
        name.setStrikethrough(true);
        points.setStrikethrough(true);
        lives.setStrikethrough(true);
      }

      scoreBox.getChildren().addAll(name, points, lives);

      var fade = new FadeTransition(Duration.millis(750), scoreBox);
      fade.setFromValue(0);
      fade.setToValue(1);
      fade.setCycleCount(1);

      fade.play();

      getChildren().add(scoreBox);
    }
  }

  /**
   * Updates the leaderboard to show the final game status
   */
  public void revealFinal() {
    logger.info("Updating list");
    getChildren().clear();

    for (Triplet<String, Integer, String> score : localScores) {
      logger.info("Inserting " + "<" + score.getValue0() + " : " + score.getValue1() + ">");

      //Score container
      HBox scoreBox = new HBox();
      scoreBox.getStyleClass().add("scoreitem");
      scoreBox.setAlignment(Pos.CENTER);
      scoreBox.setSpacing(10);

      //Name label
      var name = new Text(score.getValue0());
      name.getStyleClass().add("label");
      name.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(name, Priority.ALWAYS);

      //Points label
      var points = new Text(String.valueOf(score.getValue1()));
      points.getStyleClass().add("label");
      points.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(points, Priority.ALWAYS);

      scoreBox.getChildren().addAll(name, points);

      var fade = new FadeTransition(Duration.millis(750), scoreBox);
      fade.setFromValue(0);
      fade.setToValue(1);
      fade.setCycleCount(1);

      fade.play();

      getChildren().add(scoreBox);
    }
  }

  /**
   * Returns the local scores property
   * @return The local scores property
   */
  public ListProperty<Triplet<String, Integer, String>> multiplayerListProperty() {
    return localScores;
  }
}
