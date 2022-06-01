package br.com.tiozinnub.civilization.registry;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class ItemGroupRegistry {
    public static final ItemGroup SPECIAL_ITEMS = FabricItemGroupBuilder.build(idFor("special_items"), () -> new ItemStack(ItemRegistry.BLUEPRINT));
    public static final ItemGroup SPECIAL_BLOCKS = FabricItemGroupBuilder.build(idFor("special_blocks"), () -> new ItemStack(Items.OAK_LOG));
}
