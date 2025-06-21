package me.emafire003.dev.ohmymeteors.blocks.laser;

import com.mojang.serialization.MapCodec;
import me.emafire003.dev.ohmymeteors.blocks.OMMBlocks;
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

//Ah remeber that the whole chunk is loaded when a meteor enters it so this will be loaded as well no need for fancy stuff
public class MeteorLaserBlock extends BlockWithEntity implements BlockEntityProvider {

    //TODO maybe these could be properties?
    /// Is able to destroy meteors of this size without fragmentation
    protected static int POWER_LEVEL = 1;
    ///Is able to detect and destroy meteors this many blocks up from its position
    protected static int Y_LEVEL_AREA_COVERAGE = 64;
    /// Must wait before firing again for this many seconds
    protected static int COOLDOWN_TIME = 10;
    /// The radius in blocks that this type of laser can cover aka how fare on the xz plane it can detect and shoot meteors
    protected static int RADIUS_AREA_COVERAGE = 32;


    //Used to count how much time is left before being able to fire again
    private int cooldownTicker = 0;

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
        //TODO ok now this is risky sometimes misses the fastest meteors TODO remove!!!
        //This way checks only half of the time. Technically super fast meteors could escape but...
        if(tickCounter%2 == 0){
            return;
        }
        if(world instanceof ServerWorld serverWorld){
            //Box box = new Box(new BlockPos(pos.getX(), Config.METEOR_SPAWN_HEIGHT, pos.getZ())).expand(16, 0, 16);

            Box box = new Box(new BlockPos(pos.getX(), pos.getY()+Y_LEVEL_AREA_COVERAGE, pos.getZ())).expand(RADIUS_AREA_COVERAGE, 0, RADIUS_AREA_COVERAGE);


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

                if(POWER_LEVEL < meteorProjectileEntity.getSize()){
                    meteorProjectileEntity.detonateScatter();
                }else{
                    meteorProjectileEntity.detonateSimple();
                }


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
