/*
 * (c) 2016 - Marek Kost and Lukas Bandura
 */
package gameboy;

import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayerBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 * Class used to display game window.
 * @author Marek Kost
 */
public class GameWindow extends StackPane implements UpdateEventListener {
    int width = 10;         // number of collumns
    int height = 13;        // number of rows
    
    List<Node> points;      // all the points to paint
    
    Player p = new Player();        // player
    List<Bot> bots;                 // bots
    
    Text score = new Text();        // score label
    
    Rectangle background;               // background image
    List<Color> backgroundColors;       // background colors
    
    Image currentPlayerImage;       // image used to paint player with
    List<Image> playerImages;       // list of all available images
    
    ImageView gameOver;             // game over screen
    ImageView startGame;            // start game screen
    ImageView pauseGame;            // pause screen
    
    MediaPlayerBuilder builder;     // media builder
    MediaPlayer player;             // media player that will play audio
    boolean isPlaying = false;      // signals if sound is playing
    
    boolean isRunning = false;      // true if game should be running
    
    public GameWindow() {
        // initialize lists
        points = new ArrayList<>();
        bots = new ArrayList<>();
        
        // create background
        background = new Rectangle();
        background.setWidth(194);
        background.setHeight(178);
        background.setFill(Color.AQUAMARINE);
        
        //create game over screen
        gameOver = new ImageView("gaemover.png");
        gameOver.setVisible(false);
        gameOver.setTranslateX(getTranslateX());
        gameOver.setTranslateY(getTranslateY());
        
        // create start game screen
        startGame = new ImageView("presakey.png");
        startGame.setTranslateX(getTranslateX());
        startGame.setTranslateY(getTranslateY());
        
        // create pause screen
        pauseGame = new ImageView("paused.png");
        pauseGame.setTranslateX(getTranslateX());
        pauseGame.setTranslateY(getTranslateY());
        
        // add some background colors
        backgroundColors = new ArrayList<>();
        backgroundColors.add(Color.AQUAMARINE);
        backgroundColors.add(Color.BEIGE);
        backgroundColors.add(Color.CORNFLOWERBLUE);
        backgroundColors.add(Color.DARKORANGE);
        backgroundColors.add(Color.GOLD);
        backgroundColors.add(Color.LIGHTPINK);
        
        // add player point textures
        playerImages = new ArrayList<>();
        playerImages.add(new Image("point.png"));
        playerImages.add(new Image("pointW.png"));
        playerImages.add(new Image("pointR.png"));
        playerImages.add(new Image("pointG.png"));
        playerImages.add(new Image("pointB.png"));
        currentPlayerImage = new Image("point.png");
        
        // create score label
        Text scoreLabel = new Text("Score:");
        scoreLabel.setTranslateX(65);
        scoreLabel.setTranslateY(-80);
        score.setText("0");
        score.setTranslateX(65);
        score.setTranslateY(-60);
        
        // create instruction label
        Text resetLabel = new Text("Press\nR\nto reset\nthe game.");
        resetLabel.setTextAlignment(TextAlignment.CENTER);
        resetLabel.setTranslateX(65);
        resetLabel.setTranslateY(50);
        
        // add objects as children
        this.getChildren().add(background);
        this.getChildren().add(scoreLabel);
        this.getChildren().add(score);
        this.getChildren().add(resetLabel);
        
        // create borders
        createBorders();
        
        // create player
        p.setPosX(0);
        p.setPosY(0);
        
        // paint player
        paintActors();
        
        // show startGame screen
        this.getChildren().add(startGame);
    }
    
    /**
     * Method returns true if game should be running.
     * @return true if isRunning, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Method shows or hides game over screen.
     * @param show 
     */
    void showGameOver(boolean show) {
        if(show) {
            if(this.getChildren().contains(gameOver))
                this.getChildren().remove(gameOver);
            this.getChildren().add(gameOver);
            gameOver.setVisible(true);
        }
        else {
            gameOver.setVisible(false);
            this.getChildren().remove(gameOver);
        }
    }
    
    /**
     * Method randomly choses between Colors in the list and applies background color.
     */
    public void changeBackground() {
        Color temp = backgroundColors.get(ThreadLocalRandom.current().nextInt(0, backgroundColors.size()));     // get random color
        if(temp == background.getFill())        // if color is the same as current background color
            changeBackground();                 // try again
        
        background.setFill(temp);       // set color
    }
    
