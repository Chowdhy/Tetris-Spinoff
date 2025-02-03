package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;
import uk.ac.soton.comp1206.ui.ScoresList;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The UI scene showing High Scores and their respective players
 */
public class ScoresScene extends BaseScene {
  private final Logger logger = LogManager.getLogger(ScoresScene.class);
  private final int score;
  private final ListProperty<Pair<String, Integer>> localScores;
  private final ListProperty<Pair<String, Integer>> remoteScores;
  private final File file;
  private final Communicator communicator;
  private BorderPane mainPane;
  private HBox scoresBox;
  private final Game game;
  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   * @param game The Game being played
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    logger.info("Creating Scores Screen");
    logger.info("Player scored: " + game.getScoreProperty().get());

    //Handles bindings
    this.score = game.getScoreProperty().get();
    localScores = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
    remoteScores = new SimpleListProperty<>(FXCollections.observableArrayList(new ArrayList<>()));
    communicator = gameWindow.getCommunicator();

    //Tries to open the file
    String fileName = "highScores.txt";
    file = new File(fileName);
    if (!file.exists()) {
      for (int i = 0; i < 10; i++) {
        var scorePair = new Pair<>("Username", 0);
        localScores.add(scorePair);
      }
      writeScores();
    } else {
      loadScores();
    }
  }

  @Override
  public void initialise() {
    logger.info("Initialising " + this.getClass().getName());

    loadOnlineScores();
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //Creates base/main pane
    mainPane = new BorderPane();
    mainPane.setMaxWidth(gameWindow.getWidth());
    mainPane.setMaxHeight(gameWindow.getHeight());
    mainPane.getStyleClass().add("scores-background");
    root.getChildren().add(mainPane);

    //Creates game over text container
    var lostBox = new VBox();
    lostBox.setAlignment(Pos.CENTER);
    lostBox.setPadding(new Insets(10));

    //Add game over text
    var title = new Text("Game Over");
    title.getStyleClass().add("title");

    //Add player score
    var score = new Text("You scored: " + this.score);
    score.getStyleClass().add("label");
    lostBox.getChildren().addAll(title, score);
    mainPane.setTop(lostBox);

    //Add local scores container
    scoresBox = new HBox();
    scoresBox.setAlignment(Pos.CENTER);
    scoresBox.setSpacing(30);

    //Handles local score reveal
    var scoreBox = new VBox();
    scoreBox.setAlignment(Pos.TOP_CENTER);
    if (game instanceof MultiplayerGame) {
      var highScores = new Text("Player Scores");
      highScores.getStyleClass().add("title");
      var scoresList = new Leaderboard();
      scoresList.multiplayerListProperty().bind(((MultiplayerGame) game).playerStatsProperty());
      scoresList.revealFinal();
      scoreBox.getChildren().addAll(highScores, scoresList);
    } else {
      var highScores = new Text("Local High Scores");
      highScores.getStyleClass().add("title");
      var scoresList = new ScoresList();
      scoresList.listProperty().bind(localScores);
      scoresList.reveal();
      scoreBox.getChildren().addAll(highScores, scoresList);
    }


    //Creates remote scores container
    var remoteScoreBox = new VBox();
    remoteScoreBox.setAlignment(Pos.TOP_CENTER);

    //Add online high score title
    var remoteHighScores = new Text("Online High Scores");
    remoteHighScores.getStyleClass().add("title");

    //Add remote scores list
    var remoteScoresList = new ScoresList();
    remoteScoresList.listProperty().bind(remoteScores);
    remoteScoresList.reveal();
    remoteScoreBox.getChildren().addAll(remoteHighScores, remoteScoresList);

    scoresBox.getChildren().addAll(scoreBox, remoteScoreBox);

    mainPane.setCenter(scoresBox);

    if (withinScores(localScores) != -1) {
      buildNewScoreBox();
    }

    //HISCORE listener
    communicator.addListener(message -> {
      Platform.runLater(() -> {
        if (message.startsWith("HISCORES")) {
          var formattedScores = message.substring(9).split("\n");
          for (var fScore : formattedScores) {
            addScore(fScore, remoteScores);
          }

          if (withinScores(remoteScores) != -1 && mainPane.getBottom() == null) {
            buildNewScoreBox();
          }
        }
      });
    });
  }

  @Override
  public void cleanup() {
    //Nothing to cleanup
  }

  private void buildNewScoreBox() {
    Multimedia.playAudio("sounds/pling.wav");

    //Create new score container
    var newScoreBox = new VBox();
    newScoreBox.getStyleClass().add("newScoreBox");
    newScoreBox.setAlignment(Pos.CENTER);

    //Add new score information text
    var newScore = new Text("You scored within the top 10 scores!");
    newScore.getStyleClass().add("heading");

    //Add name information text
    var namePrompt = new Text("Enter your name:");
    namePrompt.getStyleClass().add("heading");

    //Add name text field
    var nameField = new TextField(ScoresList.usernameProperty().get());
    nameField.getStyleClass().add("TextField");

    //Add submit button
    var submit = new Button("Submit");
    submit.getStyleClass().add("submit");


    newScoreBox.getChildren().addAll(newScore, namePrompt, nameField, submit);
    mainPane.setCenter(newScoreBox);
    mainPane.setBottom(scoresBox);

    submit.setOnAction((event -> submitScore(mainPane, nameField, scoresBox)));

    nameField.setOnKeyPressed(event -> {
      if (event.getCode() != KeyCode.ENTER)
        return;
      submitScore(mainPane, nameField, scoresBox);
    });
  }

  private String getLine(BufferedReader reader) {
    try {
      return reader.readLine();
    } catch (IOException exception) {
      return null;
    }
  }

  private void loadScores() {
    FileReader fileReader;
    BufferedReader reader;

    try {
      fileReader = new FileReader(file);
      reader = new BufferedReader(fileReader);

      String line;
      while ((line = getLine(reader)) != null) {
        addScore(line, localScores);
      }
    } catch (FileNotFoundException ignored) {

    }
  }

  private void submitScore(BorderPane mainPane, TextField nameField, HBox scoreBox) {
    nameField.setOnKeyPressed((keyEvent) -> {});
    mainPane.setBottom(null);
    mainPane.setCenter(scoreBox);
    ScoresList.usernameProperty().set(nameField.getText());
    writeScores();
    writeOnlineScore();
  }

  private void writeScores() {
    int localPosition = withinScores(localScores);
    if (localPosition != -1 && !ScoresList.usernameProperty().get().equals("Enter Username")) {
      var scorePair = new Pair<>(ScoresList.usernameProperty().get(), this.score);
      localScores.add(localPosition, scorePair);
    }
    try {
      file.createNewFile();
      var printStream = new PrintStream(file);
      printStream.print("");
      int counter = 0;

      //Write top 10 scores
      for (var score : localScores) {
        counter++;

        if (counter > 10) {
          break;
        }

        String stringToWrite = score.getKey() + ":" + score.getValue();
        printStream.println(stringToWrite);
      }
      printStream.close();
    } catch (IOException ignored) {
    }
  }

  private void loadOnlineScores() {
    communicator.send("HISCORES");
  }

  private void addScore(String line, ListProperty<Pair<String, Integer>> scoresListProperty) {
    var lineArray = line.split(":");
    var score = new Pair<>(lineArray[0], Integer.valueOf(lineArray[1]));
    var added = false;

    //Inserts score into the correct position
    for (int i = 0; i < scoresListProperty.size(); i++) {
      if (scoresListProperty.get(i).getValue().compareTo(score.getValue()) < 0) {
        scoresListProperty.add(i, score);
        added = true;
        break;
      }
    }
    if (!added) {
      scoresListProperty.add(score);
    }
  }

  private void writeOnlineScore() {
    int remotePosition = withinScores(remoteScores);
    if (remotePosition != -1 && !ScoresList.usernameProperty().get().equals("Enter Username")) {
      var scorePair = new Pair<>(ScoresList.usernameProperty().get(), this.score);
      remoteScores.add(remotePosition, scorePair);
      communicator.send("HISCORE " + scorePair.getKey() + ":" + scorePair.getValue());
    }
  }

  private int withinScores(ListProperty<Pair<String, Integer>> scoreListProperty) {
    for (int i = 0; i < 10; i++) {
      if (scoreListProperty.get(i).getValue() < this.score) {
        return i;
      }
    }
    return -1;
  }
}
