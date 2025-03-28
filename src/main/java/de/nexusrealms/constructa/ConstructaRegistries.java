package de.nexusrealms.constructa;

import de.nexusrealms.constructa.api.Multiblock;
import de.nexusrealms.constructa.api.MultiblockType;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class ConstructaRegistries {
    public static final Registry<MultiblockType<?>> MULTIBLOCK_TYPES = createSimple(Keys.MULTIBLOCK_TYPES);
    private static <T> Registry<T> createSimple(RegistryKey<Registry<T>> registryKey){
        return FabricRegistryBuilder.createSimple(registryKey).buildAndRegister();
    }
    public static void init(){
        DynamicRegistries.registerSynced(Keys.MULTIBLOCKS, Multiblock.DIRECT_CODEC);
    }
    public static class Keys {
        public static final RegistryKey<Registry<Multiblock>> MULTIBLOCKS = RegistryKey.ofRegistry(Identifier.of(Constructa.MOD_ID, "multiblock"));
        public static final RegistryKey<Registry<MultiblockType<?>>> MULTIBLOCK_TYPES = RegistryKey.ofRegistry(Identifier.of(Constructa.MOD_ID, "multiblock_type"));

    }
}
