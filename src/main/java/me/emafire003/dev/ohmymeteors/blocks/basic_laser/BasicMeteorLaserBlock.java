package me.emafire003.dev.ohmymeteors.blocks.basic_laser;

import com.mojang.serialization.MapCodec;
import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import me.emafire003.dev.ohmymeteors.blocks.OMMBlocks;
import me.emafire003.dev.ohmymeteors.blocks.OMMProperties;
import me.emafire003.dev.ohmymeteors.config.Config;
import me.emafire003.dev.ohmymeteors.entities.MeteorProjectileEntity;
import me.emafire003.dev.ohmymeteors.sounds.OMMSounds;
import me.emafire003.dev.particleanimationlib.effects.CuboidEffect;
import me.emafire003.dev.particleanimationlib.effects.LineEffect;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

//Ah remeber that the whole chunk is loaded when a meteor enters it so this will be loaded as well no need for fancy stuff
public class BasicMeteorLaserBlock extends BlockWithEntity implements BlockEntityProvider {

    /// This is used when interacting with the block. With a normal click the checking area will get highlited by particles
    public static final BooleanProperty SHOW_AREA = OMMProperties.SHOW_AREA;

    ///Is able to detect and destroy meteors this many blocks up from its position
    protected static final int Y_LEVEL_AREA_COVERAGE = 64;
    /// The radius in blocks that this type of laser can cover aka how fare on the xz plane it can detect and shoot meteors
    protected static final int RADIUS_AREA_COVERAGE = 48; //Which is around 3x3 chunks

    /// Only awakens when a meteor is spawned somewhere in the world, to save up on checks
    //TODO migrate this to a property maybe
    private static boolean AWAKE = false;
    /// Used to determine for how long it should stay actively searching
    private static int tickCounterAwakening = -1;
    private static final int AWAKE_TIME_LIMIT = 20*25; //Should remain awake for 25 seconds after a meteor has spawned in

