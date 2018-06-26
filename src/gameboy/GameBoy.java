/*
 * (c) 2016 - Marek Kost and Lukas Bandura
 */
package gameboy;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main class of GameBoy project.
 * Creates GameWindow and throws onUpdate() calls from within game Thread.
 * @author Marek Kost
 */
public class GameBoy extends Application {
    static GameBoy gameBoy;     // this class instance
    GameWindow gameWindow;      // game window
    double xOffset, yOffset;    // used for dragging the window
    
    static Thread thread;       // game Thread
    int currentTimer = 200;     // speed of the game, number of milliseconds between ticks
    int frame = 0;              // current frame
    int ticksLeft = 100;        // ticks left to make difficulty harder
    int score = 0;              // current score
    
    static List<UpdateEventListener> listeners;     // update listeners
    static boolean end = false;                     // signals if game has ended
    boolean pause = false;                          // signal if game is paused

    @Override
    public void start(Stage primaryStage) {
        gameBoy = this;                     // assign reference to this instance of GameBoy
        listeners = new ArrayList<>();      // initialize ArrayList
        
        // create background
        ImageView background = new ImageView();
        background.setImage(new Image("gejmboj.png"));
        
        // create game window
        gameWindow = new GameWindow();
        addUpdateEventListener(gameWindow);
        gameWindow.setTranslateX(7);
        gameWindow.setTranslateY(-135);
        
        // add components to root object
        StackPane root = new StackPane();
        root.getChildren().add(background);     // add background
        root.getChildren().add(gameWindow);     // add game window
        
        // create scene
        Scene scene = new Scene(root, 480, 640);
        scene.setFill(null);
       
        // exit button
        ImageView exitButton = new ImageView();
        exitButton.setOnMousePressed(new EventHandler<MouseEvent>() {
            // quit the game
            @Override
            public void handle(MouseEvent event) {
                thread.stop();
                primaryStage.close();
            }
        });
        exitButton.setTranslateX(165);
        exitButton.setTranslateY(-280);
        exitButton.setImage(new Image("closeButton.png"));
        root.getChildren().add(exitButton);
        
        // button A
        ImageView buttonA = new ImageView();
        buttonA.setTranslateX(80);
        buttonA.setTranslateY(120);
        buttonA.setImage(new Image("batn.png"));
        buttonA.setOnMousePressed(new EventHandler<MouseEvent>() {
            // change background
            @Override
            public void handle(MouseEvent event) {
                gameWindow.changeBackground();
            }
        });
        root.getChildren().add(buttonA);
        
        // button B
        ImageView buttonB = new ImageView();
        buttonB.setTranslateX(145);
        buttonB.setTranslateY(90);
        buttonB.setImage(new Image("batn.png"));
        buttonB.setOnMousePressed(new EventHandler<MouseEvent>() {
            // change player image
            @Override
            public void handle(MouseEvent event) {
                gameWindow.changePlayerColor();
            }
        });
        root.getChildren().add(buttonB);
        
        // button PAUSE
        ImageView buttonSelect = new ImageView();
        buttonSelect.setTranslateX(-45);
        buttonSelect.setTranslateY(200);
        buttonSelect.setImage(new Image("batn.png"));
        buttonSelect.setOnMousePressed(new EventHandler<MouseEvent>() {
            // pause the game
            @Override
            public void handle(MouseEvent event) {
                gameBoy.pause(!pause);
            }
        });
        root.getChildren().add(buttonSelect);
        
        // button RESTART
        ImageView buttonStart = new ImageView();
        buttonStart.setTranslateX(20);
        buttonStart.setTranslateY(200);
        buttonStart.setImage(new Image("batn.png"));
        buttonStart.setOnMousePressed(new EventHandler<MouseEvent>() {
            // restart the game
            @Override
            public void handle(MouseEvent event) {
                restart();
            }
        });
        root.getChildren().add(buttonStart);
        
        // button RIGHT
        ImageView buttonRight = new ImageView();
        buttonRight.setTranslateX(-75);
        buttonRight.setTranslateY(112);
        buttonRight.setImage(new Image("batn.png"));
        buttonRight.setOnMousePressed(new EventHandler<MouseEvent>() {
            // move player to the right
            @Override
            public void handle(MouseEvent event) {
                try {
                    gameWindow.movePlayer(Settings.MOVE_RIGHT);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(GameBoy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        root.getChildren().add(buttonRight);
        
        // button LEFT
        ImageView buttonLeft = new ImageView();
        buttonLeft.setTranslateX(-135);
        buttonLeft.setTranslateY(112);
        buttonLeft.setImage(new Image("batn.png"));
        buttonLeft.setOnMousePressed(new EventHandler<MouseEvent>() {
            // move player to the left
            @Override
            public void handle(MouseEvent event) {
                try {
                    gameWindow.movePlayer(Settings.MOVE_LEFT);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(GameBoy.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        root.getChildren().add(buttonLeft);
        
        
        // register key event handler
        final EventHandler<KeyEvent> keyEventHandler =
        new EventHandler<KeyEvent>() {
            public void handle(final KeyEvent keyEvent) {
                // if escape was pressed, quit the game
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    thread.stop();              // stop thread
                    primaryStage.close();       // close application
                    keyEvent.consume();
                }
                // if R is pressed, restart the game
                else if (keyEvent.getCode() == KeyCode.R) {
                    restart();
                    keyEvent.consume();
                }
                // if LEFT or A is pressed, move player to the left
                else if (keyEvent.getCode() == KeyCode.A || keyEvent.getCode() == KeyCode.LEFT) {
                    if(end || pause) return;
                    try {
                        gameWindow.movePlayer(Settings.MOVE_LEFT);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(GameBoy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    keyEvent.consume();
                }
                // if RIGHT or D is pressed, move player to the right
                else if (keyEvent.getCode() == KeyCode.D || keyEvent.getCode() == KeyCode.RIGHT) { 
                    if(end || pause) return;
                    try {
                        gameWindow.movePlayer(Settings.MOVE_RIGHT);
                    } catch (MalformedURLException ex) {
                        Logger.getLogger(GameBoy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    keyEvent.consume();
                }
                // if P was pressed, pause the game
                else if (keyEvent.getCode() == KeyCode.P) { 
                    pause(!pause);
                    keyEvent.consume();
                }
            }
        };
        scene.setOnKeyPressed(keyEventHandler);
       
        // create game thread
        thread = new Thread(){
            public void run(){
              while(true) {
                  try {
                      Thread.sleep(5);                  // run this 200 times per second for smooth gameplay
                      frame+=5;                         // add time between frames to the frame value
                      if(frame >= currentTimer) {       // if frame value equals or is higher than current timer which determines difficulty of the game
                        Tick();                         // do Tick()
                        frame = 0;                      // reset frame
                      }
                  } catch (InterruptedException ex) {
                      Logger.getLogger(GameBoy.class.getName()).log(Level.SEVERE, null, ex);
                  }
              }
            }
            
            /**
             * Throws onUpdate event and updates score.
             */
            void Tick() {
                if(gameWindow.isRunning() && !end && !pause) {
                    score++;                            // score is equal to number of ticks, so increment it
                    gameWindow.updateScore(score);      // show new score

                    // throw onUpdate events
                    for(int i=0; i<listeners.size();i++) {
                        listeners.get(i).onUpdate();
                    }
                    
                    // this part of code makes difficulty harder every 100 ticks
                    ticksLeft--;
                    if(ticksLeft == 0) {
                        if(currentTimer > 75)
                            currentTimer -= 25;
                        ticksLeft = 100;
                    }
                }
            }
        };
        thread.start();
        
        // create window
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("Gaemboi");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // make window draggable (only works on bottom ~150 pixels)
        background.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = primaryStage.getX() - event.getScreenX();
                yOffset = primaryStage.getY() - event.getScreenY();
            }
        });

        background.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage.setX(event.getScreenX() + xOffset);
                primaryStage.setY(event.getScreenY() + yOffset);
            }
        });
    }

    /**
     * Adds update event listener.
     * @param ueh 
     */
    public static void addUpdateEventListener(UpdateEventListener uel) {
        listeners.add(uel);
    }
    
    /**
     * Removes update event listener.
     * @param ueh 
     */
    public static void removeUpdateEventListener(UpdateEventListener uel) {
        listeners.remove(uel);
    }
    
    /**
     * Method used to end the game. Makes thread not fire onUpdate events.
     */
    public static void endGame() {
        end = true;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Method used to pause or resume the game.
     * @param pause 
     */
    void pause(boolean pause) {
        if(gameWindow.isRunning()) {
            this.pause = pause;
            gameWindow.pause(pause);
        }
    }
    
    /**
     * Method called to restart the game.
     */
    void restart() {
        pause(false);               // unpause the game in case it was paused
        gameWindow.clearGrid();     // clear the grid
        currentTimer = 250;         // reset difficulty
        score = 0;                  // reset score
        gameWindow.resetGame();     // reset game window
        end = false;                // start the game again
    }
}
