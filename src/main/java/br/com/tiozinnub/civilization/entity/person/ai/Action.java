package br.com.tiozinnub.civilization.entity.person.ai;

import br.com.tiozinnub.civilization.entity.person.PersonEntity;

public abstract class Action {
    protected final PersonEntity person;
    private boolean finished;
    private boolean finishedSuccess;

    protected Action(PersonEntity person) {
        this.person = person;
    }

    protected abstract void tick();

    protected abstract boolean canTick();

    public boolean isFinished() {
        return this.finished;
    }

    protected void finish(boolean success) {
        this.finished = true;
        this.finishedSuccess = success;
    }
}