    public BasicMeteorLaserBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(SHOW_AREA, false));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(BasicMeteorLaserBlock::new);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BasicMeteorLaserBlockEntity(pos, state);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        //I'll keep the has sky light check, since a laser shouldn't be able to work indoors
        //TODO wiki say that it needs skylight
        return !world.isClient && world.getDimension().hasSkyLight() ? validateTicker(type, OMMBlocks.BASIC_METEOR_LASER_BLOCK_ENTITY, BasicMeteorLaserBlock::tick) : null;
    }

    /**
     * Wakes up all the lasers to check for meteors above them.
     * They automatically go back to sleep after {@link #AWAKE_TIME_LIMIT} ticks*/
    public static void awakeLasers(){
        AWAKE = true;
        tickCounterAwakening = 0;
    }

    public static boolean areLasersAwake(){
        return AWAKE;
    }


    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        //TODO add a texture change maybe
        //Note: sneaking won't work since it disables this interaction
        //TODO which item to use?
        if(stack.isOf(Items.IRON_INGOT)){
            BlockState blockState = state.cycle(SHOW_AREA);
            if(blockState.get(SHOW_AREA)){
                world.playSound(null, pos, OMMSounds.LASER_AREA_ON, SoundCategory.BLOCKS, 0.7f, 1f);
            }else{
                world.playSound(null, pos, OMMSounds.LASER_AREA_OFF, SoundCategory.BLOCKS, 0.7f, 1f);
            }
            world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }


    //TODO add variants cooldown counter etc
    /** This is the main logic of the block. Will check every tick the space around the y level where meteors spawn
     * to see if a meteor has spawned. If it has, it shoots it down.
     */
    protected static void tick(World world, BlockPos pos, BlockState state, BasicMeteorLaserBlockEntity blockEntity) {
        if(world instanceof ServerWorld serverWorld && world.isSkyVisible(pos.up())){

            //Checks if either the laser is awake or if it needs to show the area. If none of this are true, returns early
            if(!state.get(SHOW_AREA) && !AWAKE){
                return;
            }

            Box box = new Box(new BlockPos(pos.getX(), Math.min(pos.getY()+getYLevelAreaCoverage(), Config.METEOR_SPAWN_HEIGHT), pos.getZ())).expand(getRadiusAreaCoverage(), 1, getRadiusAreaCoverage());

            //useful to see where the box is, gets shown when the the show area blockstate property is true
            if(state.get(SHOW_AREA)){
                CuboidEffect cuboidEffect = CuboidEffect.builder(serverWorld, ParticleTypes.BUBBLE_POP, box.getMinPos())
                        .particles(30).targetPos(box.getMaxPos()).iterations(1)
                        .build();
                cuboidEffect.run();


                Vec3d lowerPos = new Vec3d(box.getMaxPos().getX(), pos.getY(), box.getMaxPos().getZ());

                //The two vertical lines at the angles
                LineEffect line = LineEffect
                        .builder(serverWorld, ParticleTypes.BUBBLE_POP, box.getMaxPos())
                        .targetPos(lowerPos)
                        .particles((int) (lowerPos.distanceTo(box.getMaxPos())))
                        .iterations(1)
                        .build();
                line.run();

                lowerPos = new Vec3d(box.getMinPos().getX(), pos.getY(), box.getMinPos().getZ());
                line.setTargetPos(lowerPos);
                line.setOriginPos(box.getMinPos());
                line.setParticles((int) (lowerPos.distanceTo(box.getMinPos())));
                line.run();

                //The vertical line in the middle

                lowerPos = new Vec3d(box.getCenter().getX(), pos.getY(), box.getCenter().getZ());
                line.setTargetPos(lowerPos);
                line.setOriginPos(box.getCenter());
                line.setParticles((int) (lowerPos.distanceTo(box.getCenter())));
                line.run();

                //The horizontal lines at the top which point to the corner of the box
                lowerPos = box.getMaxPos();
                line.setTargetPos(lowerPos);
                line.setOriginPos(box.getCenter());
                line.setParticles((int) (lowerPos.distanceTo(box.getCenter())));
                line.run();

                lowerPos = box.getMinPos();
                line.setTargetPos(lowerPos);
                line.setOriginPos(box.getCenter());
                line.setParticles((int) (lowerPos.distanceTo(box.getCenter())));
                line.run();


            }

            if(!AWAKE){
                return;
            }
            //Makes sure this is awake
            if(tickCounterAwakening > AWAKE_TIME_LIMIT){
                tickCounterAwakening = 0;
                AWAKE = false;
                return;
            }

            List<MeteorProjectileEntity> meteors = world.getEntitiesByClass(MeteorProjectileEntity.class, box, (meteorProjectileEntity -> true));
            if(meteors == null){
                return;
            }

            meteors.forEach( meteorProjectileEntity -> {

                if(meteorProjectileEntity.getSize() > Config.NATURAL_METEOR_MAX_SIZE/1.5){
                    meteorProjectileEntity.detonateScatter();
                }else{
                    meteorProjectileEntity.detonateSimple();
                }

                //TODO later add a proper custom particle effect maybe
                //BUBBLE_POP could also work?
                LineEffect lineEffect = LineEffect
                        .builder(serverWorld, ParticleTypes.GLOW, pos.toCenterPos())
                        .targetPos(meteorProjectileEntity.getPos())
                        .particles((int) (pos.toCenterPos().distanceTo(meteorProjectileEntity.getPos())*2))
                        .build();
                lineEffect.runFor(1);

                //Plays the "pew" laser firing sound
                world.playSound(null, pos, OMMSounds.LASER_FIRE, SoundCategory.BLOCKS, 1f, 1.25f);


                if(Config.ANNOUNCE_METEOR_DESTROYED){
                    serverWorld.getPlayers().forEach(player -> player.sendMessage(Text.literal(OhMyMeteors.PREFIX).append(Text.translatable("message.ohmymeteors.meteor_destroyed").formatted(Formatting.GREEN)), Config.ACTIONBAR_ANNOUNCEMENTS));
                }
            });

            tickCounterAwakening++;
        }

    }

    protected static int getYLevelAreaCoverage(){
        return Y_LEVEL_AREA_COVERAGE;
    }

    protected static int getRadiusAreaCoverage(){
        return RADIUS_AREA_COVERAGE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {

        builder.add(SHOW_AREA);
    }

    public VoxelShape makeShape(){
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0, 0, 0, 1, 0.625, 1), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.4375, 0.625, 0.4375, 0.5625, 1, 0.5625), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0, 0.8125, 0, 1, 0.875, 0.4375), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0, 0.625, 0, 0.0625, 0.8125, 0.0625), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.9375, 0.625, 0, 1, 0.8125, 0.0625), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0, 0.625, 0.9375, 0.0625, 0.8125, 1), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.9375, 0.625, 0.9375, 1, 0.8125, 1), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0, 0.8125, 0.4375, 0.4375, 0.875, 0.5625), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0, 0.8125, 0.5625, 1, 0.875, 1), BooleanBiFunction.OR);
        shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(0.5625, 0.8125, 0.4375, 1, 0.875, 0.5625), BooleanBiFunction.OR);

        return shape;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return makeShape();
    }
}
