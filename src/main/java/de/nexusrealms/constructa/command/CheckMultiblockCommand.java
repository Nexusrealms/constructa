package de.nexusrealms.constructa.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import de.nexusrealms.constructa.multiblocks.SimpleMultiblock;

public class CheckMultiblockCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("checkmultiblock")
            .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                .executes(CheckMultiblockCommand::run)));
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");
        World world = source.getWorld();

        boolean isValid = SimpleMultiblock.checkMultiblock(world, pos);
        if (isValid) {
            source.sendFeedback(() -> Text.of("Multiblock structure is valid!"), false);
        } else {
            source.sendFeedback(() -> Text.of("Multiblock structure is invalid!"), false);
        }

        return 1;
    }
}