package rails.game.state;

/**
 * Identifies items which are countable
 * They are stored inside wallets
 * @author freystef
 */

public interface CountableItem extends Item {

    public int value();
    
    public void change(int amount);
    
    public void set(int amount);
    
}
