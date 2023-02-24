package br.com.tiozinnub.civilization.entity.person.ai;

import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import br.com.tiozinnub.civilization.utils.Serializable;

public class PersonMind extends Serializable {
    private final PersonEntity person;
    private Action currentAction;

    public PersonMind(PersonEntity person) {
        this.person = person;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {

    }

    public Action getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(Action currentAction) {
        this.currentAction = currentAction;
    }

    public void tick() {
        if (this.currentAction == null) return;

        if (this.currentAction.canTick()) {
            this.currentAction.tick();
        }

        if (this.currentAction.isFinished()) {
            this.currentAction = null;
        }
    }
}
