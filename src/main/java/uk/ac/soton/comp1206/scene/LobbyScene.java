package uk.ac.soton.comp1206.scene;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.ChatBox;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The scene showing the multiplayer channels and lobby
 */
public class LobbyScene extends BaseScene {
  private final Logger logger = LogManager.getLogger(LobbyScene.class);
  private final Communicator communicator;
  private VBox channelsBox;
  private Timer timer;
  private String currentChannel;
  private BorderPane mainPane;
  private BorderPane selectedChannelPane;
  private VBox userBox;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    communicator = gameWindow.getCommunicator();
  }

  @Override
  public void initialise() {
    communicator.addListener(message -> {
      Platform.runLater(() -> {
        //Handles network protocols
        if (message.startsWith("CHANNELS ")) {
          var channels = message.substring(9).split("\n");
          Platform.runLater(() -> updateChannels(channels));
        } else if (message.startsWith("JOIN ")) {
          if (currentChannel == null) {
            var channel = message.substring(5);
            Platform.runLater(() -> joinChannel(channel));
          }
        } else if (message.startsWith("USERS ")) {
          var users = message.substring(6).split("\n");
          Platform.runLater(() -> updateUsers(users));
        } else if (message.equals("HOST")) {
          Platform.runLater(this::addStartButton);
        } else if (message.equals("PARTED")) {
          currentChannel = null;
          Platform.runLater(() -> {
            mainPane.setRight(null);
            selectedChannelPane = null;
          });
        } else if (message.equals("START")) {
          Platform.runLater(this::startGame);
        } else if (message.startsWith("ERROR ")) {
          var alert = new Alert(AlertType.ERROR, message.substring(6));
          alert.showAndWait();
        }
      });
    });
    timerLoop();
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    //Create base pane
    var lobbyPane = new StackPane();
    lobbyPane.setPrefSize(root.getPrefWidth(), root.getPrefHeight());
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("lobby-background");
    root.getChildren().add(lobbyPane);

    //Create main pane
    mainPane = new BorderPane();
    lobbyPane.getChildren().add(mainPane);

    //Create scrollPane and channel VBox
    var scroller = new ScrollPane();
    channelsBox = new VBox();
    updateChannels(new String[]{""});
    scroller.setContent(channelsBox);
    scroller.getStyleClass().add("channelScroll");
    scroller.setFitToHeight(true);
    mainPane.setCenter(scroller);

    //Add create channel button
    var createButton = new Button("Create Channel");
    createButton.getStyleClass().add("menuItem");
    createButton.setAlignment(Pos.CENTER);
    mainPane.setBottom(createButton);

    //Create button clicked handler
    createButton.setOnAction(event -> {
      //Create prompt box
      var prompt = new HBox();
      prompt.setSpacing(10);
      prompt.setAlignment(Pos.BOTTOM_RIGHT);

      //Create channel name text field
      var channelName = new TextField();
      channelName.setPromptText("Enter channel name");
      channelName.setPrefWidth(300);
      channelName.getStyleClass().add("TextField");

      //Create submit button
      var submitButton = new Button("Create");
      submitButton.getStyleClass().add("submit");
      prompt.getChildren().addAll(channelName, submitButton);

      lobbyPane.getChildren().add(prompt);
      prompt.setTranslateX(-100);
      prompt.setTranslateY(-20);

      //Handle submit button
      submitButton.setOnAction(event1 -> {
        communicator.send("CREATE " + channelName.getText());
        lobbyPane.getChildren().remove(prompt);
      });

      channelName.setOnKeyPressed(event1 -> {
        if (event1.getCode() != KeyCode.ENTER) return;
        communicator.send("CREATE " + channelName.getText());
        lobbyPane.getChildren().remove(prompt);
      });
    });
  }

  @Override
  public void cleanup() {
    timer.cancel();
  }

  private void updateChannels(String[] channels) {
    channelsBox.getChildren().clear();

    //No channels to be added
    if (channels[0].equals("")) {
      var label = new Text("There are currently no channels :(");
      label.getStyleClass().add("label");
      channelsBox.getChildren().add(label);
      return;
    }

    //Add channels to VBox
    for (var channel : channels) {
      var channelBox = new HBox();
      channelBox.setPrefWidth(channelsBox.getPrefWidth());
      var button = new Button(channel);
      channelBox.getChildren().addAll(button);
      HBox.setHgrow(button, Priority.ALWAYS);
      button.getStyleClass().add("channelItem");
      channelsBox.getChildren().add(channelBox);
      button.setOnAction(event -> communicator.send("JOIN " + channel));
    }
  }

  private void joinChannel(String channel) {
    currentChannel = channel;

    selectedChannelPane = new BorderPane();

    //Add name of channel
    var channelName = new Text(channel);
    channelName.getStyleClass().add("label");

    //Add user list
    userBox = new VBox();

    //Add leave button
    var leave = new Button("Leave");
    VBox channelInfo = new VBox(channelName, userBox, leave);
    leave.getStyleClass().add("channelButton");
    leave.setPrefWidth(250);

    //Add chatBox
    var chatBox = new ChatBox(communicator);
    chatBox.setPrefWidth(400);

    selectedChannelPane.setTop(channelInfo);
    selectedChannelPane.setCenter(chatBox);
    selectedChannelPane.setMaxWidth(250);

    leave.setOnAction(event -> {
      communicator.send("PART");
    });

    mainPane.setRight(selectedChannelPane);
  }

  private void updateUsers(String[] users) {
    userBox.getChildren().clear();
    for (var user : users) {
      var userText = new Text(user);
      userText.getStyleClass().add("playerName");
      userBox.getChildren().add(userText);
    }
  }

  private void addStartButton() {
    var start = new Button("Start Game");
    start.setPrefWidth(250);
    start.getStyleClass().add("channelButton");

    start.setOnAction(event -> {
      communicator.send("START");
    });
    selectedChannelPane.setBottom(start);
  }

  private void timerLoop() {
    communicator.send("LIST");
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        timerLoop();
      }
    }, 5000);
  }

  private void startGame() {
    gameWindow.startMultiChallenge();
  }
}