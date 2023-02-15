package br.com.tiozinnub.civilization.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CommandRegistry {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(CivilizationCommand::register);
    }
}
