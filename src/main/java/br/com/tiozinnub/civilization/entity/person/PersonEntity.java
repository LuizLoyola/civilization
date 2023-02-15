package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.entity.EntityBase;
import br.com.tiozinnub.civilization.entity.property.Gender;
import br.com.tiozinnub.civilization.entity.property.IGendered;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class PersonEntity extends EntityBase implements IGendered {
    public static final EntityDimensions DIMENSIONS = EntityDimensions.changing(0.6F, 1.8F);
    public static final SpawnGroup SPAWN_GROUP = SpawnGroup.MISC;
    public static final String NAME = "person";

    private static final TrackedData<String> GENDER_STR;
    private static final TrackedData<String> SPECIES_STR;

    static {
        GENDER_STR = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.STRING);
        SPECIES_STR = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.STRING);
    }

    public PersonEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);

        this.startTracking(world.getRandom());
    }

    private void startTracking(Random random) {
        this.dataTracker.startTracking(GENDER_STR, Gender.randomGender(random).asString());
        this.dataTracker.startTracking(SPECIES_STR, Species.HUMAN.asString());
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return EntityBase.createMobAttributes(); // TODO
    }

    @Override
    public Gender getGender() {
        return Gender.byName(this.dataTracker.get(GENDER_STR));
    }

    public Species getSpecies() {
        return Species.byName(this.dataTracker.get(SPECIES_STR));
    }
}
