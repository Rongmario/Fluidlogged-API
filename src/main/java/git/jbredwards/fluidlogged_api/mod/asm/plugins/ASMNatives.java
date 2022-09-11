package git.jbredwards.fluidlogged_api.mod.asm.plugins;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.mod.common.config.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * methods for this class are built during runtime via PluginASMNatives
 * @author jbred
 *
 */
public final class ASMNatives
{
    /**
     * {@link Block}
     */
    @Nullable
    public static native ConfigHandler.ICanFluidFlowHandler getCanFluidFlow(@Nonnull Block block);
    public static native void setCanFluidFlow(@Nonnull Block block, @Nullable ConfigHandler.ICanFluidFlowHandler canFluidFlow);

    /**
     * {@link BlockBush}
     */
    public static native boolean canSustainBush(@Nonnull BlockBush bush, @Nonnull IBlockState state);

    /**
     * {@link BlockFluidClassic}
     */
    @Nonnull
    public static native boolean[] getOptimalFlowDirections(@Nonnull BlockFluidClassic block, @Nonnull World world, @Nonnull BlockPos pos);
    public static native int getLargerQuanta(@Nonnull BlockFluidClassic block, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, int compare);
    public static native boolean canFlowInto(@Nonnull BlockFluidClassic block, @Nonnull IBlockAccess world, @Nonnull BlockPos pos);
    public static native void flowIntoBlock(@Nonnull BlockFluidClassic block, @Nonnull World world, @Nonnull BlockPos pos, int meta);

    /**
     * {@link Fluid}
     */
    @Nonnull public static native FluidState getDefaultFluidState(@Nonnull Fluid fluid);
    @Nonnull public static native FluidState setDefaultFluidState(@Nonnull Fluid fluid, @Nonnull FluidState fluidState);

    /**
     * {@link Template}
     */
    public static native void setKeepOldFluidStates(@Nonnull Template template, boolean keepOldFluidStates);
}
