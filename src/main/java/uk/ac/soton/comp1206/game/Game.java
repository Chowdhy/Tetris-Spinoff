package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.GameLostListener;
import uk.ac.soton.comp1206.event.LineClearListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.PieceRotatedListener;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {
    /**
     *  Logs information pertaining to the status
     */
    protected static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;
    /**
     * Listens for next piece
     */
    protected NextPieceListener nextPieceListener;
    /**
     * Listens for line clears
     */
    protected LineClearListener lineClearListener;
    /**
     * Listens for rotated piece
     */
    protected PieceRotatedListener pieceRotatedListener;
    /**
     * Listens for looped game
     */
    protected GameLoopListener gameLoopListener;
    /**
     * Listens for lost game
     */
    protected GameLostListener gameLostListener;
    /**
     * The current piece being played
     */
    protected GamePiece currentPiece;
    /**
     * The following piece to be played
     */
    protected GamePiece followingPiece;
    /**
     * The score property
     */
    protected final SimpleIntegerProperty score = new SimpleIntegerProperty();
    /**
     * The level property
     */
    protected final SimpleIntegerProperty level = new SimpleIntegerProperty();
    /**
     * The lives property
     */
    protected final SimpleIntegerProperty lives = new SimpleIntegerProperty();
    /**
     * The multiplier property
     */
    protected final SimpleIntegerProperty multiplier = new SimpleIntegerProperty();
    /**
     * The countdown timer
     */
    protected Timer timer;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        timer = new Timer();
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        score.set(0);
        level.set(0);
        lives.set(3);
        multiplier.set(1);

        //Generate first pieces
        followingPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param x The x coordinate of the clicked block
     * @param y The y coorinate of the clicked block
     */
    public void blockClicked(int x, int y) {
        //Get the position of this block

        //Check if the piece can be played from the centre
        if (grid.canPlayPiece(currentPiece, x, y)) {
            //Play the piece
            grid.playPiece(currentPiece, x, y);
            Multimedia.playAudio("sounds/place.wav");
            timer.cancel();
            timer = new Timer();
            afterPiece();
        } else {
            // Plays fail sound
            Multimedia.playAudio("sounds/fail.wav");
        }
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Creates and returns a new piece
     * @return The new piece created
     */
    protected GamePiece spawnPiece() {
        int randomNumber = new Random().nextInt( 15);
        logger.info("Spawning new piece: " + randomNumber);
        return GamePiece.createPiece(randomNumber);
    }

    /**
     * Generates a new piece and starts the timer
     */
    protected void nextPiece() {
        logger.info("Switching pieces");

        //Spawns new following piece
        currentPiece = followingPiece;
        followingPiece = spawnPiece();

        //Runs gameloop when countdown finishes
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.info("Timer finished");
                gameLoop();
            }
        }, getTimerDelay());

        //Fires listeners to let interface know that timer has started
        Platform.runLater(() -> {
            gameLoopListener.timerStarted(getTimerDelay());
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        });
    }

    /**
     * Checks if there are lines to be cleared after playing a piece and clears them
     */
    protected void afterPiece() {
        logger.info("Checking for lines to be cleared");
        int lines = 0;
        HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();

        //Checks for columns to clear
        for (int x = 0; x < cols; x++) {
            logger.info("Checking column: x = " + x);
            var columnBlocks = new HashSet<GameBlockCoordinate>();
            for (int y = 0; y < rows; y++) {
                var block = new GameBlockCoordinate(x, y);
                columnBlocks.add(block);
                if (grid.get(x, y) == 0) break;
                else if (y == rows - 1) {
                    blocksToClear.addAll(columnBlocks);
                    lines++;
                }
            }
        }

        //Checks for rows to clear
        for (int y = 0; y < rows; y++) {
            logger.info("Checking row: y = " + y);
            var rowBlocks = new HashSet<GameBlockCoordinate>();
            for (int x = 0; x < cols; x++) {
                var block = new GameBlockCoordinate(x, y);
                rowBlocks.add(block);
                if (grid.get(x, y) == 0) break;
                else if (x == cols - 1) {
                    blocksToClear.addAll(rowBlocks);
                    lines++;
                }
            }
        }

        //Clears blocks
        for (GameBlockCoordinate block : blocksToClear) {
            logger.info("Clearing x " + block.getX() + " y " + block.getY());
            grid.set(block.getX(), block.getY(), 0);
        }
        score(lines, blocksToClear);
        logger.info(lines + " line(s) cleared, " + blocksToClear.size() + " blocks cleared");
    }

    /**
     * Calculates the score based on the lines and blocks cleared
     * @param lines The number of lines cleared
     * @param blocks The coordinates of the blocks to be cleared
     */
    public void score(int lines, Set<GameBlockCoordinate> blocks) {
        logger.info("Calculating score");

        //Calculates score
        int noBlocks = blocks.size();
        int scoreIncrease = lines * noBlocks * 10 * multiplier.get();
        score.set(score.get() + scoreIncrease);

        //Increases or resets level
        if (lines >= 1) {
            multiplier.set(multiplier.get() + 1);
            lineClearListener.clearLine(lines, blocks);
        } else {
            multiplier.set(1);
        }
        int oldLevel = level.get();
        int newLevel = Math.floorDiv(score.get(), 1000);
        if (newLevel > oldLevel) level.set(newLevel);

        logger.info("Player gained " + scoreIncrease + " points");

        nextPiece();
    }

    /**
     * Sets what happens when a piece is generated
     * @param listener The next piece listener
     */
    public void setNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }

    /**
     * Sets what happens when a line is cleared
     * @param listener The line clear listener
     */
    public void setLineClearListener(LineClearListener listener) {
        this.lineClearListener = listener;
    }

    /**
     * Sets what happens when a piece is rotated
     * @param listener The rotate piece listener
     */
    public void setPieceRotatedListener(PieceRotatedListener listener) {
        this.pieceRotatedListener = listener;
    }

    /**
     * Sets what happens when the game loops
     * @param listener The game loop listener
     */
    public void setGameLoopListener(GameLoopListener listener) {
        this.gameLoopListener = listener;
    }

    /**
     * Sets what happens when the game is lost
     * @param listener The game lost listener
     */
    public void setOnGameLost(GameLostListener listener) {
        this.gameLostListener = listener;
    }

    /**
     * Returns the score property
     * @return The score property
     */
    public SimpleIntegerProperty getScoreProperty() {
        return score;
    }

    /**
     * Returns the level property
     * @return The level property
     */
    public SimpleIntegerProperty getLevelProperty() {
        return level;
    }

    /**
     * Returns the lives property
     * @return The lives property
     */
    public SimpleIntegerProperty getLivesProperty() {
        return lives;
    }

    /**
     * Returns the multiplier property
     * @return The multiplier property
     */
    public SimpleIntegerProperty getMultiplierProperty() {
        return multiplier;
    }

    /**
     * Handles rotating a piece a number of times
     * @param times The number of times for the piece to be rotated
     */
    public void rotateCurrentPiece(int times) {
        logger.info("Rotating Current Piece " + times + " times");
        currentPiece.rotate(times);

        //Let interface know
        Platform.runLater(() -> {
            pieceRotatedListener.pieceRotated(currentPiece);
        });
    }

    /**
     * Swaps the current and following pieces
     */
    public void swapCurrentPiece() {
        logger.info("Swapping Current Piece and Following Piece");
        var temp = followingPiece;
        followingPiece = currentPiece;
        currentPiece = temp;
        Platform.runLater(() -> {
            nextPieceListener.nextPiece(currentPiece, followingPiece);
        });
    }

    /**
     * Calculates and returns the delay based on the current level
     * @return The delay for the timer
     */
    public int getTimerDelay() {
        int timerDelay = Math.max(2500, 12000 - (level.get() * 500));
        logger.info("Calculated timer delay: " + timerDelay + "ms");
        return timerDelay;
    }

    /**
     * Handles what happens when a timer completes
     */
    protected void gameLoop() {
        if (lives.get() > 0) {
            lives.set(lives.get() - 1);
            multiplier.set(1);
            nextPiece();
            logger.info("Player lost a life");
            Multimedia.playAudio("sounds/lifelose.wav");
        } else {
            Multimedia.playAudio("sounds/explode.wav");
            logger.info("Player lost the game");
            Platform.runLater(() -> {
                gameLostListener.gameLost(score.get());
            });
        }
    }

    /**
     * Cancels the timer
     */
    public void cancelTimer() {
        timer.cancel();
    }
}
