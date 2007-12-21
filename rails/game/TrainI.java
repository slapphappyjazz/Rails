/* $Header: /Users/blentz/rails_rcs/cvs/18xx/rails/game/TrainI.java,v 1.5 2007/12/21 21:18:12 evos Exp $ */
package rails.game;

public interface TrainI
{
    public void init (TrainTypeI type, int index);

	/**
	 * @return Returns the cost.
	 */
	public int getCost();

	/**
	 * @return Returns the number of major stops cities, off-board, perhaps
	 *         towns.
	 */
	public int getMajorStops();

	/**
	 * @return Returns the minorStops (towns).
	 */
	public int getMinorStops();

	/**
	 * @return Returns the townCountIndicator (major, minor or not at all).
	 */
	public int getTownCountIndicator();

	/**
	 * @return Returns the cityScoreFactor (0 or 1).
	 */
	public int getCityScoreFactor();

	/**
	 * @return Returns the townScoreFactor (0 or 1).
	 */
	public int getTownScoreFactor();

	/**
	 * @return Returns the train type.
	 */
	public TrainTypeI getType();

	public String getName();
	
	public String getUniqueId ();

	public Portfolio getHolder();

	public CashHolder getOwner();

    public boolean isObsolete ();

	public void setHolder(Portfolio newHolder);
    
    public void moveTo (Portfolio to);

	public void setRusted();
    
    public void setObsolete ();

	public boolean canBeExchanged();

    public String toDisplay();

}
