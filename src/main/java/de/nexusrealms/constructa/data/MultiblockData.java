// src/main/java/de/nexusrealms/constructa/data/MultiblockData.java
package de.nexusrealms.constructa.data;

import com.google.gson.annotations.SerializedName;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Map;

public class MultiblockData {
    private String name;
    private char[][][] pattern;
    @SerializedName("block_mappings")
    private Map<Character, String> blockMappings;
    private boolean preview;

    public String getName() {
        return name;
    }

    public char[][][] getPattern() {
        return pattern;
    }

    public Map<Character, String> getBlockMappings() {
        return blockMappings;
    }

    public boolean shouldPreview() {
        return preview;
    }

    public Block getBlock(char key) {
        String blockId = blockMappings.get(key);
        return Registries.BLOCK.get(new Identifier(blockId));
    }
}