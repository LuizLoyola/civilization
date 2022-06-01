package br.com.tiozinnub.civilization.registry;

import br.com.tiozinnub.civilization.command.CivilizationCommand;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class CommandRegistry {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(CivilizationCommand::register);
    }
}
