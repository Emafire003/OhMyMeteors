package me.emafire003.dev.ohmymeteors.entities;

import com.google.common.annotations.VisibleForTesting;
import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import me.emafire003.dev.ohmymeteors.config.Config;
import me.emafire003.dev.structureplacerapi.StructurePlacerAPI;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;


/**
 * The projectile entity that gets spawned as a meteor.
 * Upon hitting a block which is not air, it will execute the on-hit actions
 * such as creating an explosion and spawning the structure of blocks of the meteor thing*/
public class MeteorProjectileEntity extends ExplosiveProjectileEntity {

    private static final TrackedData<Integer> SIZE = DataTracker.registerData(MeteorProjectileEntity.class, TrackedDataHandlerRegistry.INTEGER);
    //TODO implement
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 127;
    //TODO needs proper testing
    private static final ChunkTicketType<Vec3i> METEOR_CHUCK_TICKET = ChunkTicketType.create("meteor", Vec3i::compareTo, 5*20);

    public MeteorProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> entityType, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, world);
        initialize();
    }

    public MeteorProjectileEntity(World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, world);
        initialize();
    }

    public MeteorProjectileEntity(double x, double y, double z, Vec3d velocity, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, x, y, z, velocity, world);
        initialize();
    }

    public MeteorProjectileEntity(LivingEntity owner, Vec3d velocity, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, owner, velocity, world);
        initialize();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SIZE, 1);
    }


    @VisibleForTesting
    public void setSize(int size) {
        int i = MathHelper.clamp(size, 1, 127);
        this.dataTracker.set(SIZE, i);
        this.refreshPosition();
        this.calculateDimensions();
    }

    public int getSize() {
        return this.dataTracker.get(SIZE);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("Size", this.getSize() - 1);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        this.setSize(nbt.getInt("Size") + 1);
        super.readCustomDataFromNbt(nbt);
    }
    @Override
    public void calculateDimensions() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.calculateDimensions();
        this.setPosition(d, e, f);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (SIZE.equals(data)) {
            this.calculateDimensions();
            this.setYaw(this.getYaw()); //todo maybe remove?
            if (this.isTouchingWater() && this.random.nextInt(20) == 0) {
                this.onSwimmingStart();
            }
        }

        super.onTrackedDataSet(data);
    }

    /**
     * Initializes the meteor with a random size upon creation of the meteor object.
     * Called along with the constructor method
     * */
    public void initialize() {
        Random random = this.getRandom();
        int i = random.nextInt(3);
        if (i < 2 && random.nextFloat() < 0.5f) {
            i++;
        }
        int j = 1 << i;
        this.setSize(j);
    }


    @Override
    public final EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).scaled(this.getSize());
    }

    /// these things are used to keep track of a chunk load, in order to not send a loading ticket each tick
    private int loadingChuckTicks = 0;
    private ChunkPos currentlyLoadedChunk;

    @Override
    public void tick() {
        super.tick();
        //Every 100 seconds or every time the meteor enters a new chuck, the meteor loads the chunk it's in for 5 seconds or 100 ticks
        if(this.getWorld() instanceof ServerWorld world){
            if(loadingChuckTicks > 0){
                if(currentlyLoadedChunk == null || !currentlyLoadedChunk.equals(this.getChunkPos())){
                    world.getChunkManager().addTicket(METEOR_CHUCK_TICKET,  this.getChunkPos(), 1, this.getBlockPos());
                    currentlyLoadedChunk = this.getChunkPos();
                    loadingChuckTicks = 5*20;
                }
                loadingChuckTicks++;
                return;
            }

            world.getChunkManager().addTicket(METEOR_CHUCK_TICKET,  this.getChunkPos(), 1, this.getBlockPos());
            currentlyLoadedChunk = this.getChunkPos();
            loadingChuckTicks = 5*20;
        }
    }

    /// This is the main method which does the meteor stuff on impact
    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        BlockState state = this.getWorld().getBlockState(blockHitResult.getBlockPos());

        //It also registers Air blocks as a collision so we need to avoid such cases
        if(!state.isAir()){
            //If bypass leaves is false skip directly to the other code
            if(Config.SHOULD_BYPASS_LEAVES && state.isIn(BlockTags.LEAVES)){
                //Early return so the rest of the code doesn't run if the meteor hits a leaves block and the config option is there
                return;
            }
            //this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 10, World.ExplosionSourceType.NONE);


            ExplosionBehavior explosionBehavior = new ExplosionBehavior();

            //entity.getWorld().addParticle(ParticleTypes.FLASH, pos.getX(), pos.getY(), pos.getZ(), 0,0,0);
            this.getWorld().createExplosion(this, this.getDamageSources().explosion(this, this), explosionBehavior, this.getPos(), this.getSize(), true, World.ExplosionSourceType.TNT);

            if(!this.getWorld().isClient()){
                StructurePlacerAPI placer = new StructurePlacerAPI((StructureWorldAccess) this.getWorld(), OhMyMeteors.getIdentifier("proto_meteor"), this.getBlockPos(), BlockMirror.NONE, BlockRotation.NONE, true, 1f, new BlockPos(0, 0, 0));
                placer.loadStructure();
            }

            this.discard();
        }
    }

    //TODO override the tick method where this is used and use some of ParticleAnimationLib effects
    @Nullable
    protected ParticleEffect getParticleType() {
        return ParticleTypes.FLASH;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
    }
}
