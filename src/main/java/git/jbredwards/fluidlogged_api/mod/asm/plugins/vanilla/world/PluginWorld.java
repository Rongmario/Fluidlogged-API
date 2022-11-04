package git.jbredwards.fluidlogged_api.mod.asm.plugins.vanilla.world;

import git.jbredwards.fluidlogged_api.api.asm.IASMPlugin;
import org.apache.logging.log4j.core.util.Loader;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

/**
 * corrects a lot of FluidState related interactions
 * @author jbred
 *
 */
public final class PluginWorld implements IASMPlugin
{
    @Override
    public int getMethodIndex(@Nonnull MethodNode method, boolean obfuscated) {
        //setBlockState, line 401
        if(checkMethod(method, obfuscated ? "func_180501_a" : "setBlockState", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"))
            return 1;

        //setBlockToAir, line 456
        else if(checkMethod(method, obfuscated ? "func_175698_g" : "setBlockToAir", null))
            return 2;

        //destroyBlock, line 480
        else if(checkMethod(method, obfuscated ? "func_175655_b" : "destroyBlock", null))
            return 2;

        //neighborChanged, line 626
        else if(checkMethod(method, obfuscated ? "func_190524_a" : "neighborChanged", null))
            return 3;

        //handleMaterialAcceleration, line 2443
        else if(checkMethod(method, obfuscated ? "func_72918_a" : "handleMaterialAcceleration", null)) {
            return 4;
        }

        //isMaterialInBB, line 2494
        else if(checkMethod(method, obfuscated ? "func_72875_a" : "isMaterialInBB", null)) {
            return 5;
        }

        //changes some methods to use FluidloggedUtils#getFluidOrReal
        else if(checkMethod(method, obfuscated ? "func_72953_d" : "containsAnyLiquid", null)
        || checkMethod(method, obfuscated ? "func_147470_e" : "isFlammableWithin", null)
        || checkMethod(method, obfuscated ? "func_175696_F" : "isWater", null)
        || checkMethod(method, obfuscated ? "func_175705_a" : "getLightFromNeighborsFor", null)
        || checkMethod(method, obfuscated ? "func_175721_c" : "getLight", "(Lnet/minecraft/util/math/BlockPos;Z)I"))
            return 6;

        //isFlammableWithin, fix bug with lava level
        else if(method.name.equals(obfuscated ? "func_147470_e" : "isFlammableWithin")) return 7;

        //rayTraceBlocks, ray traces now include fluidlogged fluid blocks
        else if(checkMethod(method, obfuscated ? "func_147447_a" : "rayTraceBlocks", "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;"))
            return 8;

        return 0;
    }

    @Override
    public boolean transform(@Nonnull InsnList instructions, @Nonnull MethodNode method, @Nonnull AbstractInsnNode insn, boolean obfuscated, int index) {
        //setBlockState, line 401
        if(index == 1 && checkMethod(insn, obfuscated ? "func_177436_a" : "setBlockState", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;")) {
            final InsnList list = new InsnList();
            //oldState local var, check for Galaxy Space mod cause that mod shifts local variable indexes, gross...
            list.add(new VarInsnNode(ALOAD, Loader.isClassAvailable("galaxyspace.core.hooklib.minecraft.HookLibPlugin") ? 7 : 6));
            //params
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ILOAD, 3));

            list.add(genMethodNode("setBlockState", "(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;I)Lnet/minecraft/block/state/IBlockState;"));
            instructions.insert(insn, list);
            instructions.remove(insn);
            return true;
        }

        //setBlockToAir & destroyBlock
        else if(index == 2 && checkMethod(insn, obfuscated ? "func_176223_P" : "getDefaultState", "()Lnet/minecraft/block/state/IBlockState;")) {
            final InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ALOAD, 1));
            list.add(genMethodNode("getFluidOrAir", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"));

            instructions.insert(insn, list);
            instructions.remove(insn.getPrevious());
            instructions.remove(insn);
            return true;
        }

        //neighborChanged, line 626
        else if(index == 3 && checkMethod(insn, obfuscated ? "func_189546_a" : "neighborChanged", null)) {
            final InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ALOAD, 1));
            list.add(new VarInsnNode(ALOAD, 2));
            list.add(new VarInsnNode(ALOAD, 3));
            list.add(new VarInsnNode(ALOAD, 4));
            list.add(genMethodNode("neighborChanged", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V"));

            instructions.insert(insn, list);
            return true;
        }

        //handleMaterialAcceleration, line 2406
        else if(index == 4 && checkField(insn, obfuscated ? "field_186680_a" : "ZERO", "Lnet/minecraft/util/math/Vec3d;")) {
            final InsnList list = new InsnList();
            //new (mutable) Vec3d instance based at the origin
            list.add(new TypeInsnNode(NEW, "net/minecraft/util/math/Vec3d"));
            list.add(new InsnNode(DUP));
            list.add(new InsnNode(DCONST_0));
            list.add(new InsnNode(DCONST_0));
            list.add(new InsnNode(DCONST_0));
            list.add(new MethodInsnNode(INVOKESPECIAL, "net/minecraft/util/math/Vec3d", "<init>", "(DDD)V", false));

            instructions.insert(insn, list);
            instructions.remove(insn);
            return false;
        }

        //handleMaterialAcceleration, line 2443
        else if(index == 4 && checkMethod(insn, obfuscated ? "func_185344_t" : "release", "()V")) {
            final InsnList list = new InsnList();
            //params
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ALOAD, 2));
            list.add(new VarInsnNode(ALOAD, 3));
            //vec3d
            list.add(new VarInsnNode(ALOAD, 11));
            //flag
            list.add(new VarInsnNode(ILOAD, 10));
            //aabb positions
            list.add(new VarInsnNode(ILOAD, 4));
            list.add(new VarInsnNode(ILOAD, 5));
            list.add(new VarInsnNode(ILOAD, 6));
            list.add(new VarInsnNode(ILOAD, 7));
            list.add(new VarInsnNode(ILOAD, 8));
            list.add(new VarInsnNode(ILOAD, 9));
            //adds new code
            list.add(genMethodNode("handleMaterialAcceleration", "(Lnet/minecraft/util/math/BlockPos$PooledMutableBlockPos;Lnet/minecraft/world/World;Lnet/minecraft/block/material/Material;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;ZIIIIII)Z"));
            //set flag to new code value
            list.add(new VarInsnNode(ISTORE, 10));

            instructions.insert(insn, list);
            instructions.remove(insn);
            return true;
        }

