package br.com.tiozinnub.civilization.item.debug;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class PathWandItem extends Item {
    public PathWandItem() {
        super(
                new FabricItemSettings()
                        .maxCount(1)
        );
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
