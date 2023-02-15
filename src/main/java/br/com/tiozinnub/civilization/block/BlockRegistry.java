package br.com.tiozinnub.civilization.block;

import br.com.tiozinnub.civilization.block.building.citycenter.CityCenterBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static br.com.tiozinnub.civilization.utils.Constraints.idFor;

public class BlockRegistry {
    public static final CityCenterBlock CITY_CENTER_BLOCK = new CityCenterBlock();

    public static void register() {
        Registry.register(Registries.BLOCK, idFor("city_center_block"), CITY_CENTER_BLOCK);
    }
}
