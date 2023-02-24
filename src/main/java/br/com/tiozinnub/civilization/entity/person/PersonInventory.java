package br.com.tiozinnub.civilization.entity.person;

import br.com.tiozinnub.civilization.utils.Serializable;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;

public class PersonInventory extends Serializable implements Inventory {

    private static final int MAIN_SIZE = 36;
    private static final int OFFHAND_SIZE = 1;
    private static final int ARMOR_SIZE = 4;
    public static final int INVENTORY_SIZE = MAIN_SIZE + OFFHAND_SIZE + ARMOR_SIZE;
    public static final int FEET_SLOT = INVENTORY_SIZE - 4;
    public static final int LEGS_SLOT = INVENTORY_SIZE - 3;
    public static final int CHEST_SLOT = INVENTORY_SIZE - 2;
    public static final int HEAD_SLOT = INVENTORY_SIZE - 1;
    public static final int[] ARMOR_SLOTS = new int[]{FEET_SLOT, LEGS_SLOT, CHEST_SLOT, HEAD_SLOT};
    private final PersonEntity person;
    private final DefaultedList<ItemStack> inventory;
    public int selectedSlot = 0;
    public int selectedOffhandSlot = 1;
    private int changeCount;

    public PersonInventory(PersonEntity person) {
        this.inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        this.person = person;
    }

    public int getChangeCount() {
        return changeCount;
    }