    /**
     * Method moves player in specified direction.
     * @param direction Left or right direction. Use Settings.MOVE_LEFT or Settings.MOVE_LEFT.
     * @throws MalformedURLException 
     */
    public void movePlayer(int direction) throws MalformedURLException {
        if(this.getChildren().contains(startGame)) {        // if game hasnt started yet
            isRunning = true;                               // start the game
            playSound();                                    // start playing the sound
            this.getChildren().remove(startGame);           // remove startGame image
        }
        
        clearGrid();        // clear the grid
        
        // create new position for player
        int newXpos = p.getPosX() + direction;      
        if(newXpos < 0) newXpos = 0;
        if(newXpos > 1) newXpos = 1;
        
        // check if new position isn't colliding with bot, if not, move player
        if(!detectCollisionsAt(p.getPosY(), newXpos))
            p.moveToSide(direction);
        
        paintActors();      // repaint actors
    }
    
    /**
     * Method starts playing the sound.
     * @throws MalformedURLException 
     */
    void playSound() throws MalformedURLException {
        if(isPlaying) return;
        
        builder = MediaPlayerBuilder
            .create()
            .media(new Media(GameBoy.class.getResource("/loop.mp3").toString()))
            .volume(.2f)
            .onEndOfMedia(new Runnable() {
                public void run() {
                    MediaPlayer player = GameWindow.this.builder.build();
                    player.play();
                }
            })
            .onReady(new Runnable() {
                public void run() {
                    isPlaying = true;
                }
            });

        player = this.builder.build();
        player.play();
    }
    
    /**
     * Method creates left and right border on the grid.
     */
    void createBorders() {
        for(int i=0; i<10; i+=9) {
            for(int j=0; j<13; j++) {
                FillPoint(j,i, false);
            }
        }
    }
    
    /**
     * Paints point in the grid.
     * @param y row
     * @param x collumn
     * @param isPlayer If set to true, image used for point will be currentPlayerImage
     */
    void FillPoint(int y, int x, boolean isPlayer) {
        if(x>9 || y<0 || y>12)      // if point is not on the grid
            return;
        
        ImageView temp = new ImageView();
        temp.setFitWidth(13);                   // point is 13x13 pixels
        temp.setFitHeight(13);
        temp.setTranslateX(13*x -90);           // set point position X
        temp.setTranslateY(13*y -79);           // set point position Y
        if(isPlayer) temp.setImage(currentPlayerImage);     // if is player, image used for point will be currentPlayerImage
        else temp.setImage(new Image("point.png"));         // else, default image is used
        
        points.add(temp);
        this.getChildren().add(temp);
    }
    
    /**
    * Paints actor at desired position
    * Bottom row is represented by number 0
    * Left and right collumns are 0 and 1 respectively
    * Bottom row is 0.
    */
    void PaintActor(int row, int collumn) {
        FillPoint(height - 1 - row, collumn*3 + 2, false);
        FillPoint(height - 1 - row, collumn*3 + 4, false);
        row++;
        FillPoint(height - 1 - row, collumn*3 + 3, false);
        row++;
        FillPoint(height - 1 - row, collumn*3 + 2, false);
        FillPoint(height - 1 - row, collumn*3 + 3, false);
        FillPoint(height - 1 - row, collumn*3 + 4, false);
        row++;
        FillPoint(height - 1 - row, collumn*3 + 3, false);
    }
    
    /**
     * Method paints the player.
     * @param row row to paint player in
     * @param collumn collumn that player is currently in
     */
    void PaintPlayer(int row, int collumn) {
        FillPoint(height - 1 - row, collumn*3 + 2, true);       // bottom left
        FillPoint(height - 1 - row, collumn*3 + 4, true);       // bottom right
        row++;
        FillPoint(height - 1 - row, collumn*3 + 3, true);       // second row middle
        row++;
        FillPoint(height - 1 - row, collumn*3 + 2, true);       // third row
        FillPoint(height - 1 - row, collumn*3 + 3, true);
        FillPoint(height - 1 - row, collumn*3 + 4, true);
        row++;
        FillPoint(height - 1 - row, collumn*3 + 3, true);       // top center point
    }
    
    /**
     * Method changes color of player
     */
    public void changePlayerColor() {
        Image temp = playerImages.get(ThreadLocalRandom.current().nextInt(0, playerImages.size()));     // set random image for player
        if(temp == currentPlayerImage)      // if image is the same as player has now
            changePlayerColor();            // try again
        
        currentPlayerImage = temp;      // set player image
        clearGrid();                    // clear grid
        paintActors();                  // paint player and bots
    }
    
