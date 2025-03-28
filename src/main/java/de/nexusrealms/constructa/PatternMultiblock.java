package de.nexusrealms.constructa;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.nexusrealms.constructa.api.Multiblock;
import de.nexusrealms.constructa.api.MultiblockType;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

import java.util.List;
import java.util.Map;

public class PatternMultiblock implements Multiblock {

    public static final MapCodec<PatternMultiblock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.listOf().listOf().fieldOf("pattern").forGetter(PatternMultiblock::pattern),
            Codec.unboundedMap(Constructa.CHARACTER_CODEC, BlockPositionPredicate.CODEC).fieldOf("chars").forGetter(PatternMultiblock::chars),
            BlockPos.CODEC.listOf().fieldOf("checkablePositions").forGetter(PatternMultiblock::checkablePositions)
    ).apply(instance, PatternMultiblock::new));
    //List of layers of Z rows of X strings
    //So iterates YZX
    private final List<List<String>> pattern;
    private final Map<Character, BlockPositionPredicate> chars;
    private final List<BlockPos> checkablePositions;
    public List<List<String>> pattern() {
        return pattern;
    }

    public Map<Character, BlockPositionPredicate> chars() {
        return chars;
    }

    private final int width, height, depth;
    public PatternMultiblock(List<List<String>> pattern, Map<Character, BlockPositionPredicate> chars, List<BlockPos> checkablePositions) {
        this.pattern = pattern;
        this.chars = chars;
        this.checkablePositions = checkablePositions;
        this.width = pattern.getFirst().getFirst().length();
        this.height = pattern.size();
        this.depth = pattern.getFirst().size();
    }

    public List<BlockPos> checkablePositions() {
        return checkablePositions;
    }

    @Override
    public boolean wasFound(WorldView world, BlockPos searchPos) {
        Matcher matcher = new Matcher(this);
        // If that fails, try the rotation-aware matcher
        return matcher.searchForMatchByCheckables(world, searchPos) != null;
    }

    @Override
    public MultiblockType<?> getType() {
        return null;
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
    //You might be useful one day
/*   static <T> List<List<T>> transpose(List<List<T>> table) {
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
    }*/

    public static class Matcher {
        private final PatternMultiblock multiblock;
        private final int width, height, depth;

        public Matcher(PatternMultiblock multiblock) {
            this.multiblock = multiblock;
            // Pattern is in YZX format
            this.height = multiblock.height;
            this.depth = multiblock.depth;
            this.width = multiblock.width;
        }
        public Pair<BlockPos, Direction> searchForMatchByCheckables(WorldView world, BlockPos pos){
            return Direction.Type.HORIZONTAL.stream()
                    //Adds the checkable positions to the stream
                    .flatMap(direction -> multiblock.checkablePositions.stream()
                            //Rotates the checkable position with the direction
                            .map(blockPos -> blockPos.rotate(dirToRotNorthDefault(direction)))
                            //Adapts the checkable position to the corner position
                            .map(pos::subtract)
                            //Creates a pair to keep the direction
                            .map(blockPos -> new Pair<>(blockPos, direction)))
                    //Filters for origin positions for ones where the multiblock matches
                    .filter(pair -> matches(world, pair.getLeft(), pair.getRight()))
                    .findFirst()
                    .orElse(null);
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
        private static BlockRotation dirToRotNorthDefault(Direction dir){
            return switch (dir){
                case NORTH -> BlockRotation.NONE;
                case SOUTH -> BlockRotation.CLOCKWISE_180;
                case WEST -> BlockRotation.COUNTERCLOCKWISE_90;
                case EAST -> BlockRotation.CLOCKWISE_90;
                default -> throw new RuntimeException("How");
            };
        }
    }
}
