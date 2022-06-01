package br.com.tiozinnub.civilization.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;

public abstract class BlockBase extends Block {
    public BlockBase(Settings settings) {
        super(settings);
    }

    public abstract Identifier getIdentifier();

    public abstract ItemGroup getItemGroup();

    public BlockItem asBlockItem() {
        return new BlockItem(this, new FabricItemSettings().group(this.getItemGroup()));
    }
}