    /**
     * Method clears the grid and repaints borders.
     */
    public void clearGrid() {
        this.getChildren().removeAll(points);       // remove all painted points
        points.removeAll(points);                   // remove points from the list
        createBorders();                            // paint borders
    }

    @Override
    public void onUpdate() {
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                if(readyToSpawnBot())           // if game is ready to spawn bot
                   spawnBot();                  // spawn bot
                
                clearGrid();                    // clear grid
                destroyDeactivatedBots();       // destroy deactivated bots and paint actors
            }
        });
    }
    
    /**
     * Method will paint all actors (player and bots).
     */
    void paintActors() {
        PaintPlayer(p.getPosY(), p.getPosX());      // paint player
        
        // paint bots
        for(Bot b : bots) {
            PaintActor(b.getPosY(), b.getPosX());
        }
        
        // detect collision between player and bots
        detectCollisions();
    }
    
    /**
     * Method will destroy all bots that are below the visible screen.
     */
    void destroyDeactivatedBots() {
        Bot toRemove = new Bot();
        for(Bot b : bots) {
            if(b.isDeactivated()) {     // if bot is deactivated
                toRemove = b;           // save bot for removing
                break;
            }
        }
        GameBoy.removeUpdateEventListener(toRemove);     // remove update listener
        bots.remove(toRemove);                          // remove bot
        clearGrid();                                    // clear grid
        paintActors();                                  // paint bots and player
    }
    
    /**
     * Method will show or hide pause screen based on parameter.
     * @param pause If true, pause image will be shown, if false, pause image will be hidden.
     */
    public void pause(boolean pause) {
        if(pause) {
            this.getChildren().add(pauseGame);
        }
        else {
            this.getChildren().remove(pauseGame);
        }
    }
    
    /**
     * Method will detect any collisions.
     * If positive, game will be ended.
     */
    void detectCollisions() {
        for(Bot b : bots) {
            if(p.isCollidingWith(b)) {      // if player is colliding
                isRunning = false;          // stop game
                showGameOver(true);         // show game over image
                GameBoy.endGame();          // call GameBoy to stop game thread from trying to the run game 
            }
        }
    }
    
    /**
     * Detects future collision of player.
     * @param positionY future player Y position
     * @param positionX future player X position
     * @return true if player will be colliding, otherwise false
     */
    boolean detectCollisionsAt(int positionY, int positionX) {
        Player temp = new Player();
        temp.setPosX(positionX);
        temp.setPosY(positionY);
        for(Bot b : bots) {
            if(temp.isCollidingWith(b))
                return true;
        }
        return false;
    }
    
    /**
     * Method decides if game is ready to spawn bot based on position of other bots.
     * @return true if game is ready to spawn bot
     */
    boolean readyToSpawnBot() {
        boolean ready = true;
        for(Bot b : bots) {
            if(b.getPosY() > 4)     // if there is any bot above 4th row
                ready = false;
        }
        return ready;
    }
    
    /**
     * Method spawns bot at top of the window.
     * Has 50% chance to spawn bot and bot will be spawned randomly in right or left collumn.
     */
    void spawnBot() {
        // 50% chance to spawn bot
        if(ThreadLocalRandom.current().nextInt(0, 1 + 1) == 1) {
            Bot temp = new Bot();
            temp.setPosX(ThreadLocalRandom.current().nextInt(0, 1 + 1));        // set collumn
            temp.setPosY(13);                                                   // set Y position to be above top-most visible row
            bots.add(temp);                                                     // add bot
            
            GameBoy.addUpdateEventListener(temp);                                // add update listener
            clearGrid();                                                        // clear grid
            paintActors();                                                      // paint bots and player
        }
    }
    
    /**
     * Updates and shows new score on the screen.
     * @param score
     */
    public void updateScore(int score) {
        this.score.setText("" + score);
    }
    
    /**
     * Resets GameWindow and shows startGame image.
     */
    public void resetGame() {
        // remove update listeners from all bots
        for(Bot b : bots) {
            GameBoy.removeUpdateEventListener(b);
        }
        showGameOver(false);        // hide game over image
        bots.removeAll(bots);       // remove all bots
        paintActors();              // paint new actors
        isRunning = false;          // make game stop
        
        // show start game image
        if(!this.getChildren().contains(startGame))
            this.getChildren().add(startGame);
    }
}
