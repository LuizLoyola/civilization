package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.stat.Stat;

public class PersonStatHandler extends Serializable {
    public PersonStatHandler() {

    }

    @Override
    public void registerProperties(SerializableHelper helper) {

    }

    public void increaseStat(Stat<?> stat, int amount) {
    }
}
