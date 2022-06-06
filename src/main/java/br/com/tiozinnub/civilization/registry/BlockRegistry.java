package br.com.tiozinnub.civilization.registry;

import br.com.tiozinnub.civilization.block.BlockBase;
import br.com.tiozinnub.civilization.block.BlueprintTableBlock;
import br.com.tiozinnub.civilization.block.CityCenterBlock;
import net.minecraft.util.registry.Registry;

public class BlockRegistry {
    public static final CityCenterBlock CITY_CENTER_BLOCK = new CityCenterBlock();
    public static final BlueprintTableBlock BLUEPRINT_TABLE_BLOCK = new BlueprintTableBlock();

    private static void registerBlock(BlockBase block) {
        Registry.register(Registry.BLOCK, block.getIdentifier(), block);
        Registry.register(Registry.ITEM, block.getIdentifier(), block.asBlockItem());
    }

    public static void register() {
        registerBlock(CITY_CENTER_BLOCK);
        registerBlock(BLUEPRINT_TABLE_BLOCK);
    }
}
