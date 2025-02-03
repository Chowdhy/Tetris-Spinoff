package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    private Button createButton(String text, VBox box) {
        //Create button
        Button button = new Button();
        button.setText(text);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.getStyleClass().add("menuItem");

        //Create button box
        var buttonBox = new HBox(button);
        buttonBox.setAlignment(Pos.CENTER);
        box.getChildren().add(buttonBox);

        return button;
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        //Create base pane
        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        //Create main pane
        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Add TetrECS logo
        var image = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm(), 500, 100, true, false);
        var logo = new ImageView(image);
        BorderPane.setAlignment(logo, Pos.CENTER);
        BorderPane.setMargin(logo, new Insets(100));
        mainPane.setTop(logo);

        //Add buttons container
        var buttonBox = new VBox();
        mainPane.setCenter(buttonBox);

        //Create menu buttons
        var playSoloButton = createButton("Single Player", buttonBox);
        var playMultiButton = createButton("Multi Player", buttonBox);
        var instructionsButton = createButton("Instructions", buttonBox);
        var exitButton = createButton("Exit", buttonBox);

        //Bind the button action to the startGame method in the menu
        playSoloButton.setOnAction(this::startGame);
        playMultiButton.setOnAction(this::startMultiplayer);
        instructionsButton.setOnAction(this::startInstructions);
        exitButton.setOnAction(event -> {
            gameWindow.getCommunicator().send("QUIT");
            App.getInstance().shutdown();
        });

        //Animate logo rotation
        logo.setRotate(-7);
        var rotate = new RotateTransition(Duration.millis(4000), logo);
        rotate.setByAngle(14);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setAutoReverse(true);
        rotate.play();

        //Animate logo fade in
        var fade = new FadeTransition(Duration.millis(1500), logo);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setCycleCount(1);
        fade.play();
    }

    @Override
    public void cleanup() {
        //No cleanup required
    }



    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        logger.info("Initialising " + this.getClass().getName());
        Multimedia.playMusic("music/menu.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    private void startInstructions(ActionEvent event) {
        gameWindow.startInstructions();
    }

    private void startMultiplayer(ActionEvent event) {
        gameWindow.startMultiplayer();
    }

}
