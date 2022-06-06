package br.com.tiozinnub.civilization.registry;

import br.com.tiozinnub.civilization.item.ItemBase;
import net.minecraft.util.registry.Registry;

public class ItemRegistry {

    private static void registerItem(ItemBase item) {
        Registry.register(Registry.ITEM, item.getIdentifier(), item);
    }

    public static void register() {
        
    }
}
