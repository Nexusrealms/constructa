// src/main/java/de/nexusrealms/constructa/multiblocks/SimpleMultiblock.java
package de.nexusrealms.constructa.multiblocks;

import de.nexusrealms.constructa.api.multiblock.Multiblock;
import de.nexusrealms.constructa.data.MultiblockData;
import de.nexusrealms.constructa.manager.MultiblockManager;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.function.Predicate;

public class SimpleMultiblock {
    public static void register() {
        // Registration now handled by MultiblockManager
    }

    public static boolean checkMultiblock(World world, BlockPos pos) {
        MultiblockData data = MultiblockManager.getMultiblock("simple_multiblock");
        if (data == null) return false;

        HashMap<Character, Predicate<BlockState>> predicates = new HashMap<>();
        data.getBlockMappings().forEach((key, value) ->
            predicates.put(key, BlockStatePredicate.forBlock(data.getBlock(key)))
        );

        Multiblock multiblock = new Multiblock(data.getPattern(), predicates, data.shouldPreview());
        return multiblock.check(pos, world);
    }
}