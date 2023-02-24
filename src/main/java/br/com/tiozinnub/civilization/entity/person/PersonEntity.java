package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.entity.base.PathingEntity;
import br.com.tiozinnub.civilization.entity.person.ai.PersonMind;
import br.com.tiozinnub.civilization.entity.person.property.Gender;
import br.com.tiozinnub.civilization.entity.person.property.IGendered;
import br.com.tiozinnub.civilization.ext.IServerWorldExt;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
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
    private final PersonStatHandler statHandler;
    private final PersonHungerManager hungerManager;
    private final PersonMind mind;
    private boolean registeredOnCatalog = false;

    public PersonEntity(EntityType<? extends PathingEntity> entityType, World world) {
        super(entityType, world);

        this.startTracking();

        if (!this.isClient()) {
            this.inventory = new PersonInventory(this);
            this.statHandler = new PersonStatHandler();
            this.hungerManager = new PersonHungerManager(this);
            this.mind = new PersonMind(this);
        } else {
            this.inventory = null;
            this.statHandler = null;
            this.hungerManager = null;
            this.mind = null;
        }
    }

    public static DefaultAttributeContainer.Builder createPersonAttributes() {
        return createPathingEntityAttributes()
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1.0f)
//                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1f)
//                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1.6f)
//                .add(EntityAttributes.GENERIC_LUCK)
                ;
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


        // TODO: Check if person catalog is still needed
        if (!registeredOnCatalog) {
            ((IServerWorldExt) getWorld()).getPersonCatalog().update(getId(), getUuid());
            registeredOnCatalog = true;
        }

        ++this.lastAttackedTicks;

        this.hungerManager.tick();

        this.mind.tick();
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

        var d = this.getEyeY() - 0.3f;
        var itemEntity = new ItemEntity(this.world, this.getX(), d, this.getZ(), stack);
        itemEntity.setPickupDelay(40);
        if (retainOwnership) {
            itemEntity.setThrower(this.getUuid());
        }

        float f;
        float g;
        if (throwRandomly) {
            f = this.random.nextFloat() * 0.5F;
            g = this.random.nextFloat() * ((float) Math.PI * 2F);
            itemEntity.setVelocity(-MathHelper.sin(g) * f, 0.2, MathHelper.cos(g) * f);
        } else {
            g = MathHelper.sin(this.getPitch() * (float) Math.PI / 180.0F);
            var h = MathHelper.cos(this.getPitch() * (float) Math.PI / 180.0F);
            var i = MathHelper.sin(this.getYaw() * (float) Math.PI / 180.0F);
            var j = MathHelper.cos(this.getYaw() * (float) Math.PI / 180.0F);
            var k = this.random.nextFloat() * (float) Math.PI * 2F;
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
        if (getWorld().isClient()) return ActionResult.PASS;

//        player.sendMessage(Text.of(getUuidAsString()), false);
//        player.sendMessage(Text.of(getUuid().toString()), false);
//        player.sendMessage(Text.of(String.valueOf(getId())), false);

//        this.mind.setCurrentAction(new AttackEntityAction(this, player));

//        this.inventory.setStack(37, Items.DIAMOND_BOOTS.getDefaultStack());
//        this.inventory.setStack(38, Items.DIAMOND_LEGGINGS.getDefaultStack());
//        this.inventory.setStack(39, Items.DIAMOND_CHESTPLATE.getDefaultStack());
//        this.inventory.setStack(40, Items.DIAMOND_HELMET.getDefaultStack());

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

        this.statHandler.fromNbt(nbt, "stats");
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

        this.statHandler.toNbt(nbt, "stats");
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

    @Override
    public double squaredAttackRange(LivingEntity target) {
        return super.squaredAttackRange(target) * 1.5;
    }

    public void attack(LivingEntity target) {
        if (this.world.isClient) return;

        if (target == null) return;
        if (target.isDead()) return;
        if (!target.isAttackable()) return;

        this.getMovementControl().anchorLook(target, false);

        // is within cooldown?
        if (!this.canAttackWithCooldown()) return;

        // check if target wants to handle this attack
        if (target.handleAttack(this)) return;

        var damage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var enchantmentDamage = EnchantmentHelper.getAttackDamage(this.getMainHandStack(), target.getGroup());

        this.resetLastAttackedTicks();
        if (damage <= 0.0F && enchantmentDamage <= 0.0F) return;

        var sprintKnockback = false;
        var i = 0;
        i += EnchantmentHelper.getKnockback(this);
        if (this.isSprinting()) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0F, 1.0F);
            ++i;
            sprintKnockback = true;
        }

        var crit = this.fallDistance > 0.0F && !this.onGround && !this.isClimbing() && !this.isTouchingWater() && !this.hasStatusEffect(StatusEffects.BLINDNESS) && !this.hasVehicle();
        crit = crit && !this.isSprinting();
        if (crit) {
            damage *= 1.5F;
        }

        damage += enchantmentDamage;
        var sweep = false;
        double speedDiff = this.horizontalSpeed - this.prevHorizontalSpeed;
        if (!crit && !sprintKnockback && this.onGround && speedDiff < (double) this.getMovementSpeed()) {
            var itemStack = this.getStackInHand(Hand.MAIN_HAND);
            if (itemStack.getItem() instanceof SwordItem) {
                sweep = true;
            }
        }

        var putOnFire = false;
        var fireAspectLevel = EnchantmentHelper.getFireAspect(this);
        var targetHealth = target.getHealth();
        if (fireAspectLevel > 0 && !target.isOnFire()) {
            putOnFire = true;
            target.setOnFireFor(1);
        }

        var vec3d = target.getVelocity();
        var damageAccepted = target.damage(DamageSource.mob(this), damage);
        if (!damageAccepted) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0F, 1.0F);
            if (putOnFire) {
                target.extinguish();
            }
            return;
        }

        if (i > 0) {
            target.takeKnockback((float) i * 0.5F, MathHelper.sin(this.getYaw() * (float) Math.PI / 180), -MathHelper.cos(this.getYaw() * (float) Math.PI / 180));

            this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
            this.setSprinting(false);
        }

        if (sweep) {
            var l = 1.0F + EnchantmentHelper.getSweepingMultiplier(this) * damage;
            var closeEntities = this.world.getNonSpectatingEntities(LivingEntity.class, target.getBoundingBox().expand(1.0, 0.25, 1.0));
            Iterator<LivingEntity> it = closeEntities.iterator();

            label166:
            while (true) {
                LivingEntity livingEntity;
                do {
                    do {
                        do {
                            do {
                                if (!it.hasNext()) {
                                    this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.0F);
                                    this.spawnSweepAttackParticles();
                                    break label166;
                                }

                                livingEntity = it.next();
                            } while (livingEntity == this);
                        } while (livingEntity == target);
                    } while (this.isTeammate(livingEntity));
                } while (livingEntity instanceof ArmorStandEntity && ((ArmorStandEntity) livingEntity).isMarker());

                if (this.squaredDistanceTo(livingEntity) < 9.0) {
                    livingEntity.takeKnockback(0.4, MathHelper.sin(this.getYaw() * (float) Math.PI / 180), -MathHelper.cos(this.getYaw() * (float) Math.PI / 180));
                    livingEntity.damage(DamageSource.mob(this), l);
                }
            }
        }

        if (target instanceof ServerPlayerEntity && target.velocityModified) {
            ((ServerPlayerEntity) target).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
            target.velocityModified = false;
            target.setVelocity(vec3d);
        }

        if (crit) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
            this.addCritParticles(target);
        }

        if (!crit && !sweep) {
            this.world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0F, 1.0F);
        }

        if (enchantmentDamage > 0.0F) {
            this.addEnchantedHitParticles(target);
        }

        this.onAttacking(target);
        EnchantmentHelper.onUserDamaged(target, this);

        EnchantmentHelper.onTargetDamaged(this, target);
        var itemStack2 = this.getMainHandStack();

        if (!this.world.isClient && !itemStack2.isEmpty()) {
            itemPostHit(itemStack2, target);
            if (itemStack2.isEmpty()) {
                this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            }
        }

        var m = targetHealth - target.getHealth();
        this.increaseStat(Stats.DAMAGE_DEALT, Math.round(m * 10.0F));
        if (fireAspectLevel > 0) {
            target.setOnFireFor(fireAspectLevel * 4);
        }

        if (this.world instanceof ServerWorld && m > 2.0F) {
            var n = (int) ((double) m * 0.5);
            ((ServerWorld) this.world).spawnParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getBodyY(0.5), target.getZ(), n, 0.1, 0.0, 0.1, 0.2);
        }

        this.addExhaustion(0.1F);
    }

    public void addExhaustion(float exhaustion) {
        if (!this.world.isClient) {
            this.hungerManager.addExhaustion(exhaustion);
        }
    }

    private void itemPostHit(ItemStack itemStack, LivingEntity entity) {
        Item item = itemStack.getItem();
        if (item.postHit(itemStack, entity, this)) {
            this.incrementStat(Stats.USED.getOrCreateStat(item));
        }
    }


    public void spawnSweepAttackParticles() {
        double d = -MathHelper.sin(this.getYaw() * (float) Math.PI / 180);
        double e = MathHelper.cos(this.getYaw() * (float) Math.PI / 180);
        if (this.world instanceof ServerWorld) {
            ((ServerWorld) this.world).spawnParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + d, this.getBodyY(0.5), this.getZ() + e, 0, d, 0.0, e, 0.0);
        }
    }

    public void addCritParticles(Entity target) {
        ((ServerChunkManager) this.getWorld().getChunkManager()).sendToNearbyPlayers(this, new EntityAnimationS2CPacket(target, 4));
    }

    public void addEnchantedHitParticles(Entity target) {
        ((ServerChunkManager) this.getWorld().getChunkManager()).sendToNearbyPlayers(this, new EntityAnimationS2CPacket(target, 5));
    }

    public void incrementStat(Stat<?> stat) {
        this.increaseStat(stat, 1);
    }

    public void increaseStat(Identifier stat, int amount) {
        this.increaseStat(Stats.CUSTOM.getOrCreateStat(stat), amount);
    }

    public void increaseStat(Stat<?> stat, int amount) {
        this.statHandler.increaseStat(stat, amount);
    }

    public void resetLastAttackedTicks() {
        this.lastAttackedTicks = 0;
    }

    private boolean canAttackWithCooldown() {
        return this.getAttackCooldownProgress(0.5F) >= 1.0F;
    }

    public float getAttackCooldownProgress(float baseTime) {
        return MathHelper.clamp(((float) this.lastAttackedTicks + baseTime) / this.getAttackCooldownProgressPerTick(), 0.0F, 1.0F);
    }

    public float getAttackCooldownProgressPerTick() {
        return (float) (1.0 / this.getAttackSpeed() * 20.0);
    }

    private double getAttackSpeed() {
        return 1.6d;
    }
}
