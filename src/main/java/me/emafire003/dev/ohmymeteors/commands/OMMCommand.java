package me.emafire003.dev.ohmymeteors.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;


//Based on Factions' code https://github.com/ickerio/factions (MIT license)
public interface OMMCommand {
    LiteralCommandNode<ServerCommandSource> getNode(CommandRegistryAccess registryAccess);

}
