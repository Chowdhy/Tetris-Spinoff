package uk.ac.soton.comp1206.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.utility.Multimedia;

/**
 * Handles displaying, receiving, and sending chat messages to and from the server
 * Extends BorderPane
 */
public class ChatBox extends BorderPane {
  private static final Logger logger = LogManager.getLogger(ChatBox.class);
  private final Communicator communicator;
  private final TextFlow messages;
  private final ScrollPane pane;

  /**
   * Constructs the UI components of the ChatBox
   * @param communicator The game window's communicator
   */
  public ChatBox(Communicator communicator) {
    this.communicator = communicator;

    //Scroller and message container
    pane = new ScrollPane();
    messages = new TextFlow(new Text("Welcome to the channel.\n"));
    pane.setContent(messages);
    pane.setFitToWidth(true);
    pane.setFitToHeight(true);
    pane.getStyleClass().add("scroller");
    messages.getStyleClass().add("messages");

    //Chat box for messages
    var chatField = new TextField("");
    chatField.getStyleClass().add("TextField");
    chatField.setAlignment(Pos.CENTER_LEFT);

    //Send button
    var sendButton = new Button("Send");
    var chatBox = new HBox(chatField, sendButton);
    HBox.setHgrow(chatField, Priority.ALWAYS);
    this.setCenter(pane);
    this.setBottom(chatBox);

    //Listen for messages or nickname changed
    communicator.addListener(message -> {
      if (message.startsWith("MSG ")) {
        var messageArray = message.substring(4).split(":");
        Platform.runLater(() -> receiveMessage(messageArray));
      } else if (message.startsWith("NICK ")) {
        receiveMessage(new String[]{"SYSTEM", "Your nickname has been changed."});
      }
    });

    //Send messages
    sendButton.setOnAction(event -> {
      if (chatField.getText().strip().equals("")) return;
      sendMessage(chatField.getText());
      chatField.clear();
    });

    chatField.setOnKeyPressed(event -> {
      if (event.getCode() != KeyCode.ENTER || chatField.getText().strip().equals("")) return;
      sendMessage(chatField.getText());
      chatField.clear();
    });
  }

  private void sendMessage(String message) {
    if (message.toLowerCase().startsWith("/nick ")) {
      communicator.send("NICK " + message.substring(6));
      return;
    }
    communicator.send("MSG " + message);
    pane.requestFocus();
  }
  private void receiveMessage(String[] message) {
    Multimedia.playAudio("sounds/message.wav");
    var text = new Text(message[0] + ": " + message[1] + "\n");
    messages.getChildren().add(text);
    pane.setVvalue(1.0);
  }
}
