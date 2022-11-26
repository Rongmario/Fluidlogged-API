package git.jbredwards.fluidlogged_api.mod.asm.plugins.vanilla.world;

import git.jbredwards.fluidlogged_api.api.asm.IASMPlugin;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * spawner dungeons now void FluidStates when they generate
 * @author jbred
 *
 */
public final class PluginWorldGenDungeons implements IASMPlugin
{
    @Override
    public boolean isMethodValid(@Nonnull MethodNode method, boolean obfuscated) { return method.name.equals(obfuscated ? "func_180709_b" : "generate"); }

    @Override
    public boolean transform(@Nonnull InsnList instructions, @Nonnull MethodNode method, @Nonnull AbstractInsnNode insn, boolean obfuscated, int index) {
        /*
         * generate: (changes are around lines 81 & 86)
         * Old code:
         * worldIn.setBlockToAir(blockpos1);
         *
         * New code:
         * //don't keep old FluidStates
         * worldIn.setBlockState(blockpos1, Blocks.AIR.getDefaultState());
         */
        if(checkMethod(insn, obfuscated ? "func_175698_g" : "setBlockToAir")) {
            instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, "net/minecraft/init/Blocks", obfuscated ? "field_150350_a" : "AIR", "Lnet/minecraft/block/Block;"));
            instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/block/Block", obfuscated ? "func_176223_P" : "getDefaultState", "()Lnet/minecraft/block/state/IBlockState;", false));
            instructions.insertBefore(insn, new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/world/World", obfuscated ? "func_175656_a" : "setBlockState", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z", false));
            instructions.remove(insn);
        }
        /*
         * generate: (changes are around lines 92, 97, 126, and 140)
         * Old code:
         * worldIn.setBlockState(position, Blocks.MOB_SPAWNER.getDefaultState(), 2);
         *
         * New code:
         * //change to blockFlag | 32 to void potential FluidStates
         * worldIn.setBlockState(position, Blocks.MOB_SPAWNER.getDefaultState(), 2 | 32);
         */
        else if(insn.getOpcode() == ICONST_2 && checkMethod(insn.getNext(), obfuscated ? "func_180501_a" : "setBlockState")) {
            final boolean isEndOfTransform = checkField(insn.getPrevious(), obfuscated ? "field_150474_ac" : "MOB_SPAWNER");
            instructions.insert(insn, new LdcInsnNode(2 | 32));
            instructions.remove(insn);
            return isEndOfTransform;
        }

        return false;
    }
}
