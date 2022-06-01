package br.com.tiozinnub.civilization;

import br.com.tiozinnub.civilization.registry.EntityRegistry;
import net.fabricmc.api.ClientModInitializer;

public class CivilizationModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRegistry.registerClient();
    }
}
