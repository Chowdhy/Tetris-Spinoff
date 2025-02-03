package uk.ac.soton.comp1206.utility;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Multimedia {
  private static final Logger logger = LogManager.getLogger(Multimedia.class);
  private static MediaPlayer musicPlayer;
  private static MediaPlayer audioPlayer;

  private static boolean musicEnabled = true;
  private static boolean audioEnabled = true;

  public static void playMusic(String file) {
    if (!musicEnabled) return;
    if (musicPlayer != null) musicPlayer.stop();
    String toPlay = Multimedia.class.getResource("/" + file).toExternalForm();
    try {
      Media music = new Media(toPlay);
      musicPlayer = new MediaPlayer(music);
      musicPlayer.play();

      musicPlayer.setOnEndOfMedia(new Thread(() -> {
        musicPlayer.seek(Duration.ZERO);
        musicPlayer.play();
      }));
    } catch (Exception e) {
      musicEnabled = false;
      e.printStackTrace();
      logger.info("Music couldn't be played, disabling music");
    }
  }

  public static void playAudio(String file) {
    if (!audioEnabled) return;
    String toPlay = Multimedia.class.getResource("/" + file).toExternalForm();
    try {
      Media audio = new Media(toPlay);
      audioPlayer = new MediaPlayer(audio);
      audioPlayer.play();
    } catch (Exception e) {
      audioEnabled = false;
      e.printStackTrace();
      logger.info("Audio couldn't be played, disabling audio");
    }
  }

  public static void playMusic(String file1, String file2) {
    if (!musicEnabled) return;
    if (musicPlayer != null) musicPlayer.stop();
    String toPlay = Multimedia.class.getResource("/" + file1).toExternalForm();
    try {
      Media music = new Media(toPlay);
      musicPlayer = new MediaPlayer(music);
      musicPlayer.play();

      musicPlayer.setOnEndOfMedia(new Thread(() -> playMusic(file2)));
    } catch (Exception e) {
      musicEnabled = false;
      e.printStackTrace();
      logger.info("Music couldn't be played, disabling music");
    }
  }
}
