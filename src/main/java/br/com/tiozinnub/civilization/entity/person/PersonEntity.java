package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.entity.base.PathingEntity;
import br.com.tiozinnub.civilization.entity.person.property.Gender;
import br.com.tiozinnub.civilization.entity.person.property.IGendered;
import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static br.com.tiozinnub.civilization.entity.person.property.Gender.byGender;

public class PersonEntity extends PathingEntity implements IGendered {

    private static final TrackedData<NbtCompound> IDENTITY;

    private static final TrackedData<NbtCompound> EQUIPPED_ITEMS;

    static {
        IDENTITY = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
        EQUIPPED_ITEMS = DataTracker.registerData(PersonEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);
    }

    private final PersonInventory inventory;
    private boolean registeredOnCatalog = false;

    public PersonEntity(EntityType<? extends PathingEntity> entityType, World world) {
        super(entityType, world);

        this.startTracking();

        if (!this.isClient()) {
            this.inventory = new PersonInventory(this);
        } else {
            this.inventory = null;
        }
    }

    public static DefaultAttributeContainer.Builder createPersonAttributes() {
        return createPathingEntityAttributes();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        if (!this.isClient()) {
            return this.inventory.getStackAtEquipmentSlot(slot);
        }

        return ItemStack.fromNbt(this.dataTracker.get(EQUIPPED_ITEMS).getCompound(slot.getName()));
    }

    @Override
    public Iterable<ItemStack> getHandItems() {
        return Arrays.asList(this.getMainHandStack(), this.getOffHandStack());
    }

    @Override
    public ItemStack getMainHandStack() {
        return this.getEquippedStack(EquipmentSlot.MAINHAND);
    }

    @Override
    public ItemStack getOffHandStack() {
        return this.getEquippedStack(EquipmentSlot.OFFHAND);
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return Arrays.asList(
                this.getEquippedStack(EquipmentSlot.HEAD),
                this.getEquippedStack(EquipmentSlot.CHEST),
                this.getEquippedStack(EquipmentSlot.LEGS),
                this.getEquippedStack(EquipmentSlot.FEET)
        );
    }

    @Override
    public void tickMovement() {
        if (!this.isClient()) {
            this.inventory.tick();
            this.dataTracker.set(EQUIPPED_ITEMS, this.inventory.getEquippedItemsAsNbt());
            this.inventory.setClean();
        }

        super.tickMovement();
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        return super.canPickupItem(stack);
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    @Override
    protected void loot(ItemEntity item) {
        var itemStack = item.getStack();

        var allowArmor = false;

        while (!itemStack.isEmpty()) {
            var added = this.inventory.insertStack(itemStack, allowArmor);
            if (added) {
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, this.getSoundCategory(), 0.2F, ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                break;
            }
        }

        if (itemStack.isEmpty()) {
            item.setPos(this.getX(), this.getY(), this.getZ());
            item.discard();
        }
    }


    @Override
    public void sendPickup(Entity item, int count) {
        super.sendPickup(item, count);
    }

    @Override
    protected Vec3i getItemPickUpRangeExpander() {
        return super.getItemPickUpRangeExpander();
    }

    @Override
    public void setCanPickUpLoot(boolean canPickUpLoot) {
        super.setCanPickUpLoot(canPickUpLoot);
    }

    @Override
    public void triggerItemPickedUpByEntityCriteria(ItemEntity item) {
        super.triggerItemPickedUpByEntityCriteria(item);
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

    protected void dropInventory() {
        this.vanishCursedItems();
        this.inventory.dropAll();
    }

    @Override
    protected boolean shouldDropLoot() {
        return super.shouldDropLoot();
    }

    protected void vanishCursedItems() {
        for (var i = 0; i < this.inventory.size(); ++i) {
            var itemStack = this.inventory.getStack(i);

            if (itemStack.isEmpty() || !EnchantmentHelper.hasVanishingCurse(itemStack)) continue;

            this.inventory.removeStack(i);
        }
    }

    public void dropItem(ItemStack stack, boolean retainOwnership) {
        this.dropItem(stack, false, retainOwnership);
    }

    public void dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership) {
        if (stack.isEmpty()) return;

        var d = this.getEyeY() - 0.30000001192092896;
        var itemEntity = new ItemEntity(this.world, this.getX(), d, this.getZ(), stack);
        itemEntity.setPickupDelay(40);
        if (retainOwnership) {
            itemEntity.setThrower(this.getUuid());
        }

        float f;
        float g;
        if (throwRandomly) {
            f = this.random.nextFloat() * 0.5F;
            g = this.random.nextFloat() * 6.2831855F;
            itemEntity.setVelocity(-MathHelper.sin(g) * f, 0.20000000298023224, MathHelper.cos(g) * f);
        } else {
            g = MathHelper.sin(this.getPitch() * 0.017453292F);
            var h = MathHelper.cos(this.getPitch() * 0.017453292F);
            var i = MathHelper.sin(this.getYaw() * 0.017453292F);
            var j = MathHelper.cos(this.getYaw() * 0.017453292F);
            var k = this.random.nextFloat() * 6.2831855F;
            var l = 0.02F * this.random.nextFloat();
            itemEntity.setVelocity((double) (-i * h * 0.3F) + Math.cos(k) * (double) l, -g * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F, (double) (j * h * 0.3F) + Math.sin(k) * (double) l);
        }

        this.world.spawnEntity(itemEntity);
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
        this.dataTracker.startTracking(EQUIPPED_ITEMS, new NbtCompound());
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
                for (var i = 2; i <= 20; i = i + 2) {
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
    public void damageArmor(DamageSource source, float amount) {
        this.inventory.damageArmor(source, amount, PersonInventory.ARMOR_SLOTS);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);

        if (nbt.contains("identity")) this.dataTracker.set(IDENTITY, nbt.getCompound("identity"));
    }

    public PersonInventory getPersonInventory() {
        return this.inventory;
    }

    @Override
    public SimpleInventory getInventory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeInventory(NbtCompound nbt) {
        this.inventory.toNbt(nbt, "inventory");
    }

    @Override
    public void readInventory(NbtCompound nbt) {
        this.inventory.fromNbt(nbt, "inventory");
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
