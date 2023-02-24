package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.utils.Serializable;

public class PersonHungerManager extends Serializable {
    private final PersonEntity entity;

    public PersonHungerManager(PersonEntity entity) {

        this.entity = entity;
    }

    @Override
    public void registerProperties(SerializableHelper helper) {

    }

    public void tick() {
    }

    public void addExhaustion(float exhaustion) {
    }
}
