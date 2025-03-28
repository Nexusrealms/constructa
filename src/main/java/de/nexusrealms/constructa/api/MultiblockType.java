package de.nexusrealms.constructa.api;

import com.mojang.serialization.MapCodec;
import de.nexusrealms.constructa.Constructa;
import de.nexusrealms.constructa.ConstructaRegistries;
import de.nexusrealms.constructa.PatternMultiblock;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public record MultiblockType<T extends Multiblock>(MapCodec<T> codec) {
    public static final MultiblockType<PatternMultiblock> PATTERN = create("pattern", PatternMultiblock.CODEC);
    private static <T extends Multiblock> MultiblockType<T> create(String name, MapCodec<T> codec){
        return Registry.register(ConstructaRegistries.MULTIBLOCK_TYPES, Identifier.of(Constructa.MOD_ID, name), new MultiblockType<>(codec));
    }
    public static void init(){}
}
