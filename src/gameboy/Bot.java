/*
 * (c) 2016 - Marek Kost and Lukas Bandura
 */
package gameboy;

/**
 * Class responsible for bot behavior.
 * @author Marek Kost
 */
public class Bot extends Actor implements UpdateEventListener {
    private boolean deactivated = false;        // signals if bot should be destroyed
    
    /**
     * Method moves bot down the grid by 1 point.
     */
    public void move() {
        setPosY(getPosY()-1);       // set position
        if(getPosY() < -3) setDeactivated(true);        // if new position gets bot below grid, mark him deactivated
    }

    @Override
    public void onUpdate() {
        move();
    }
    
    /**
     * Method checks if bot is marked deactivate.
     * @return true if deactivated, otherwise false
     */
    public boolean isDeactivated() {
        return deactivated;
    }
    
    /**
     * Method sets bot's deacivated property.
     * @param deactivated 
     */
    void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }
}
