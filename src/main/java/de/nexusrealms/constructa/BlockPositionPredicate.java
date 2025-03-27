package de.nexusrealms.constructa;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

import java.util.function.Predicate;

public interface BlockPositionPredicate extends Predicate<CachedBlockPosition> {
    Codec<BlockPositionPredicate> CODEC = Codec.withAlternative(BlockBased.BLOCK_CODEC, Codec.withAlternative(BlockStateBased.STATE_CODEC, Codec.withAlternative(TagBased.TAG_CODEC, PredicateBased.PREDICATE_CODEC)));
    BlockState stateForPreview(RegistryWrapper.WrapperLookup wrapperLookup);
    record BlockBased(Block block) implements BlockPositionPredicate {
        public static final Codec<BlockPositionPredicate> BLOCK_CODEC = Registries.BLOCK.getCodec().flatComapMap(BlockBased::new,
                blockPositionPredicate -> blockPositionPredicate instanceof BlockBased blockBased ? DataResult.success(blockBased.block()) : DataResult.error(() -> "Not right type"));
        @Override
        public boolean test(CachedBlockPosition cachedBlockPosition) {
            return cachedBlockPosition.getBlockState().isOf(block);
        }

        @Override
        public BlockState stateForPreview(RegistryWrapper.WrapperLookup wrapperLookup) {
            return block.getDefaultState();
        }
    }
    record BlockStateBased(BlockState state) implements BlockPositionPredicate {
        public static final Codec<BlockPositionPredicate> STATE_CODEC = BlockState.CODEC.flatComapMap(BlockStateBased::new,
                blockPositionPredicate -> blockPositionPredicate instanceof BlockStateBased stateBased ? DataResult.success(stateBased.state()) : DataResult.error(() -> "Not right type"));
        @Override
        public boolean test(CachedBlockPosition cachedBlockPosition) {
            return cachedBlockPosition.getBlockState().equals(state);
        }

        @Override
        public BlockState stateForPreview(RegistryWrapper.WrapperLookup wrapperLookup) {
            return state;
        }
    }
    record TagBased(TagKey<Block> tag) implements BlockPositionPredicate {
        public static final Codec<BlockPositionPredicate> TAG_CODEC = TagKey.codec(RegistryKeys.BLOCK).flatComapMap(TagBased::new,
                blockPositionPredicate -> blockPositionPredicate instanceof TagBased tagBased ? DataResult.success(tagBased.tag()) : DataResult.error(() -> "Not right type"));
        @Override
        public boolean test(CachedBlockPosition cachedBlockPosition) {
            return cachedBlockPosition.getBlockState().isIn(tag);
        }

        @Override
        public BlockState stateForPreview(RegistryWrapper.WrapperLookup wrapperLookup) {
            return wrapperLookup.getWrapperOrThrow(RegistryKeys.BLOCK).getOrThrow(tag).stream().findAny().map(RegistryEntry::value).orElse(Blocks.BEDROCK).getDefaultState();
        }
    }
    record PredicateBased(BlockPredicate predicate) implements BlockPositionPredicate {
        public static final Codec<BlockPositionPredicate> PREDICATE_CODEC = BlockPredicate.CODEC.flatComapMap(PredicateBased::new,
                blockPositionPredicate -> blockPositionPredicate instanceof PredicateBased predicateBased ? DataResult.success(predicateBased.predicate()) : DataResult.error(() -> "Not right type"));
        @Override
        public boolean test(CachedBlockPosition cachedBlockPosition) {
            return predicate.test(cachedBlockPosition);
        }

        @Override
        public BlockState stateForPreview(RegistryWrapper.WrapperLookup wrapperLookup) {
            return Blocks.BEDROCK.getDefaultState();
        }
    }
}
