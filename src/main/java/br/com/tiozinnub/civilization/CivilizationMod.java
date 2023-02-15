package br.com.tiozinnub.civilization;

import br.com.tiozinnub.civilization.block.BlockRegistry;
import br.com.tiozinnub.civilization.block.entity.BlockEntityRegistry;
import br.com.tiozinnub.civilization.command.CommandRegistry;
import br.com.tiozinnub.civilization.config.ConfigRegistry;
import br.com.tiozinnub.civilization.entity.EntityRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static br.com.tiozinnub.civilization.utils.Constraints.MOD_ID;

public class CivilizationMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ConfigRegistry.register();

        CommandRegistry.register();

        BlockRegistry.register();
        BlockEntityRegistry.register();
        EntityRegistry.register();
    }
}
