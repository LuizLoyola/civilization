package br.com.tiozinnub.civilization;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CivilizationMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("civilization");

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
	}
}