    @Override
    public int size() {
        return MAIN_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (var itemStack : this.inventory) {
            if (!itemStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return inventory.get(slot).isEmpty() ? Inventories.splitStack(this.inventory, slot, amount) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        if (this.inventory.get(slot).isEmpty()) {
            return ItemStack.EMPTY;
        }

        var itemStack = this.inventory.get(slot);
        this.inventory.set(slot, ItemStack.EMPTY);
        this.markDirty();
        return itemStack;

    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        this.markDirty();
    }

    @Override
    public void markDirty() {
        ++this.changeCount;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return !player.isRemoved();
    }

    @Override
    public void clear() {
        this.inventory.clear();
        this.markDirty();
    }

    public void tick() {
        for (var i = 0; i < this.inventory.size(); i++) {
            var itemStack = this.inventory.get(i);
            if (itemStack.isEmpty()) continue;
            itemStack.inventoryTick(this.person.getWorld(), this.person, i, this.selectedSlot == i);
        }
        this.markDirty();
    }

    public void dropAll() {
        for (var i = 0; i < this.inventory.size(); i++) {
            var itemStack = this.inventory.get(i);
            if (itemStack.isEmpty()) continue;
            this.person.dropItem(itemStack, true, false);
            this.inventory.set(i, ItemStack.EMPTY);
            this.markDirty();
        }
    }

    @Override
    public void registerProperties(SerializableHelper helper) {
        helper.registerProperty("inventory", this::getInventoryNbt, this::setInventoryNbt, null);
        helper.registerProperty("selectedSlot", () -> this.selectedSlot, (value) -> this.selectedSlot = value, 0);
        helper.registerProperty("selectedOffhandSlot", () -> this.selectedOffhandSlot, (value) -> this.selectedOffhandSlot = value, 1);
    }

    private NbtCompound getInventoryNbt() {
        var nbt = new NbtCompound();

        for (var i = 0; i < this.inventory.size(); i++) {
            var itemStack = this.inventory.get(i);
            if (itemStack.isEmpty()) continue;
            var itemNbt = new NbtCompound();
            itemStack.writeNbt(itemNbt);
            nbt.put(String.valueOf(i), itemNbt);
        }

        return nbt;
    }

    private void setInventoryNbt(NbtCompound nbt) {
        for (var key : nbt.getKeys()) {
            var itemNbt = nbt.getCompound(key);
            var itemStack = ItemStack.fromNbt(itemNbt);
            this.inventory.set(Integer.parseInt(key), itemStack);
            this.markDirty();
        }
    }

    public void damageArmor(DamageSource source, float amount, int[] slots) {
        if (amount <= 0.0F) return;

        amount /= 4.0F;
        if (amount < 1.0F) amount = 1.0F;

        for (var slot : slots) {
            var itemStack = this.inventory.get(slot);

            var equipmentSlot = switch (slot) {
                case FEET_SLOT -> EquipmentSlot.FEET;
                case LEGS_SLOT -> EquipmentSlot.LEGS;
                case CHEST_SLOT -> EquipmentSlot.CHEST;
                case HEAD_SLOT -> EquipmentSlot.HEAD;
                default -> throw new IllegalStateException("Unexpected value: " + slot);
            };

            if (source.isFire() && itemStack.getItem().isFireproof()) continue;

            if (itemStack.getItem() instanceof ArmorItem) {
                itemStack.damage((int) amount, this.person, (person) -> person.sendEquipmentBreakStatus(equipmentSlot));
            }
        }
    }

    public ItemStack getStackAtEquipmentSlot(EquipmentSlot slot) {
        return switch (slot) {
            case FEET -> this.inventory.get(FEET_SLOT);
            case LEGS -> this.inventory.get(LEGS_SLOT);
            case CHEST -> this.inventory.get(CHEST_SLOT);
            case HEAD -> this.inventory.get(HEAD_SLOT);
            case MAINHAND -> this.inventory.get(this.selectedSlot);
            case OFFHAND -> this.inventory.get(this.selectedOffhandSlot);
        };
    }


    public boolean insertStack(ItemStack stack, boolean allowArmor) {
        return insertStack(stack, -1, allowArmor);
    }

    private boolean canStackAddMore(ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty() && ItemStack.canCombine(existingStack, stack) && existingStack.isStackable() && existingStack.getCount() < existingStack.getMaxCount() && existingStack.getCount() < this.getMaxCountPerStack();
    }

    public int getOccupiedSlotWithRoomForStack(ItemStack stack, boolean allowArmor) {
        if (this.canStackAddMore(this.getStack(this.selectedSlot), stack)) {
            return this.selectedSlot;
        } else if (this.canStackAddMore(this.getStack(40), stack)) {
            return 40;
        } else {
            for (var i = 0; i < this.inventory.size(); ++i) {
                if (!allowArmor && (i == FEET_SLOT || i == LEGS_SLOT || i == CHEST_SLOT || i == HEAD_SLOT)) continue;
                if (this.canStackAddMore(this.inventory.get(i), stack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    private int addStack(ItemStack stack, boolean allowArmor) {
        var i = this.getOccupiedSlotWithRoomForStack(stack, allowArmor);
        if (i == -1) i = this.getEmptySlot(allowArmor);

        return i == -1 ? stack.getCount() : this.addStack(i, stack);
    }

    private int addStack(int slot, ItemStack stack) {
        var item = stack.getItem();
        var itemCount = stack.getCount();
        var itemStack = this.getStack(slot);
        if (itemStack.isEmpty()) {
            itemStack = new ItemStack(item, 0);
            if (stack.hasNbt()) {
                itemStack.setNbt(stack.getNbt().copy());
            }

            this.setStack(slot, itemStack);
        }

        var j = Math.min(itemCount, itemStack.getMaxCount() - itemStack.getCount());

        if (j > this.getMaxCountPerStack() - itemStack.getCount()) {
            j = this.getMaxCountPerStack() - itemStack.getCount();
        }

        if (j != 0) {
            itemCount -= j;
            itemStack.increment(j);
            itemStack.setBobbingAnimationTime(5);
        }

        return itemCount;
    }

    public boolean insertStack(ItemStack stack, int slot, boolean allowArmor) {
        if (stack.isEmpty()) return false;

        try {
            if (stack.isDamaged()) {
                if (slot == -1) {
                    slot = this.getEmptySlot(allowArmor);
                }

                if (slot >= 0) {
                    this.inventory.set(slot, stack.copy());
                    this.markDirty();
                    this.inventory.get(slot).setBobbingAnimationTime(5);
                    stack.setCount(0);
                    return true;
                } else {
                    return false;
                }
            } else {
                int i;
                do {
                    i = stack.getCount();
                    if (slot == -1) {
                        stack.setCount(this.addStack(stack, allowArmor));
                    } else {
                        stack.setCount(this.addStack(slot, stack));
                    }
                } while (!stack.isEmpty() && stack.getCount() < i);

                return stack.getCount() < i;
            }
        } catch (Throwable var6) {
            var crashReport = CrashReport.create(var6, "Adding item to person inventory");
            var crashReportSection = crashReport.addElement("Item being added");
            crashReportSection.add("Item ID", Item.getRawId(stack.getItem()));
            crashReportSection.add("Item data", stack.getDamage());
            crashReportSection.add("Item name", () -> stack.getName().getString());
            throw new CrashException(crashReport);
        }
    }

    private int getEmptySlot(boolean allowArmor) {
        for (var i = 0; i < INVENTORY_SIZE; i++) {
            if (!allowArmor && (i == FEET_SLOT || i == LEGS_SLOT || i == CHEST_SLOT || i == HEAD_SLOT)) continue;
            if (this.inventory.get(i).isEmpty()) return i;
        }

        return -1;
    }

    public NbtCompound getEquippedItemsAsNbt() {
        var nbt = new NbtCompound();

        nbt.put(EquipmentSlot.FEET.getName(), this.inventory.get(FEET_SLOT).writeNbt(new NbtCompound()));
        nbt.put(EquipmentSlot.LEGS.getName(), this.inventory.get(LEGS_SLOT).writeNbt(new NbtCompound()));
        nbt.put(EquipmentSlot.CHEST.getName(), this.inventory.get(CHEST_SLOT).writeNbt(new NbtCompound()));
        nbt.put(EquipmentSlot.HEAD.getName(), this.inventory.get(HEAD_SLOT).writeNbt(new NbtCompound()));
        nbt.put(EquipmentSlot.MAINHAND.getName(), this.inventory.get(this.selectedSlot).writeNbt(new NbtCompound()));
        nbt.put(EquipmentSlot.OFFHAND.getName(), this.inventory.get(this.selectedOffhandSlot).writeNbt(new NbtCompound()));

        return nbt;
    }

    public boolean isDirty() {
        return this.changeCount > 0;
    }

    public void setClean() {
        this.changeCount = 0;
    }
}
