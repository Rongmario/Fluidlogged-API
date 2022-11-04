package git.jbredwards.fluidlogged_api.mod.asm.plugins.vanilla.block;

import git.jbredwards.fluidlogged_api.api.asm.IASMPlugin;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.state.IBlockState;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;

/**
 * makes piston bases fluidloggable by default
 * @author jbred
 *
 */
public final class PluginBlockPistonBase implements IASMPlugin
{
    @Override
    public boolean transformClass(@Nonnull ClassNode classNode, boolean obfuscated) {
        classNode.interfaces.add("git/jbredwards/fluidlogged_api/api/block/IFluidloggable");
        /*
         * isFluidloggable:
         * New code:
         * //piston bases are fluidloggable while extended
         * @ASMGenerated
         * public boolean isFluidloggable(IBlockState state, World world, BlockPos pos)
         * {
         *     return Hooks.isPistonFluidloggable(state);
         * }
         */
        addMethod(classNode, "isFluidloggable", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z",
            "isPistonFluidloggable", "(Lnet/minecraft/block/state/IBlockState;)Z", generator -> generator.visitVarInsn(ALOAD, 1));

        return false;
    }

    @SuppressWarnings("unused")
    public static final class Hooks
    {
        public static boolean isPistonFluidloggable(@Nonnull IBlockState state) { return state.getValue(BlockPistonBase.EXTENDED); }
    }
}
