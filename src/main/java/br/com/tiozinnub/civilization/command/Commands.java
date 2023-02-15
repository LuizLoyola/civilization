package br.com.tiozinnub.civilization.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(CivilizationCommand::register);
    }
}
