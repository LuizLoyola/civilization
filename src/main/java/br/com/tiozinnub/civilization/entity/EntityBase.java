package br.com.tiozinnub.civilization.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public abstract class EntityBase extends LivingEntity {

    protected EntityBase(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return LivingEntity.createLivingAttributes();
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        // TODO: Implement
    }

    public abstract String getNameId();

    public Identifier getTexture() {
        return idFor("textures/entity/%s.png".formatted(getNameId()));
    }

    public List<Text> getMultilineNameplate() {
        return List.of(getName());
    }

    @Override
    public void setCustomName(@Nullable Text name) {
        // ignore
    }
}
