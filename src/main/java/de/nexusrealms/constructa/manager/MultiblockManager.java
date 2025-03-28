// src/main/java/de/nexusrealms/constructa/manager/MultiblockManager.java
package de.nexusrealms.constructa.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.nexusrealms.constructa.Constructa;
import de.nexusrealms.constructa.data.MultiblockData;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static com.mojang.text2speech.Narrator.LOGGER;

public class MultiblockManager implements SimpleSynchronousResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, MultiblockData> MULTIBLOCKS = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return Constructa.id("multiblocks");
    }

    @Override
    public void reload(ResourceManager manager) {
        MULTIBLOCKS.clear();

        for (Map.Entry<Identifier, Resource> entry : manager.findResources("multiblocks", path -> path.getPath().endsWith(".json")).entrySet()) {
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().getInputStream())) {
                MultiblockData data = GSON.fromJson(reader, MultiblockData.class);
                MULTIBLOCKS.put(data.getName(), data);
            } catch (Exception e) {
                LOGGER.error("Error loading multiblock " + entry.getKey(), e);
            }
        }
    }

    public static MultiblockData getMultiblock(String name) {
        return MULTIBLOCKS.get(name);
    }
}