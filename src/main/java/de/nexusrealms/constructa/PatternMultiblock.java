package de.nexusrealms.constructa;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

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
    //TODO Remove this once matcher is done
    private final BlockPattern transformed;

    private final int width, height, depth;
    public PatternMultiblock(List<List<String>> pattern, Map<Character, BlockPositionPredicate> chars) {
        this.pattern = pattern;
        this.chars = chars;
        //TODO Remove this once matcher is done
        transformed = toPattern();

        this.width = pattern.getFirst().getFirst().length();
        this.height = pattern.size();
        this.depth = pattern.getFirst().size();
    }
    @Override
    public boolean wasFound(WorldView world, BlockPos searchPos) {
        //TODO Remove this once matcher is done
        // First try the fast search using BlockPattern
        //if (transformed.searchAround(world, searchPos) != null) {
        //    return true;
        //}
        Matcher matcher = new Matcher(this);
        // If that fails, try the rotation-aware matcher
        for (Direction facing : Direction.Type.HORIZONTAL) {
            if (matcher.matches(world, searchPos, facing)) {
                return true;
            }
        }
        return false;
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
    //TODO Remove this once matcher is done
    public void constructFromPattern(ServerWorld world, BlockPos frontTopLeft) {
        List<List<List<BlockState>>> states = Arrays.stream(transformed.getPattern()).map(a2 -> Arrays.stream(a2).map(a3 -> Arrays.stream(a3).map(p -> p instanceof BlockPositionPredicate predicate ? predicate.stateForPreview(world.getRegistryManager()) : Blocks.BARRIER.getDefaultState()).toList()).toList()).toList();
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < depth; z++) {
                for (int x = 0; x < width; x++) {
                    world.setBlockState(frontTopLeft.add(x, y, z), states.get(z).get(y).get(x));
                }
            }
        }
    }
    //TODO Remove this once matcher is done
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

    public static class Matcher {
        private final PatternMultiblock multiblock;
        private final int width;
        private final int height;
        private final int depth;

        public Matcher(PatternMultiblock multiblock) {
            this.multiblock = multiblock;
            // Pattern is in YZX format
            this.height = multiblock.height;
            this.depth = multiblock.depth;;
            this.width = multiblock.width;
        }

        public boolean matches(WorldView world, BlockPos pos, Direction facing) {
            // We'll check from top to bottom
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    String row = multiblock.pattern().get(y).get(z);
                    for (int x = 0; x < width; x++) {
                        char pattern = row.charAt(x);
                        BlockPositionPredicate predicate = multiblock.chars().get(pattern);

                        if (predicate == null) {
                            continue; // Skip if no predicate defined
                        }

                        BlockPos checkPos = transformPosition(pos, x, y, z, facing);
                        if (!predicate.test(new CachedBlockPosition(world, checkPos, true))) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }

        private BlockPos transformPosition(BlockPos origin, int x, int y, int z, Direction facing) {
            return switch (facing) {
                case SOUTH -> origin.add(-x, y, -z);
                case EAST -> origin.add(-z, y, x);
                case WEST -> origin.add(z, y, -x);
                default -> origin.add(x, y, z);
            };
        }
    }
}
