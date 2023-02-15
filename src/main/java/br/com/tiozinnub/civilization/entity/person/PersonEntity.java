package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.entity.EntityBase;
import br.com.tiozinnub.civilization.entity.property.Gender;
import br.com.tiozinnub.civilization.entity.property.IGendered;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static br.com.tiozinnub.civilization.entity.property.Gender.byGender;

public class PersonEntity extends EntityBase implements IGendered {
    public static final EntityDimensions DIMENSIONS = EntityDimensions.changing(0.6F, 1.8F);
    public static final SpawnGroup SPAWN_GROUP = SpawnGroup.MISC;
    public static final String NAME = "person";

    private static final TrackedData<NbtCompound> IDENTITY;

    static {
        IDENTITY = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    }

    public PersonEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);

        this.startTracking();
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return EntityBase.createMobAttributes(); // TODO
    }

    private void startTracking() {
        this.dataTracker.startTracking(IDENTITY, PersonIdentity.empty().toNbt());
    }

    public PersonIdentity getIdentity() {
        return new PersonIdentity(this.dataTracker.get(IDENTITY));
    }

    @Override
    public Gender getGender() {
        return getIdentity().getGender();
    }


    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public List<Text> getMultilineNameplate() {
        var sbName = new StringBuilder();
        var health = (int) this.getHealth();

        if (health == 0) health = (int) Math.ceil(this.getHealth());

        sbName.append(getIdentity().getFullName());
        sbName.append(" ");
        sbName.append(byGender(getGender(), "§1\u2642", "§5\u2640"));

        var sbHealth = new StringBuilder();
        if (health <= 0) {
            sbHealth.append("§f");
            sbHealth.append("\u2620");
            sbHealth.append("§r");
        } else {
            sbHealth.append("§4");
            if (health > 20) {
                sbHealth.append(health);
                sbHealth.append(" \u2764");
            } else {
                var gray = false;
                for (int i = 2; i <= 20; i = i + 2) {
                    if (health == i - 1) sbHealth.append("§e");
                    else if (health < i && !gray) {
                        sbHealth.append("§7");
                        gray = true;
                    }
                    sbHealth.append("\u2764");
                }
            }
        }

//        var sbHunger = new StringBuilder();
//        var foodLevel = 20; // this.getFoodLevel();
//        if (health > 0) {
//            sbHunger.append("§6");
//            var gray = false;
//            for (int i = 2; i <= 20; i = i + 2) {
//                if (foodLevel == i - 1) sbHunger.append("§e");
//                else if (foodLevel < i && !gray) {
//                    sbHunger.append("§7");
//                    gray = true;
//                }
//                sbHunger.append("\uD83C\uDF56");
//            }
//        }

        return Arrays.stream(new Text[]{
                Text.of(sbName.toString()),
                Text.of(sbHealth.toString()),
//                Text.of(sbHunger.toString()),
                //Text.of("§00§11§22§33§44§55§66§77§88§99§aa§bb§cc§dd§ee§ff")
        }).toList();
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if (entityNbt != null) {
            readCustomDataFromNbt(entityNbt);
        } else {
//            this.dataTracker.set(IDENTITY, PersonIdentity.randomize(world.getRandom()).toNbt());
            // cancel creation, kill entity
            this.kill();
        }

        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        this.dataTracker.set(IDENTITY, nbt.getCompound("identity"));
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.put("identity", this.dataTracker.get(IDENTITY));
    }

    @Override
    public String getEntityName() {
        return super.getEntityName();
    }

    @Override
    protected Text getDefaultName() {
        return super.getDefaultName();
    }

    @Nullable
    @Override
    public Text getCustomName() {
        return super.getCustomName();
    }

    @Override
    public Text getDisplayName() {
        return super.getDisplayName();
    }

    @Override
    public Text getName() {
        return Text.of(getIdentity().getFullName());
    }
}
