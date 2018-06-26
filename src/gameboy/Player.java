/*
 * (c) 2016 - Marek Kost and Lukas Bandura
 */
package gameboy;

/**
 * Class responsible for player behavior.
 * @author Marek Kost
 */
public class Player extends Actor {
    
    /**
     * Method moves the player to the left or to the right.
     * @param direction -1 for left, 1 for right, use Settings.MOVE_LEFT and Settings.MOVE_RiGHT
     */
    public void moveToSide(int direction) {
        if(direction == Settings.MOVE_LEFT) {
            if(getPosX() != 0) setPosX(0);
        }
        else if (direction == Settings.MOVE_RIGHT) {
            if(getPosX() != 1) setPosX(1);
        }
    }
    
    /**
     * Method used to detect collision of player.
     * @param actor Actor to check if player is colliding with.
     * @return True if collided, otherwise false.
     */
    public boolean isCollidingWith(Actor actor) {
        return getPosX() == actor.getPosX() && getPosY() + 4 > actor.getPosY();
    }
}
