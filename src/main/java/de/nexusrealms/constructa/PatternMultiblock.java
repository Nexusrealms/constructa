package de.nexusrealms.constructa;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PatternMultiblock implements Multiblock {

    public static final Codec<PatternMultiblock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().listOf().fieldOf("pattern").forGetter(PatternMultiblock::pattern),
            Codec.unboundedMap(Constructa.CHARACTER_CODEC, BlockPositionPredicate.CODEC).fieldOf("chars").forGetter(PatternMultiblock::chars)
    ).apply(instance, PatternMultiblock::new));
    //List of layers of Z rows of X strings
    //So iterates YZX
    private final List<List<String>> pattern;
    private final Map<Character, BlockPositionPredicate> chars;

    public List<List<String>> pattern() {
        return pattern;
    }

    public Map<Character, BlockPositionPredicate> chars() {
        return chars;
    }
    private final BlockPattern transformed;
    private final int width, height, depth;
    public PatternMultiblock(List<List<String>> pattern, Map<Character, BlockPositionPredicate> chars) {
        this.pattern = pattern;
        this.chars = chars;
        transformed = toPattern();
        this.width = transformed.getWidth();
        this.height = transformed.getHeight();
        this.depth = transformed.getDepth();
    }
    @Override
    public boolean wasFound(WorldView world, BlockPos searchPos) {
        return transformed.searchAround(world, searchPos) != null;
    }
    public void construct(ServerWorld world, BlockPos frontTopLeft){
        List<List<List<BlockState>>> states = pattern.stream().map(layer -> layer.stream().map(row -> row.chars().mapToObj(i -> (char) i).map(chars::get).map(p -> p.stateForPreview(world.getRegistryManager())).toList()).toList()).toList();
        for(int y = 0; y < height; y++){
            for(int z = 0; z < depth; z++){
                for(int x = 0; x < width; x++){
                    world.setBlockState(frontTopLeft.add(x, y, z), states.get(y).get(z).get(x));
                }
            }
        }
    }
    public BlockPattern toPattern(){
        BlockPatternBuilder builder = BlockPatternBuilder.start();
        toAisles().forEach(builder::aisle);
        chars.forEach(builder::where);
        return builder.build();
    }
    static <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<>();
        final int N = table.getFirst().size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<>();
            for (List<T> row : table) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }
    //Aisles are ZYX
    private List<String[]> toAisles(){
       return transpose(pattern).stream().map(l -> l.toArray(new String[0])).toList();
    }
}
