package de.nexusrealms.constructa;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.fabricmc.api.ModInitializer;

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

		LOGGER.info("Hello Fabric world!");
	}
}