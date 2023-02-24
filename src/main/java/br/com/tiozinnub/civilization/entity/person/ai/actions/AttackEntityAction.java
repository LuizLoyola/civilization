package br.com.tiozinnub.civilization.entity.person.ai.actions;

import br.com.tiozinnub.civilization.entity.base.WalkPace;
import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import br.com.tiozinnub.civilization.entity.person.ai.Action;
import net.minecraft.entity.LivingEntity;

public class AttackEntityAction extends Action {
    private LivingEntity target;

    public AttackEntityAction(PersonEntity person, LivingEntity target) {
        super(person);
        this.target = target;
    }

    @Override
    protected void tick() {
        assert this.target != null;

        if (this.target.isDead()) {
            this.target = null;
            this.finish(true);
            return;
        }

        if (this.person.isInAttackRange(this.target)) {
            this.person.attack(this.target);
        } else {
            this.person.setMovementTarget(this.target, WalkPace.RUN_JUMP, true, false, this.person.squaredAttackRange(this.target) - 1d);
        }

    }

    @Override
    protected boolean canTick() {
        return this.target != null;
    }
}
