package br.com.tiozinnub.civilization;

import br.com.tiozinnub.civilization.registry.EntityRegistry;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static br.com.tiozinnub.civilization.util.Constraints.MOD_ID;

public class CivilizationMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		EntityRegistry.register();
	}
}
