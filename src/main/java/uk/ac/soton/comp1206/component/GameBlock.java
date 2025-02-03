package uk.ac.soton.comp1206.component;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.BLACK,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;
    private Paint currentColour;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        var gc = getGraphicsContext2D();

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {

        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.BLACK);
        gc.setGlobalAlpha(0.3);
        gc.fillRect(0,0, width, height);

        currentColour = Color.BLACK;

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.setGlobalAlpha(0.85);
        gc.fillRect(0,0, width, height);

        currentColour = colour;

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);

        gc.save();

        // Adds triangle effect
        gc.fillPolygon(new double[]{
            0, 0, width
        }, new double[]{
            0, height, height
        }, 3);
        gc.setFill(Color.WHITE);
        gc.setGlobalAlpha(0.05);
    }

    /**
     * Handles a block hover
     */
    public void hover() {
        var gc = getGraphicsContext2D();

        //Colour fill
        if (currentColour == Color.BLACK) {
            gc.clearRect(0, 0, width, height);
        }

        gc.setFill(Color.WHITE);

        gc.setGlobalAlpha(0.3);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);

        showAim();
    }

    /**
     * Handles drawing the block once it is not being hovered
     */
    public void unhover() {
        if (currentColour != Color.BLACK) {
            paintColor(currentColour);
        } else {
            paintEmpty();
        }
    }

    /**
     * Handles drawing the canvas to show that the current block is aimed on
     */
    public void showAim() {
        var gc = getGraphicsContext2D();

        // Saves current state of block
        gc.save();

        // Fills block with fade effect
        gc.setGlobalAlpha(1);
        gc.setFill(Color.rgb(75, 75, 75));
        var radius = 12;
        gc.fillOval((getWidth()/2)-radius, (getHeight()/2)-radius, 2*radius, 2*radius);

        gc.strokeOval((getWidth()/2)-radius, (getHeight()/2)-radius, 2*radius, 2*radius);
        gc.setStroke(Color.BLACK);
    }

    /**
     * Handles a fade out transition on the block
     */
    public void fadeOut() {
        SimpleDoubleProperty opacity = new SimpleDoubleProperty();

        //Creates animation timer and timeline
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(0), new KeyValue(opacity, 1)),
            new KeyFrame(Duration.seconds(0.5), new KeyValue(opacity, 0))
        );
        timeline.setAutoReverse(true);
        timeline.setCycleCount(1);
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                var gc = getGraphicsContext2D();

                // Clears rectangle
                gc.clearRect(0, 0, width, height);

                // Adds original square colour
                unhover();

                // Adds transparent white block on top
                gc.setFill(Color.WHITE);
                gc.setGlobalAlpha(opacity.get());
                gc.fillRect(0, 0, width, height);
            }
        };

        timer.start();
        timeline.play();

        timeline.setOnFinished((event) -> {
            timer.stop();

            //Revert to original state
            unhover();
        });
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
