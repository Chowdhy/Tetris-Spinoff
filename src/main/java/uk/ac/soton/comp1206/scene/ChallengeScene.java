package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    /**
     * Logs the information requested
     */
    protected static final Logger logger = LogManager.getLogger(ChallengeScene.class);
    /**
     * The programmatic representation of the game
     */
    protected Game game;
    /**
     * The board displaying the next piece
     */
    protected PieceBoard nextPieceBoard;
    /**
     * The current block being hovered
     */
    protected GameBlockCoordinate aim;

    /**
     * The board displaying the game
     */
    protected GameBoard board;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        Multimedia.playMusic("music/game_start.wav", "music/game.wav");
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //Creates base pane
        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        //Creates main pane
        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        //Creates next piece board
        nextPieceBoard = (new PieceBoard(150, 150));
        challengePane.getChildren().add(nextPieceBoard);
        nextPieceBoard.setTranslateX(200);
        nextPieceBoard.setTranslateY(-30);

        //Creates following piece board
        var followingPieceBoard = new PieceBoard(100, 100);
        challengePane.getChildren().add(followingPieceBoard);
        followingPieceBoard.setTranslateX(330);
        followingPieceBoard.setTranslateY(105);

        game.setNextPieceListener((nextPiece, followingPiece) -> {
            nextPieceBoard.displayPiece(nextPiece);
            followingPieceBoard.displayPiece(followingPiece);
        });

        game.setLineClearListener((linesCleared, coordinates) -> {
            Multimedia.playAudio("sounds/clear.wav");
            board.fadeOut(coordinates);
        });

        //Create score UI component
        var scoreProperty = game.getScoreProperty();
        var score = new Text();
        score.getStyleClass().add("score");
        score.textProperty().bind(Bindings.concat("Score: ", scoreProperty));

        //Create level UI component
        var levelProperty = game.getLevelProperty();
        var level = new Text();
        level.getStyleClass().add("level");
        level.textProperty().bind(Bindings.concat("Level: ", levelProperty));

        //Create lives UI component
        var livesProperty = game.getLivesProperty();
        var lives = new Text();
        lives.getStyleClass().add("lives");
        lives.textProperty().bind(Bindings.concat("Lives: ", livesProperty));

        //Create multiplier UI component
        var multiplierProperty = game.getMultiplierProperty();
        var multiplier = new Text();
        multiplier.getStyleClass().add("lives");
        multiplier.textProperty().bind(Bindings.concat("Multiplier: x", multiplierProperty));

        //Create high score UI component
        var highScoreValue = getHighScore();
        var highScore = new Text();
        highScore.getStyleClass().add("hiscore");
        highScore.setText("HiScore: " + highScoreValue);

        var vBox = new VBox(highScore, level, score, lives, multiplier);
        vBox.setTranslateX(-40);
        vBox.setTranslateY(20);

        mainPane.setRight(vBox);

        //Creates timer rectangle
        var timer = new Rectangle(600, 30);
        timer.setFill(Color.WHITE);

        mainPane.setBottom(timer);

        AtomicBoolean beaten = new AtomicBoolean(false);
        game.setGameLoopListener((delay -> {
            //Handle beating the high score
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

            //Keyframes for timer scale and colour
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
    }

    @Override
    public void cleanup() {
        game.cancelTimer();
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock.getX(), gameBlock.getY());
    }

    /**
     * Handles when a block is clicked when only the coordinates are known
     * @param x The x coordinate of the block
     * @param y The y coordinate of the block
     */
    protected void blockClicked(int x, int y) {
        game.blockClicked(x, y);
    }

    /**
     * Handles rotating a piece
     * @param times The number of times for the piece to be rotated
     */
    protected void rotatePiece(int times) {
        Multimedia.playAudio("sounds/rotate.wav");
        game.rotateCurrentPiece(times);
    }

    /**
     * Handles a piece swap request
     */
    protected void swapPieces() {
        Multimedia.playAudio("sounds/transition.wav");
        game.swapCurrentPiece();
    }

    /**
     * Changes the aim to the new block passed
     * @param block The new block being aimed at
     */
    protected void changeAim(GameBlock block) {
        board.getBlock(aim.getX(), aim.getY()).unhover();
        block.hover();
        aim = new GameBlockCoordinate(block.getX(), block.getY());
        logger.info("Aim Updated: New aim x = " + aim.getX() + ", y = " + aim.getY());
    }

    /**
     * Updates the current aim based on the passed x and y values
     * @param x The increment for the x coordinate
     * @param y The increment for the y coordinate
     */
    protected void updateAim(int x, int y) {
        board.getBlock(aim.getX(), aim.getY()).unhover();
        aim = aim.add(x, y);
        board.getBlock(aim.getX(), aim.getY()).hover();
        logger.info("Aim Updated: New aim x = " + aim.getX() + ", y = " + aim.getY());
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising " + this.getClass().getName());
        game.start();

        gameWindow.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> logger.info(event));

        //Handle key presses
        gameWindow.getScene().setOnKeyPressed((event -> {
            if (event.getTarget() instanceof TextField) return;
            switch (event.getCode()) {
                case ENTER, X:
                    blockClicked(aim.getX(), aim.getY());
                    break;
                case RIGHT, D:
                    if (aim.getX() < game.getCols() - 1)
                        updateAim(1, 0);
                    break;
                case LEFT, A:
                    if (aim.getX() > 0) {
                        updateAim(-1, 0); }
                    break;
                case DOWN, S:
                    if (aim.getY() < game.getRows() - 1)
                        updateAim(0, 1);
                    break;
                case UP, W:
                    if (aim.getY() > 0)
                        updateAim(0, -1);
                    break;
                case SPACE, R:
                    swapPieces();
                    break;
                case OPEN_BRACKET, Q, Z:
                    rotatePiece(1);
                    break;
                case CLOSE_BRACKET, E, C:
                    rotatePiece(3);
                    break;
                case ESCAPE:
                    gameWindow.startMenu();
                    break;
            }
        }));
    }

    /**
     * Reads the high scores list to find the current local high score
     * @return The current local high score
     */
    protected int getHighScore() {
      File file = new File("highScores.txt");
      if (!file.exists()) return 0;
      FileReader fileReader;
      BufferedReader reader;

      //Read high score
      try {
        fileReader = new FileReader(file);
        reader = new BufferedReader(fileReader);
        String line = reader.readLine();
        return Integer.parseInt(line.substring(line.indexOf(":")+1));
      } catch (IOException exception) {
        return 0;
      }
    }

    /**
     * Changes to the game over screen.
     */
    protected void startGameOver() {
        gameWindow.startChallengeLost(game);
    }
}
