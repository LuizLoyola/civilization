package br.com.tiozinnub.civilization.entity.person;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class PersonInventory implements Inventory {

    private static final int MAIN_SIZE = 36;
    private static final int OFFHAND_SIZE = 1;
    private static final int ARMOR_SIZE = 4;
    public static final int INVENTORY_SIZE = MAIN_SIZE + OFFHAND_SIZE + ARMOR_SIZE;
    private final PersonEntity person;
    private final DefaultedList<ItemStack> inventory;
    public int selectedSlot;
    private int changeCount;

    public PersonInventory(PersonEntity person) {
        this.inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
        this.person = person;
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
        return itemStack;

    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
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
    }
}
