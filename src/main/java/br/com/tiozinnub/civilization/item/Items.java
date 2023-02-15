package br.com.tiozinnub.civilization.item;

import br.com.tiozinnub.civilization.item.debug.PathWandItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class Items {
    public static final PathWandItem PATH_WAND = new PathWandItem();

    public static void register() {
        Registry.register(Registries.ITEM, idFor("path_wand"), PATH_WAND);
    }
}