        //isMaterialInBB, line 2494
        else if(index == 5 && insn.getOpcode() == ICONST_0) {
            final InsnList list = new InsnList();
            //params
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ALOAD, 1));
            list.add(new VarInsnNode(ALOAD, 2));
            //aabb positions
            list.add(new VarInsnNode(ILOAD, 3));
            list.add(new VarInsnNode(ILOAD, 4));
            list.add(new VarInsnNode(ILOAD, 5));
            list.add(new VarInsnNode(ILOAD, 6));
            list.add(new VarInsnNode(ILOAD, 7));
            list.add(new VarInsnNode(ILOAD, 8));
            //adds new code
            list.add(genMethodNode("isMaterialInBB", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/block/material/Material;IIIIII)Z"));

            instructions.insert(insn, list);
            instructions.remove(insn);
            return true;
        }

        //changes some methods to use FluidloggedUtils#getFluidOrReal
        else if(index == 6 && checkMethod(insn, obfuscated ? "func_180495_p" : "getBlockState", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;")) {
            instructions.insert(insn, genMethodNode("git/jbredwards/fluidlogged_api/api/util/FluidloggedUtils", "getFluidOrReal", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"));
            instructions.remove(insn);
            return true;
        }

        //isFlammableWithin, fix bug with lava level
        else if(index == 7 && checkField(insn, obfuscated ? "field_150353_l" : "LAVA")) {
            final InsnList list = new InsnList();
            list.add(new VarInsnNode(ALOAD, 0));
            list.add(new VarInsnNode(ALOAD, 8));
            list.add(new VarInsnNode(ALOAD, 1));
            list.add(genMethodNode("isFlammableWithin", "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;)Z"));
            instructions.insert(insn, list);
            removeFrom(instructions, insn, -3);
            return true;
        }

        return false;
    }

    @Override
    public boolean transformClass(@Nonnull ClassNode classNode, boolean obfuscated) {
        classNode.interfaces.add("git/jbredwards/fluidlogged_api/api/world/IChunkProvider");
        addMethod(classNode, "getChunkFromBlockCoords", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/Chunk;", null, null, generator -> {
            generator.visitVarInsn(ALOAD, 0);
            generator.visitVarInsn(ALOAD, 1);
            generator.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/World", obfuscated ? "func_175726_f" : "getChunk", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/Chunk;", false);
        });
        //rayTraceBlocks, ray traces now include fluidlogged fluid blocks
        overrideMethod(classNode, method -> checkMethod(method, obfuscated ? "func_147447_a" : "rayTraceBlocks", "(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;"),
            "rayTraceBlocks", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;", generator -> {
                generator.visitVarInsn(ALOAD, 0);
                generator.visitVarInsn(ALOAD, 1);
                generator.visitVarInsn(ALOAD, 2);
                generator.visitVarInsn(ILOAD, 3);
                generator.visitVarInsn(ILOAD, 4);
                generator.visitVarInsn(ILOAD, 5);
            }
        );

        return true;
    }
}
