package br.com.tiozinnub.civilization;

import br.com.tiozinnub.civilization.block.BlockEntities;
import br.com.tiozinnub.civilization.block.Blocks;
import br.com.tiozinnub.civilization.command.Commands;
import br.com.tiozinnub.civilization.config.Configs;
import br.com.tiozinnub.civilization.entity.Entities;
import br.com.tiozinnub.civilization.item.ItemGroups;
import br.com.tiozinnub.civilization.item.Items;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static br.com.tiozinnub.civilization.utils.Constraints.MOD_ID;

public class CivilizationMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        Configs.register();

        Commands.register();

        Blocks.register();
        BlockEntities.register();
        Entities.register();

        Items.register();
        ItemGroups.register();
    }
}
