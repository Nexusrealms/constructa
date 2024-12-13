package de.nexusrealms.constructa;

import de.nexusrealms.constructa.command.CheckMultiblockCommand;
import de.nexusrealms.constructa.multiblocks.SimpleMultiblock;
import de.nexusrealms.constructa.registry.ConstructaEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Constructa implements ModInitializer {

    public static final Logger LOGGER = Logger.getLogger("constructa");

    @Override
    public void onInitialize() {
        ConstructaEvents.register();
        SimpleMultiblock.register();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CheckMultiblockCommand.register(dispatcher);
        });
    }

    public static Identifier id(String path) {
        return new Identifier("constructa", path);
    }
}
