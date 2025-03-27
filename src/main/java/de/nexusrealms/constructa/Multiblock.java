package de.nexusrealms.constructa;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public interface Multiblock {
    void construct(ServerWorld world, BlockPos frontTopLeft);
    boolean wasFound(WorldView world, BlockPos searchPos);
}
