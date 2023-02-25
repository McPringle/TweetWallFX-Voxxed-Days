/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2023 TweetWallFX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.tweetwallfx.vdz;

import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tweetwallfx.config.Configuration;
import org.tweetwallfx.config.TweetwallSettings;
import org.tweetwallfx.mqtt.MqttEvent;
import org.tweetwallfx.mqtt.MqttProcess;
import org.tweetwallfx.tweet.api.Tweeter;
import org.tweetwallfx.twod.TagTweets;

import static org.tweetwallfx.mqtt.MqttEvent.RESTART;
import static org.tweetwallfx.mqtt.MqttEvent.STOP;

public class Main extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    final MqttProcess mqttProcess = new MqttProcess();

    @Override
    public void start(Stage primaryStage) {
        new Thread(mqttProcess).start();
        mqttProcess.addMqttEventHandler(e ->  {
            if (STOP.equals(e.getEventType())) {
                Platform.exit();
            } else if (RESTART.equals(e.getEventType())) {
                LOG.warn("Ignoring restart command");
            }
        });

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 1920, 1080);
        borderPane.getStyleClass().add("splash");

        final TweetwallSettings tweetwallSettings
                = Configuration.getInstance().getConfigTyped(TweetwallSettings.CONFIG_KEY, TweetwallSettings.class);

        Optional.ofNullable(tweetwallSettings.stylesheetResource())
                .map(ClassLoader.getSystemClassLoader()::getResource)
                .map(java.net.URL::toExternalForm)
                .ifPresent(scene.getStylesheets()::add);
        Optional.ofNullable(tweetwallSettings.stylesheetFile())
                .ifPresent(scene.getStylesheets()::add);

        final StringPropertyAppender spa = new StringPropertyAppender();
        spa.start();

        HBox statusLineHost = new HBox();
        Text statusLineText = new Text();
        statusLineText.getStyleClass().addAll("statusline");
        statusLineText.textProperty().bind(spa.stringProperty());
        statusLineHost.getChildren().add(statusLineText);

        final TagTweets tweetsTask = new TagTweets(borderPane);
        Platform.runLater(tweetsTask::start);

        scene.setOnKeyTyped((KeyEvent event) -> {
            if (event.isMetaDown()) {
                switch (event.getCharacter()) {
                    case "d" -> toggleStatusLine(borderPane, spa, statusLineHost);
                    case "f" -> primaryStage.setFullScreen(!primaryStage.isFullScreen());
                    case "x" -> Platform.exit();
                    default -> {}
                };
            }
        });

        primaryStage.setTitle(tweetwallSettings.title());
        primaryStage.setScene(scene);

        primaryStage.show();
        primaryStage.setFullScreen(!Boolean.getBoolean("org.tweetwallfx.disable-full-screen"));
    }

    private static void toggleStatusLine(BorderPane borderPane, StringPropertyAppender spa, HBox statusLineHost) {
        LoggerConfig rootLogger = LoggerContext.getContext().getConfiguration().getRootLogger();
        if (null == statusLineHost.getParent()) {
            borderPane.setBottom(statusLineHost);
            rootLogger.addAppender(spa, Level.TRACE, null);
        } else {
            borderPane.getChildren().remove(statusLineHost);
            rootLogger.removeAppender(spa.getName());
        }
    }

    @Override
    public void stop() {
        LOG.info("closing...");
        Tweeter.getInstance().shutdown();
        mqttProcess.stop();
        System.exit(0);
    }

    /**
     * Starts the Tweetwall from command line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
