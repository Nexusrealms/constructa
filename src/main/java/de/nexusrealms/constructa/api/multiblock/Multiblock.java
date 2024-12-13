package de.nexusrealms.constructa.api.multiblock;

import de.nexusrealms.constructa.mixin.BucketItemMixin;
import de.nexusrealms.constructa.mixin.BlockMixin;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.DisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * Represents a multiblock structure with a specific pattern and predicates for block states.
 */
@SuppressWarnings({"unused"})
public class Multiblock {
    private char[][][] pattern;
    private final HashMap<Character, Predicate<BlockState>> predicates;
    private final boolean shouldPreview;
    private final int width;
    private final int height;
    private final int length;
    private boolean previewing = false;
    private static final ArrayList<BlockState> checkedCache = new ArrayList<>();

    public static HashMap<BlockPos, MultiblockPreviewElement> previewElements = new HashMap<>();

    /**
     * Constructs a Multiblock with the given pattern, predicates, and preview option.
     *
     * @param pattern       The 3D pattern of the multiblock.
     * @param predicates    The predicates for each character in the pattern.
     * @param shouldPreview Whether the multiblock should be previewed.
     */
    public Multiblock(char[][][] pattern, HashMap<Character, Predicate<BlockState>> predicates, boolean shouldPreview) {
        this.pattern = pattern;
        this.predicates = predicates;
        this.width = pattern[0].length;
        this.height = pattern.length;
        this.length = pattern[0][0].length;
        this.shouldPreview = shouldPreview;
        this.previewing = false;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getLength() {
        return this.length;
    }

    public boolean getPreviewed() {
        return this.previewing;
    }

    /**
     * Checks if the multiblock structure is valid at the given position in the world.
     *
     * @param mainBlockPos The main block position to check from.
     * @param world        The world to check in.
     * @return True if the multiblock structure is valid, false otherwise.
     */
    public boolean check(BlockPos mainBlockPos, World world) {
        // Find the $ in the pattern
        BlockPos corner = findOffset(mainBlockPos);
        if (corner == null) {
            LOGGER.error("Multiblock pattern does not contain $");
            return false;
        }

        // The value to return if the multiblock is valid or not
        boolean result = true;

        // Loop through the pattern and check if the blocks are right
        for (int i = 0; i < pattern.length; i++) {
            for (int j = 0; j < pattern[i].length; j++) {
                for (int k = 0; k < pattern[i][j].length; k++) {
                    // The blockpos of the block to check
                    BlockPos blockPos = corner.add(j, i, k);
                    Predicate<BlockState> predicate = predicates.get(pattern[i][j][k]);
                    boolean isRightBlock = predicate.test(world.getBlockState(blockPos));
                    // If the block is already in the map, remove it
                    if (shouldPreview) {
                        // If the elements are being previewed, remove the previews
                        if (previewing) {
                            previewElements.get(blockPos).destroy();
                            Multiblock.previewElements.remove(blockPos);
                        } else {
                            BlockState blockState = getBlockStateFromPredicate(predicate);
                            MultiblockPreviewElement previewElement = new MultiblockPreviewElement(blockPos, world, blockState, predicate);
                            previewElement.matchCheck(world.getBlockState(blockPos));
                            previewElements.put(blockPos, previewElement);
                        }
                    }
                    // If the block is not the right block, return false
                    if (!isRightBlock) result = false;
                }
            }
        }
        previewing = !previewing;
        return result;
    }

    /**
     * Rotates the multiblock pattern 90 degrees clockwise.
     */
    public void rotate() {
        int height = getHeight();
        int width = getWidth();
        int length = getLength();

        char[][][] rotated = new char[height][width][length];

        for (int c = 0; c < height; c++) {
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < width; j++) {
                    rotated[c][i][j] = pattern[c][width - 1 - j][i];
                }
            }
        }
        pattern = rotated;
    }

    /**
     * Finds the offset position of the $ character in the pattern.
     *
     * @param pos The position to start searching from.
     * @return The offset position of the $ character, or null if not found.
     */
    public BlockPos findOffset(BlockPos pos) {
        // Find the corner of the pattern
        for (int i = 0; i < this.height; i++) {
            for (int j = 0; j < this.width; j++) {
                for (int k = 0; k < this.length; k++) {
                    if (pattern[i][j][k] == '$') {
                        return pos.add(-j, -i, -k);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets a BlockState that matches the given predicate.
     *
     * @param predicate The predicate to match.
     * @return A BlockState that matches the predicate, or null if not found.
     */
    public static BlockState getBlockStateFromPredicate(Predicate<BlockState> predicate) {
        // If the predicate is ANY, return air to not cause it to display something else
        if (predicate == BlockStatePredicate.ANY) {
            return Blocks.AIR.getDefaultState();
        }
        // Loop through the already checked blockstates to not loop through all of them
        for (BlockState state : checkedCache) {
            if (predicate.test(state)) {
                return state;
            }
        }
        // If the blockstate is not in the cache, loop through all of them
        for (Block block : Registries.BLOCK) {
            for (BlockState state : block.getStateManager().getStates()) {
                if (predicate.test(state)) {
                    checkedCache.add(state);
                    return state;
                }
            }
        }
        return null;
    }

    /**
     * Represents a preview element for a multiblock structure.
     */
    public static class MultiblockPreviewElement {
        private final ElementHolder holder;
        private final HolderAttachment attachment;
        private final DisplayElement element;
        private boolean block = false;
        private final BlockPos pos;
        private final Predicate<BlockState> predicate;
        private final World world;

        public ElementHolder getHolder() {
            return this.holder;
        }

        public HolderAttachment getAttachment() {
            return this.attachment;
        }

        public DisplayElement getElement() {
            return this.element;
        }

        public boolean isBlock() {
            return this.block;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public BlockState getBlockState() {
            if (this.block) {
                return ((BlockDisplayElement)this.element).getBlockState();
            } else {
                return ((BucketItemMixin)(((ItemDisplayElement)element).getItem().getItem())).getFluid().getDefaultState().getBlockState();
            }
        }

        /**
         * Constructs a MultiblockPreviewElement with the given position, world, and block state.
         *
         * @param pos       The position of the preview element.
         * @param world     The world the preview element is in.
         * @param state     The block state of the preview element.
         * @param predicate The predicate for the block state.
         */
        public MultiblockPreviewElement(BlockPos pos, World world, BlockState state, Predicate<BlockState> predicate) {
            this(pos, world, createHolder(pos), state, predicate);
        }

        /**
         * Constructs a MultiblockPreviewElement with the given position, world, holder, and block state.
         *
         * @param pos       The position of the preview element.
         * @param world     The world the preview element is in.
         * @param holder    The element holder for the preview element.
         * @param state     The block state of the preview element.
         * @param predicate The predicate for the block state.
         */
        public MultiblockPreviewElement(BlockPos pos, World world, ElementHolder holder, BlockState state, Predicate<BlockState> predicate) {
            this(holder, ChunkAttachment.of(holder, (ServerWorld) world, pos), state, pos, predicate);
        }

        /**
         * Constructs a MultiblockPreviewElement with the given holder, attachment, block state, position, and predicate.
         *
         * @param holder    The element holder for the preview element.
         * @param attachment The holder attachment for the preview element.
         * @param state     The block state of the preview element.
         * @param pos       The position of the preview element.
         * @param predicate The predicate for the block state.
         */
        public MultiblockPreviewElement(ElementHolder holder, HolderAttachment attachment, BlockState state, BlockPos pos, Predicate<BlockState> predicate) {
            this.holder = holder;
            this.attachment = attachment;
            this.element = makeElementFromState(state);
            this.pos = pos;
            this.predicate = predicate;
            this.world = attachment.getWorld();
        }

        /**
         * Checks if the current block state matches the predicate and updates the preview element accordingly.
         *
         * @param state The current block state to check.
         */
        public void matchCheck(BlockState state) {
            boolean isRightBlock = predicate.test(state);
            if (isRightBlock) {
                this.element.setScale(new Vector3f(0.0F, 0.0F, 0.0F));
            } else {
                this.element.setScale(new Vector3f(0.5F, 0.5F, 0.5F));
                if (!state.isAir()) {
                    this.element.setGlowing(true);
                }
            }
            this.element.tick();
        }

        /**
         * Creates a display element from the given block state.
         *
         * @param state The block state to create the display element from.
         * @return The created display element.
         */
        private DisplayElement makeElementFromState(BlockState state) {
            DisplayElement element;
            if (state.getBlock() instanceof FluidBlock) {
                element = MultiblockPreviewElement.createEmptyItemElement();
                ((ItemDisplayElement) element).setItem(state.getFluidState().getFluid().getBucketItem().getDefaultStack());
            } else {
                element = MultiblockPreviewElement.createEmptyBlockElement();
                ((BlockDisplayElement) element).setBlockState(state);
                block = true;
            }
            this.getHolder().addElement(element);
            return element;
        }

        /**
         * Destroys the preview element and its attachments.
         */
        public void destroy() {
            this.getAttachment().destroy();
            this.getHolder().destroy();
        }

        /**
         * Creates an empty block display element.
         *
         * @return The created block display element.
         */
        public static BlockDisplayElement createEmptyBlockElement() {
            BlockDisplayElement element = new BlockDisplayElement();
            element.setGlowing(false);
            element.setScale(new Vector3f(0.5F, 0.5F, 0.5F));
            element.setOffset(new Vec3d(0.25F, 0.25F, 0.25F));
            return element;
        }

        /**
         * Creates an empty item display element.
         *
         * @return The created item display element.
         */
        public static ItemDisplayElement createEmptyItemElement() {
            ItemDisplayElement element = new ItemDisplayElement();
            element.setGlowing(false);
            element.setScale(new Vector3f(0.5F, 0.5F, 0.5F));
            element.setOffset(new Vec3d(0.5F, 0.5F, 0.5F));
            return element;
        }

        /**
         * Creates an element holder for the given position.
         *
         * @param pos The position to create the holder for.
         * @return The created element holder.
         */
        public static ElementHolder createHolder(BlockPos pos) {
            return new ElementHolder() {
                @Override
                public Vec3d getPos() {
                    return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                }
            };
        }

        /**
         * Safely checks if the block state at the given position matches the predicate and updates the preview element.
         *
         * @param world The world to check in.
         * @param pos   The position to check.
         */
        public static void safeMatchCheck(World world, BlockPos pos) {
            if (world.isClient) return;
            Multiblock.MultiblockPreviewElement previewElement = Multiblock.previewElements.get(pos);
            if (previewElement != null) {
                previewElement.matchCheck(world.getBlockState(pos));
            }
        }
    }
}