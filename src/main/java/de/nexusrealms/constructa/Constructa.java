package de.nexusrealms.constructa;

import com.google.gson.JsonParser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.nexusrealms.constructa.api.Multiblock;
import de.nexusrealms.constructa.api.MultiblockType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.LookingPosArgument;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Constructa implements ModInitializer {
    public static final String MOD_ID = "constructa";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Codec<Character> CHARACTER_CODEC = Codec.STRING.comapFlatMap(s -> s.length() == 1 ? DataResult.success(s.charAt(0)) : DataResult.error(() -> "String is not a valid char"), String::valueOf);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        ConstructaRegistries.init();
        MultiblockType.init();
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(CommandManager.literal("multiblock")
                    .then(CommandManager.argument("multiblock", RegistryEntryReferenceArgumentType.registryEntry(commandRegistryAccess, ConstructaRegistries.Keys.MULTIBLOCKS))
                            .then(CommandManager.literal("place")
                                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                            .requires(source -> source.hasPermissionLevel(2))
                                            .executes(commandContext -> {
                                                Multiblock multiblock = RegistryEntryReferenceArgumentType.getRegistryEntry(commandContext, "multiblock", ConstructaRegistries.Keys.MULTIBLOCKS).value();
                                                multiblock.construct(commandContext.getSource().getWorld(), BlockPosArgumentType.getBlockPos(commandContext, "pos"));
                                                commandContext.getSource().sendFeedback(() -> Text.translatable("message.multiblock.place"), false);
                                                return 1;
                                            })))
                            .then(CommandManager.literal("check")
                                    .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                            .requires(source -> source.hasPermissionLevel(2))
                                            .executes(commandContext -> {
                                                Multiblock multiblock = RegistryEntryReferenceArgumentType.getRegistryEntry(commandContext, "multiblock", ConstructaRegistries.Keys.MULTIBLOCKS).value();
                                                boolean bool = multiblock.wasFound(commandContext.getSource().getWorld(), BlockPosArgumentType.getBlockPos(commandContext, "pos"));
                                                commandContext.getSource().sendFeedback(() -> bool ? Text.translatable("message.multiblock.found") : Text.translatable("message.multiblock.notfound"), false);
                                                return 1;
                                            }))))
            );
        });


        LOGGER.info("Hello Fabric world!");
    }
}