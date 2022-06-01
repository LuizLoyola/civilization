package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.entity.EntityBase;
import br.com.tiozinnub.civilization.entity.property.Gender;
import br.com.tiozinnub.civilization.entity.property.IGendered;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

import static br.com.tiozinnub.civilization.entity.property.Gender.byGender;
import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class PersonEntity extends EntityBase implements IGendered {
    public static final EntityDimensions DIMENSIONS = EntityDimensions.changing(0.6F, 1.8F);
    public static final SpawnGroup SPAWN_GROUP = SpawnGroup.MISC;
    public static final String NAME_MALE = "person_male";
    public static final String NAME_FEMALE = "person_female";

    private static final TrackedData<String> FIRST_NAME;
    private static final TrackedData<String> LAST_NAME;

    static {
        FIRST_NAME = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.STRING);
        LAST_NAME = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.STRING);

    }

    private final Gender gender;

    public PersonEntity(EntityType<? extends LivingEntity> type, World world, Gender gender) {
        super(type, world);
        this.gender = gender;

        this.startTracking();
    }

    private void startTracking() {
        this.dataTracker.startTracking(FIRST_NAME, "first_name");
        this.dataTracker.startTracking(LAST_NAME, "last_name");
    }

    public String getFirstName() {
        return this.dataTracker.get(FIRST_NAME);
    }

    public String getLastName() {
        return this.dataTracker.get(LAST_NAME);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return EntityBase.createLivingAttributes();
    }

    @Override
    public String getNameId() {
        return byGender(gender, NAME_MALE, NAME_FEMALE);
    }

    @Override
    public List<Text> getMultilineNameplate() {
        return List.of(
                Text.of("%s %s".formatted(this.getFirstName(), this.getLastName()))
        );
    }

    public Identifier getSkinTexture() {
        return idFor("textures/entity/person/%s.png".formatted(gender.asString()));
    }

    @Override
    public Gender getGender() {
        return this.gender;
    }
}
