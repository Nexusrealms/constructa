package de.nexusrealms.constructa.multiblocks;

import de.nexusrealms.constructa.api.multiblock.Multiblock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.function.Predicate;

public class SimpleMultiblock {
    private static final char[][][] PATTERN = {
        {
            {'#', '#', '#'},
            {'#', '$', '#'},
            {'#', '#', '#'}
        }
    };

    private static final HashMap<Character, Predicate<BlockState>> PREDICATES = new HashMap<>();

    static {
        PREDICATES.put('#', BlockStatePredicate.forBlock(Blocks.STONE));
        PREDICATES.put('$', BlockStatePredicate.forBlock(Blocks.DIAMOND_BLOCK));
    }

    public static void register() {
        Multiblock multiblock = new Multiblock(PATTERN, PREDICATES, true);
        // Register the multiblock in your mod's registry system
        // This is a placeholder, replace with actual registration code if needed
    }

    public static boolean checkMultiblock(World world, BlockPos pos) {
        Multiblock multiblock = new Multiblock(PATTERN, PREDICATES, true);
        return multiblock.check(pos, world);
    }
}