package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A UI component that displays a list of scores that have been bound to
 */
public class ScoresList extends VBox {
  private static final Logger logger = LogManager.getLogger(ScoresList.class);
  /**
   * The list property holding the scores
   */
  protected final ListProperty<Pair<String, Integer>> localScores;
  private static final StringProperty username = new SimpleStringProperty("Enter Username");

  /**
   * Constructs the score list
   */
  public ScoresList() {
    this.localScores = new SimpleListProperty<>();

    getStyleClass().add("scorelist");

    this.localScores.addListener((ListChangeListener<? super Pair<String, Integer>>) (c) -> Platform.runLater(
        this::reveal));
  }

  /**
   * Updates the UI component to show the scores
   */
  public void reveal() {
    logger.info("Updating list");
    getChildren().clear();

    int counter = 0;

    //Creates labels for each score
    for (Pair<String, Integer> score : localScores) {
      logger.info("Inserting " + "<" + score.getKey() + " : " + score.getValue() + ">");
      counter++;
      if (counter > 10) break;

      //Create score container
      HBox scoreBox = new HBox();
      scoreBox.getStyleClass().add("scoreitem");
      scoreBox.setAlignment(Pos.CENTER);
      scoreBox.setSpacing(10);

      //Create name label
      var name = new Text(score.getKey());
      name.getStyleClass().add("label");
      name.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(name, Priority.ALWAYS);

      //Create points label
      var points = new Text(String.valueOf(score.getValue()));
      points.getStyleClass().add("label");
      points.setTextAlignment(TextAlignment.CENTER);
      HBox.setHgrow(points, Priority.ALWAYS);

      //Checks if the label is designated to client player
      if (username.get() != null && username.get().equals(name.getText())) {
        name.getStyleClass().add("myname");
        points.getStyleClass().add("myscore");
      }

      var fade = new FadeTransition(Duration.millis(750), scoreBox);
      fade.setFromValue(0);
      fade.setToValue(1);
      fade.setCycleCount(1);

      scoreBox.getChildren().addAll(name, points);

      fade.play();

      getChildren().add(scoreBox);
    }
  }

  /**
   * Returns the list property holding scores
   * @return The list property
   */
  public ListProperty<Pair<String, Integer>> listProperty() {
    return localScores;
  }

  /**
   * Returns the username property
   * @return The username property
   */
  public static StringProperty usernameProperty() {
    return username;
  }
}