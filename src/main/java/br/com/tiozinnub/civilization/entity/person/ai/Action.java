package br.com.tiozinnub.civilization.entity.person.ai;

import br.com.tiozinnub.civilization.entity.person.PersonEntity;

public abstract class Action {
    protected final PersonEntity person;

    protected Action(PersonEntity person) {
        this.person = person;
    }

    protected abstract void tick();

    protected abstract boolean canTick();
}
