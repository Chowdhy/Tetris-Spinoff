package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.javatuples.Triplet;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.ChatBox;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The UI scene responsible for showing the multiplayer game
 */
public class MultiplayerScene extends ChallengeScene {
  private final Communicator communicator;
  private MultiplayerGame game;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    setupGame();

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("multiplayer-background");
    root.getChildren().add(challengePane);

    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
    mainPane.setCenter(board);

    nextPieceBoard = (new PieceBoard(140, 140));
    challengePane.getChildren().add(nextPieceBoard);
    nextPieceBoard.setTranslateX(200);
    nextPieceBoard.setTranslateY(-60);

    var followingPieceBoard = new PieceBoard(100, 100);
    challengePane.getChildren().add(followingPieceBoard);
    followingPieceBoard.setTranslateX(330);
    followingPieceBoard.setTranslateY(-60);

    game.setNextPieceListener((nextPiece, followingPiece) -> {
      nextPieceBoard.displayPiece(nextPiece);
      followingPieceBoard.displayPiece(followingPiece);
    });

    game.setLineClearListener((linesCleared, coordinates) -> {
      Multimedia.playAudio("sounds/clear.wav");
      board.fadeOut(coordinates);
    });

    var scoreProperty = game.getScoreProperty();
    var score = new Text();
    score.getStyleClass().add("score");
    score.setStyle("-fx-font-size: 20");
    score.textProperty().bind(Bindings.concat("Score: ", scoreProperty));

    var levelProperty = game.getLevelProperty();
    var level = new Text();
    level.getStyleClass().add("level");
    level.setStyle("-fx-font-size: 20");
    level.textProperty().bind(Bindings.concat("Level: ", levelProperty));

    var livesProperty = game.getLivesProperty();
    var lives = new Text();
    lives.getStyleClass().add("lives");
    lives.setStyle("-fx-font-size: 20");
    lives.textProperty().bind(Bindings.concat("Lives: ", livesProperty));

    var multiplierProperty = game.getMultiplierProperty();
    var multiplier = new Text();
    multiplier.getStyleClass().add("lives");
    multiplier.setStyle("-fx-font-size: 20");
    multiplier.textProperty().bind(Bindings.concat("Multiplier: x", multiplierProperty));

    var highScoreValue = getHighScore();
    var highScore = new Text();
    highScore.getStyleClass().add("hiscore");
    highScore.setStyle("-fx-font-size: 20");
    highScore.setText("HiScore: " + highScoreValue);

    var infoPane = new StackPane();

    var hBox = new HBox(highScore, level, score, lives, multiplier);
    hBox.setSpacing(15);
    hBox.setAlignment(Pos.CENTER);

    var leaderboard = new Leaderboard();
    leaderboard.multiplayerListProperty().bind(game.playerStatsProperty());
    var chatBox = new ChatBox(communicator);

    infoPane.getChildren().add(leaderboard);
    infoPane.getChildren().add(chatBox);
    leaderboard.setPrefHeight(200);
    chatBox.setMaxHeight(200);
    chatBox.setPrefWidth(250);
    chatBox.setTranslateX(-20);
    chatBox.setTranslateY(150);

    mainPane.setRight(infoPane);
    mainPane.setTop(hBox);

    var timer = new Rectangle(600, 30);
    timer.setFill(Color.WHITE);

    mainPane.setBottom(timer);

    AtomicBoolean beaten = new AtomicBoolean(false);
    game.setGameLoopListener((delay -> {
      communicator.send("SCORE " + scoreProperty.get());
      communicator.send("LIVES " + livesProperty.get());
      String boardValues = "";
      for (int x = 0; x < game.getCols(); x++) {
        for (int y = 0; y < game.getRows(); y++) {
          boardValues = boardValues + board.getBlock(x, y).getValue() + " ";
        }
      }
      communicator.send("BOARD " + boardValues);
      if (scoreProperty.get() > highScoreValue) {
        highScore.setText("HiScore: " + scoreProperty.get());
        if (!beaten.get()) {
          beaten.set(true);
          Multimedia.playAudio("sounds/message.wav");
          highScore.setFill(Color.LIMEGREEN);
        }
      }
      SimpleDoubleProperty width = new SimpleDoubleProperty();
      SimpleIntegerProperty notRed = new SimpleIntegerProperty(255);
      Timeline timeline = new Timeline(
          new KeyFrame(Duration.ZERO, new KeyValue(width, 800), new KeyValue(notRed, 255)),
          new KeyFrame(Duration.millis((delay/100)*55), new KeyValue(notRed, 255)),
          new KeyFrame(Duration.millis((delay/100)*95), new KeyValue(notRed, 0)),
          new KeyFrame(Duration.millis(delay), new KeyValue(width, 0), new KeyValue(notRed, 0))
      );
      timeline.setAutoReverse(true);
      timeline.setCycleCount(1);
      AnimationTimer animationTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
          timer.setWidth(width.get());
          timer.setFill(Color.rgb(255, notRed.get(), notRed.get()));
        }
      };

      animationTimer.start();
      timeline.play();
      logger.info("Timer animation started");

      timeline.setOnFinished((event) -> {
        logger.info("Timer animation ended");
        animationTimer.stop();
      });
    }));

    //Handle block on gameboard grid being clicked
    board.setOnBlockClick(this::blockClicked);
    board.setOnBlockHover(this::changeAim);
    nextPieceBoard.setOnBlockClick(block -> rotatePiece(3));
    board.setOnRightClick(() -> rotatePiece(3));
    followingPieceBoard.setOnBlockClick(block -> swapPieces());

    game.setPieceRotatedListener((piece -> {
      nextPieceBoard.displayPiece(piece);
    }));

    game.getLevelProperty().addListener(((observable, oldValue, newValue) -> {
      if (newValue.intValue() > oldValue.intValue()) Multimedia.playAudio("sounds/level.wav");
    }));


    game.setOnGameLost(score1 -> startGameOver());

    aim = new GameBlockCoordinate(2, 2);

    Platform.runLater(() -> root.requestFocus());
  }

  @Override
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new MultiplayerGame(5, 5, communicator);
    super.game = game;
  }

  @Override
  public void cleanup() {
    game.cancelTimer();
    communicator.send("DIE");
  }
}
