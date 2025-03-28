package de.nexusrealms.constructa;

    import net.minecraft.block.pattern.CachedBlockPosition;
    import net.minecraft.util.math.BlockPos;
    import net.minecraft.util.math.Direction;
    import net.minecraft.world.WorldView;

    public class MultiblockMatcher {
        private final PatternMultiblock multiblock;
        private final int width;
        private final int height;
        private final int depth;

        public MultiblockMatcher(PatternMultiblock multiblock) {
            this.multiblock = multiblock;
            // Pattern is in YZX format
            this.height = multiblock.pattern().size();
            this.depth = multiblock.pattern().get(0).size();
            this.width = multiblock.pattern().get(0).get(0).length();
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

                        BlockPos checkPos = transformPosition(pos, x, -y, z, facing);
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
                case NORTH -> origin.add(x, y, z);
                case SOUTH -> origin.add(-x, y, -z);
                case EAST -> origin.add(-z, y, x);
                case WEST -> origin.add(z, y, -x);
                default -> origin.add(x, y, z);
            };
        }
    }