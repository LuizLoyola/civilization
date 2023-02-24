package br.com.tiozinnub.civilization.entity.person.ai.actions;

import br.com.tiozinnub.civilization.entity.base.WalkPace;
import br.com.tiozinnub.civilization.entity.person.PersonEntity;
import br.com.tiozinnub.civilization.entity.person.ai.Action;
import net.minecraft.entity.LivingEntity;

public class AttackEntityAction extends Action {
    private LivingEntity target;

    public AttackEntityAction(PersonEntity person) {
        super(person);
    }

    @Override
    protected void tick() {
        assert this.target != null;

        if (this.target.isDead()) {
            this.target = null;
            return;
        }

        if (this.person.isInAttackRange(this.target)) {
            this.person.getNavigation().stop();
            this.person.attack(this.target);
        } else {
            this.person.setMovementTarget(this.target, WalkPace.RUN_JUMP, true, false, this.person.squaredAttackRange(this.target));
        }

    }

    @Override
    protected boolean canTick() {
        return this.target != null;
    }
}
