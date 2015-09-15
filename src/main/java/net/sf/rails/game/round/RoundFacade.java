package net.sf.rails.game.round;

import net.sf.rails.game.RailsItem;
import net.sf.rails.game.state.Creatable;
import rails.game.action.PossibleAction;

public interface RoundFacade extends Creatable, RailsItem {

    // TODO: Remove as this is abstract class?
    // called from GameManager
    public abstract String getHelp();

    // called from GameManager
    public abstract boolean process(PossibleAction action);

    /** Generic stub to resume an interrupted round.
     * Only valid if implemented in a subclass.
     *
     */
    // called from GameManager
    public abstract void resume();

    // called from GameManager and GameUIManager
    public abstract String getRoundName();

    /** A stub for processing actions triggered by a phase change.
     * Must be overridden by subclasses that need to process such actions.
     * @param name (required) The name of the action to be executed
     * @param value (optional) The value of the action to be executed, if applicable
     */
    // can this be moved to GameManager, not yet as there are internal dependencies
    // called from GameManager
    public abstract void processPhaseAction(String name, String value);

    public abstract boolean setPossibleActions();

}