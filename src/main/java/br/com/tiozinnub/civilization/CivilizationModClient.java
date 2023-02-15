package br.com.tiozinnub.civilization;

import br.com.tiozinnub.civilization.block.BlockEntities;
import br.com.tiozinnub.civilization.entity.Entities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CivilizationModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntities.registerClient();
        Entities.registerClient();
    }
}
