package me.emafire003.dev.ohmymeteors.blocks.laser;

import com.mojang.serialization.MapCodec;
import me.emafire003.dev.ohmymeteors.blocks.OMMBlocks;
import me.emafire003.dev.ohmymeteors.config.Config;
import me.emafire003.dev.ohmymeteors.entities.MeteorProjectileEntity;
import me.emafire003.dev.particleanimationlib.effects.LineEffect;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MeteorLaserBlock extends BlockWithEntity implements BlockEntityProvider {
    public MeteorLaserBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(MeteorLaserBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MeteorLaserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        //I'll keep the has sky light check, since a laser shouldn't be able to work indoors
        //TODO wiki say that it needs skylight
        return !world.isClient && world.getDimension().hasSkyLight() ? validateTicker(type, OMMBlocks.METEOR_LASER_BLOCK_ENTITY, MeteorLaserBlock::tick) : null;
    }


    private static int tickCounter = 0;


    //TODO add variants cooldown counter etc
    /** This is the main logic of the block. Will check every tick the space around the y level where meteors spawn
     * to see if a metor has spawned. If it has, it shoots it down.
     */
    private static void tick(World world, BlockPos pos, BlockState state, MeteorLaserBlockEntity blockEntity) {
        //TODO let's say for now it covers a 32x32 area around where it's place
        tickCounter++;
        //This way checks only half of the time. Technically super fast meteors could escape but...
        if(tickCounter%2 == 0){
            return;
        }
        if(world instanceof ServerWorld serverWorld){
            Box box = new Box(new BlockPos(pos.getX(), Config.METEOR_SPAWN_HEIGHT, pos.getZ())).expand(16, 0, 16);

            //useful to see where the box is
            /*CuboidEffect cuboidEffect = CuboidEffect.builder(serverWorld, ParticleTypes.COMPOSTER, box.getMinPos())
                    .particles(20).targetPos(box.getMaxPos())
                    .build();
            cuboidEffect.setIterations(1);
            cuboidEffect.run();*/

            List<MeteorProjectileEntity> meteors = world.getEntitiesByClass(MeteorProjectileEntity.class, box, (meteorProjectileEntity -> true));
            if(meteors == null){
                return;
            }

            meteors.forEach( meteorProjectileEntity -> {
                meteorProjectileEntity.detonateSimple();
                LineEffect lineEffect = LineEffect
                        .builder(serverWorld, ParticleTypes.ELECTRIC_SPARK, pos.toCenterPos())
                        .targetPos(meteorProjectileEntity.getPos())
                        .particles((int) (pos.toCenterPos().distanceTo(meteorProjectileEntity.getPos())*2))
                        .build();
                lineEffect.runFor(1);
            });
        }

    }

}
