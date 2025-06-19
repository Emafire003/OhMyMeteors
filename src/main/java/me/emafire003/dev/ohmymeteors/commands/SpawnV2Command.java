package me.emafire003.dev.ohmymeteors.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.emafire003.dev.ohmymeteors.entities.MeteorProjectileEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SpawnV2Command implements OMMCommand {

    private int reloadConfig(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try{
            ServerCommandSource source = context.getSource();
            if(!source.isExecutedByPlayer()){
                source.sendMessage(Text.literal("Must be executed by player"));
                return 0;
            }

            MeteorProjectileEntity meteorProjectile = new MeteorProjectileEntity(source.getWorld());

            meteorProjectile.setVelocity(source.getPlayer(), source.getPlayer().getPitch(), source.getPlayer().getYaw(), 0f, 1.5f, 0f);

            meteorProjectile.setSize(20);

            source.getWorld().spawnEntity(meteorProjectile);
            return 1;
        }catch (Exception e){
            context.getSource().sendError(Text.literal("hi ").append("§cThere has been an error while reloading the config, check the logs"));
            e.printStackTrace();
            return 0;
        }

    }




    public LiteralCommandNode<ServerCommandSource> getNode() {
        return CommandManager
                .literal("reload")
                .executes(this::reloadConfig)
                .build();
    }

    @Override
    public LiteralCommandNode<ServerCommandSource> getNode(CommandRegistryAccess registryAccess) {
        return CommandManager
                .literal("reload")
                .executes(this::reloadConfig)
                .build();
    }
}
