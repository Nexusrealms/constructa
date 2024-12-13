package de.nexusrealms.constructa.registry;

import de.nexusrealms.constructa.api.multiblock.Multiblock;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class ConstructaEvents {
    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            Multiblock.MultiblockPreviewElement.safeMatchCheck(world, pos);
        });
    }
}
