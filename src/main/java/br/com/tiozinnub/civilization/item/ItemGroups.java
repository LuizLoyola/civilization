package br.com.tiozinnub.civilization.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class ItemGroups {
    public static final ItemGroup CIVILIZATION_DEBUG = FabricItemGroup.builder(idFor("civilization_debug"))
            .displayName(Text.of("Civilization - Debug"))
            .icon(() -> new ItemStack(Items.PATH_WAND))
            .build();


    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CIVILIZATION_DEBUG).register(entries -> {
            entries.add(Items.PATH_WAND);
        });
    }
}
