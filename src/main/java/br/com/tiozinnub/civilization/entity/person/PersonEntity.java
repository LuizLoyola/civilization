package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.entity.PathingEntity;
import br.com.tiozinnub.civilization.entity.property.Gender;
import br.com.tiozinnub.civilization.entity.property.IGendered;
import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static br.com.tiozinnub.civilization.entity.property.Gender.byGender;

public class PersonEntity extends PathingEntity implements IGendered {

    private static final TrackedData<NbtCompound> IDENTITY;

    static {
        IDENTITY = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    }

    private boolean registeredOnCatalog = false;

    public PersonEntity(EntityType<? extends PathingEntity> entityType, World world) {
        super(entityType, world);

        this.startTracking();
    }

    public static DefaultAttributeContainer.Builder createPersonAttributes() {
        return createPathingEntityAttributes();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> Items.DIAMOND_PICKAXE.getDefaultStack();
            case OFFHAND -> Items.DIAMOND_PICKAXE.getDefaultStack();
            case FEET -> Items.DIAMOND_BOOTS.getDefaultStack();
            case LEGS -> Items.DIAMOND_LEGGINGS.getDefaultStack();
            case CHEST -> Items.DIAMOND_CHESTPLATE.getDefaultStack();
            case HEAD -> Items.DIAMOND_HELMET.getDefaultStack();
        };
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient()) return;

        if (!registeredOnCatalog) {
            ((IServerWorldExt) getWorld()).getPersonCatalog().update(getId(), getUuid());
            registeredOnCatalog = true;
        }
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);

        if (world instanceof ServerWorld serverWorld) {
            ((IServerWorldExt) serverWorld).getPersonCatalog().remove(getUuid());
        }
    }

    private void startTracking() {
        this.dataTracker.startTracking(IDENTITY, new NbtCompound());
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
        // int to str
        // Option 1
        // String str = Integer.toString(i);

        // Option 2
        // String str = String.valueOf(i);

        return Arrays.stream(new Text[]{
                Text.of(sbName.toString()),
                Text.of(sbHealth.toString())
//                Text.of(sbHunger.toString()),
                //Text.of("§00§11§22§33§44§55§66§77§88§99§aa§bb§cc§dd§ee§ff")
        }).toList();
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (getWorld().isClient()) return ActionResult.SUCCESS;

//        player.sendMessage(Text.of(getUuidAsString()), false);
//        player.sendMessage(Text.of(getUuid().toString()), false);
//        player.sendMessage(Text.of(String.valueOf(getId())), false);

        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        if (entityNbt != null) {
            readCustomDataFromNbt(entityNbt);
        } else {
            this.dataTracker.set(IDENTITY, PersonIdentity.randomize(getUuid(), world.getRandom()).toNbt());
            // cancel creation, kill entity
//            this.kill();
        }

        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("identity")) {
            this.dataTracker.set(IDENTITY, nbt.getCompound("identity"));
        }
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
