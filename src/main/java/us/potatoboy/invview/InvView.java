package us.potatoboy.invview;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvView implements ModInitializer {
    private static MinecraftServer minecraftServer;
    public static boolean isTrinkets = false;
    public static boolean isLuckPerms = false;
    public static boolean isApoli = false;

    @Override
    public void onInitialize() {
        isTrinkets = FabricLoader.getInstance().isModLoaded("trinkets");
        isLuckPerms = FabricLoader.getInstance().isModLoaded("luckperms");
        isApoli = FabricLoader.getInstance().isModLoaded("apoli");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            LiteralCommandNode<ServerCommandSource> viewNode = CommandManager
                    .literal("view")
                    .requires(Permissions.require("invview.command.root", 2))
                    .build();

            LiteralCommandNode<ServerCommandSource> invNode = CommandManager
                    .literal("inv")
                    .requires(Permissions.require("invview.command.inv", 2))
                    .then(CommandManager.argument("target", GameProfileArgumentType.gameProfile())
                            .executes(ViewCommand::inv))
                    .build();

            LiteralCommandNode<ServerCommandSource> echestNode = CommandManager
                    .literal("echest")
                    .requires(Permissions.require("invview.command.echest", 2))
                    .then(CommandManager.argument("target", GameProfileArgumentType.gameProfile())
                            .executes(ViewCommand::eChest))
                    .build();

//            LiteralCommandNode<ServerCommandSource> trinketNode = CommandManager
//                    .literal("trinket")
//                    .requires(Permissions.require("invview.command.trinket", 2))
//                    .then(CommandManager.argument("target", GameProfileArgumentType.gameProfile())
//                            .executes(ViewCommand::trinkets))
//                    .build();
//
//            LiteralCommandNode<ServerCommandSource> apoliNode = CommandManager
//                    .literal("origin-inv")
//                    .requires(Permissions.require("invview.command.origin", 2))
//                    .then(CommandManager.argument("target", GameProfileArgumentType.gameProfile())
//                            .executes(ViewCommand::apoli))
//                    .build();

            dispatcher.getRoot().addChild(viewNode);
            viewNode.addChild(invNode);
            viewNode.addChild(echestNode);

            if (isTrinkets) {
//                viewNode.addChild(trinketNode);
            }
            if (isApoli) {
//                viewNode.addChild(apoliNode);
            }
        });

        ServerLifecycleEvents.SERVER_STARTING.register(this::onLogicalServerStarting);
    }

    private void onLogicalServerStarting(MinecraftServer server) {
        minecraftServer = server;
    }

    public static MinecraftServer getMinecraftServer() {
        return minecraftServer;
    }

    // Taken from net.minecraft.world.PlayerSaveHandler.savePlayerData(), which is a protected method
    public static void savePlayerData(ServerPlayerEntity player) {
       File playerDataDir = minecraftServer.getSavePath(WorldSavePath.PLAYERDATA).toFile();
       	try (ErrorReporter.Logging logging = new ErrorReporter.Logging(player.getErrorReporterContext(), LogUtils.getLogger())) {
			NbtWriteView nbtWriteView = NbtWriteView.create(logging, player.getRegistryManager());
			player.writeData(nbtWriteView);
			Path path = playerDataDir.toPath();
			Path path2 = Files.createTempFile(path, player.getUuidAsString() + "-", ".dat");
			NbtCompound nbtCompound = nbtWriteView.getNbt();
			NbtIo.writeCompressed(nbtCompound, path2);
			Path path3 = path.resolve(player.getUuidAsString() + ".dat");
			Path path4 = path.resolve(player.getUuidAsString() + ".dat_old");
			Util.backupAndReplace(path3, path2, path4);
		} catch (Exception var11) {
			LogUtils.getLogger().warn("Failed to save player data for {}", player.getName().getString());
		}
   }
}
