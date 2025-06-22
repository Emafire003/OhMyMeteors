package me.emafire003.dev.ohmymeteors.blocks.basic_laser;

import com.mojang.serialization.MapCodec;
import me.emafire003.dev.ohmymeteors.OhMyMeteors;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//Ah remeber that the whole chunk is loaded when a meteor enters it so this will be loaded as well no need for fancy stuff
public class BasicMeteorLaserBlock extends BlockWithEntity implements BlockEntityProvider {

    ///Is able to detect and destroy meteors this many blocks up from its position
    protected static int Y_LEVEL_AREA_COVERAGE = 64;
    /// Must wait before firing again for this many seconds
    protected final int COOLDOWN_TIME = 3;
    /// The radius in blocks that this type of laser can cover aka how fare on the xz plane it can detect and shoot meteors
    protected static int RADIUS_AREA_COVERAGE = 48; //Which is around 3x3 chunks

    /// Only awakens when a meteor is spawned somewhere in the world, to save up on checks
    private static boolean AWAKE = false;
    /// Used to determine for how long it should stay actively searching
    private static int tickCounter = -1;
    private static final int AWAKE_TIME_LIMIT = 20*25; //Should remain awake for 25 seconds after a meteor has spawned in

    public BasicMeteorLaserBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(BasicMeteorLaserBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BasicMeteorLaserBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        //I'll keep the has sky light check, since a laser shouldn't be able to work indoors
        //TODO wiki say that it needs skylight
        return !world.isClient && world.getDimension().hasSkyLight() ? validateTicker(type, OMMBlocks.BASIC_METEOR_LASER_BLOCK_ENTITY, BasicMeteorLaserBlock::tick) : null;
    }

    /**
     * Wakes up all of the lasers to check for meteors above them.
     * They automatically go back to sleep after {@link #AWAKE_TIME_LIMIT} ticks*/
    public static void awakeLasers(){
        AWAKE = true;
        tickCounter = 0;
        OhMyMeteors.LOGGER.info("Ok lasers have been awoken");
    }

    public static boolean areLasersAwake(){
        return AWAKE;
    }


    //TODO add variants cooldown counter etc
    /** This is the main logic of the block. Will check every tick the space around the y level where meteors spawn
     * to see if a meteor has spawned. If it has, it shoots it down.
     */
    private static void tick(World world, BlockPos pos, BlockState state, BasicMeteorLaserBlockEntity blockEntity) {
        if(world instanceof ServerWorld serverWorld){
            if(!AWAKE){
                return;
            }
            //Makes sure this is awake
            if(tickCounter > AWAKE_TIME_LIMIT){
                tickCounter = 0;
                AWAKE = false;
                return;
            }
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

                if(meteorProjectileEntity.getSize() < Config.NATURAL_METEOR_MAX_SIZE/1.5){
                    meteorProjectileEntity.detonateScatter();
                }else{
                    meteorProjectileEntity.detonateSimple();
                }


                //TODO add a "bzoot" sound effect and maybe custom particles
                LineEffect lineEffect = LineEffect
                        .builder(serverWorld, ParticleTypes.ELECTRIC_SPARK, pos.toCenterPos())
                        .targetPos(meteorProjectileEntity.getPos())
                        .particles((int) (pos.toCenterPos().distanceTo(meteorProjectileEntity.getPos())*2))
                        .build();
                lineEffect.runFor(1);

                if(Config.ANNOUNCE_METEOR_DESTROYED){
                    serverWorld.getPlayers().forEach(player -> {
                        player.sendMessage(Text.literal(OhMyMeteors.PREFIX).append(Text.translatable("message.ohmymeteors.meteor_destroyed").formatted(Formatting.GREEN)), Config.ACTIONBAR_ANNOUNCEMENTS);
                    });
                }
            });

            tickCounter++;
        }

    }

}
