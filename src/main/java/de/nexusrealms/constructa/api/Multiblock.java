package de.nexusrealms.constructa.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import de.nexusrealms.constructa.ConstructaRegistries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public interface Multiblock {
    Codec<Multiblock> DIRECT_CODEC = ConstructaRegistries.MULTIBLOCK_TYPES.getCodec().dispatch(Multiblock::getType, MultiblockType::codec);
    Codec<Multiblock> CODEC = Codec.withAlternative(RegistryFixedCodec.of(ConstructaRegistries.Keys.MULTIBLOCKS).flatComapMap(RegistryEntry::value, m -> DataResult.error(() -> "you cannot do this try the other codec")), DIRECT_CODEC);
    MultiblockType<?> getType();
    void construct(ServerWorld world, BlockPos frontTopLeft);
    boolean wasFound(WorldView world, BlockPos searchPos);
}
