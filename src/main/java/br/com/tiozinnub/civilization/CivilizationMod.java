package br.com.tiozinnub.civilization;

import br.com.tiozinnub.civilization.registry.BlockEntityRegistry;
import br.com.tiozinnub.civilization.registry.BlockRegistry;
import br.com.tiozinnub.civilization.registry.EntityRegistry;
import br.com.tiozinnub.civilization.registry.ItemRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class CivilizationMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(CivilizationMod.MOD_ID);
    public static final String MOD_ID = "civilization";

    @Override
    public void onInitialize() {
        GeckoLib.initialize();

        ItemRegistry.register();
        BlockRegistry.register();
        BlockEntityRegistry.register();
        EntityRegistry.register();
    }
}
