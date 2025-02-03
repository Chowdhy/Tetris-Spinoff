package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The scene containing the instructions and an example of all the pieces
 * Extends BaseScene
 */
public class InstructionsScene extends BaseScene {
  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Constructor for the instructions scene
   * @param gameWindow The current game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instructions Scene");
  }

  @Override
  public void initialise() {
    logger.info("Initialising " + this.getClass().getName());
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //Create base pane
    var instructionsPane = new StackPane();
    instructionsPane.setMaxWidth(gameWindow.getWidth());
    instructionsPane.setMaxHeight(gameWindow.getHeight());
    instructionsPane.getStyleClass().add("instructions-background");
    root.getChildren().add(instructionsPane);

    //Create main pane
    var mainPane = new BorderPane();
    instructionsPane.getChildren().add(mainPane);

    //Create instructions image
    var image = new Image(InstructionsScene.class.getResource("/images/Instructions.png").toExternalForm(), 676, 676, true, true);
    var instructions = new ImageView(image);
    BorderPane.setAlignment(instructions, Pos.CENTER);
    mainPane.setCenter(instructions);

    //Create example piece boards
    var examplePieces = new TilePane();
    examplePieces.setAlignment(Pos.CENTER);
    for (GamePiece piece : GamePiece.getPieces()) {
      PieceBoard board = new PieceBoard(75, 75);
      board.displayPiece(piece);
      board.setPadding(new Insets(5));
      examplePieces.getChildren().add(board);
    }

    mainPane.setBottom(examplePieces);
  }

  @Override
  public void cleanup() {
    //Nothing requires cleanup
  }
}
