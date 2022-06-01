package br.com.tiozinnub.civilization;

import br.com.tiozinnub.civilization.registry.*;
import br.com.tiozinnub.civilization.utils.Constraints;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class CivilizationMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Constraints.MOD_ID);

    @Override
    public void onInitialize() {
        GeckoLib.initialize();

        ItemRegistry.register();
        BlockRegistry.register();
        BlockEntityRegistry.register();
        EntityRegistry.register();

        CommandRegistry.register();
    }
}
