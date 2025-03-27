package de.nexusrealms.constructa;

import com.google.gson.JsonParser;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.LookingPosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constructa implements ModInitializer {
	public static final String MOD_ID = "constructa";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Codec<Character> CHARACTER_CODEC = Codec.STRING.comapFlatMap(s -> s.length() == 1 ? DataResult.success(s.charAt(0)) : DataResult.error(() -> "String is not a valid char"), String::valueOf);
	private static final String MULTIBLOCK_DEFINITION = """
			{
			  "pattern": [
			    [
			      "A#B",
			      "###",
			      "C#D"
			    ],
			    [
			      "###",
			      "###",
			      "###"
			    ],
			    [
			      "E#F",
			      "###",
			      "G#H"
			    ]
			  ],
			  "chars": {
			    "#": "minecraft:deepslate",
			    "A": "minecraft:stone",
			    "B": "minecraft:dirt",
			    "C": "minecraft:grass_block",
			    "D": "minecraft:sand",
			    "E": "minecraft:cobbled_deepslate",
			    "F": "minecraft:iron_block",
			    "G": "minecraft:gold_block",
			    "H": "minecraft:diamond_block"
			  }
			}
			""";
	private PatternMultiblock multiblock;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
			multiblock = PatternMultiblock.CODEC.parse(minecraftServer.getRegistryManager().getOps(JsonOps.INSTANCE), JsonParser.parseString(MULTIBLOCK_DEFINITION)).getOrThrow();
		});
		CommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
			commandDispatcher.register(CommandManager.literal("testmultiblock")
					.requires(ServerCommandSource::isExecutedByPlayer)
					.executes(commandContext -> {
						multiblock.construct(commandContext.getSource().getWorld(), commandContext.getSource().getPlayer().getBlockPos());
						return 1;
					}));
			commandDispatcher.register(CommandManager.literal("testmultiblock2")
					.requires(ServerCommandSource::isExecutedByPlayer)
					.executes(commandContext -> {
						multiblock.constructFromPattern(commandContext.getSource().getWorld(), commandContext.getSource().getPlayer().getBlockPos());
						return 1;
					}));
			commandDispatcher.register(CommandManager.literal("testmultiblock3")
					.then(CommandManager.argument("where", BlockPosArgumentType.blockPos())
							.requires(ServerCommandSource::isExecutedByPlayer)
							.executes(commandContext -> {
								LOGGER.info(String.valueOf(multiblock.wasFound(commandContext.getSource().getWorld(), BlockPosArgumentType.getBlockPos(commandContext, "where"))));
								return 1;
							})));
		});
		LOGGER.info("Hello Fabric world!");
	}
}