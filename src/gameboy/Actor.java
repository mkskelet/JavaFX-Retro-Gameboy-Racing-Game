/*
 * (c) 2016 - Marek Kost and Lukas Bandura
 */
package gameboy;

/**
 * Base class for player and bots.
 * @author Marek Kost
 */
public class Actor {
    private int posX;       // X position of Actor
    private int posY;       // Y position of Actor

    /**
     * Sets X position.
     * @param posX 
     */
    public void setPosX(int posX) {
        this.posX = posX;
    }

    /**
     * Sets Y position.
     * @param posY 
     */
    public void setPosY(int posY) {
        this.posY = posY;
    }

    /**
     * Returns X position.
     * @return 
     */
    public int getPosX() {
        return posX;
    }

    /**
     * Return Y position.
     * @return 
     */
    public int getPosY() {
        return posY;
    }
}
