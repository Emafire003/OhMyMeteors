package me.emafire003.dev.ohmymeteors.entities;

import com.google.common.annotations.VisibleForTesting;
import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import me.emafire003.dev.ohmymeteors.blocks.events.MeteorSpawnEvent;
import me.emafire003.dev.ohmymeteors.config.Config;
import me.emafire003.dev.particleanimationlib.effects.VortexEffect;
import me.emafire003.dev.particleanimationlib.effects.base.YPREffect;
import me.emafire003.dev.structureplacerapi.StructurePlacerAPI;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.fluid.FluidState;
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
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * The projectile entity that gets spawned as a meteor.
 * Upon hitting a block which is not air, it will execute the on-hit actions
 * such as creating an explosion and spawning the structure of blocks of the meteor thing*/
public class MeteorProjectileEntity extends ExplosiveProjectileEntity {

    private static final TrackedData<Integer> SIZE = DataTracker.registerData(MeteorProjectileEntity.class, TrackedDataHandlerRegistry.INTEGER);
    //TODO needs proper testing
    private static final ChunkTicketType<Vec3i> METEOR_CHUCK_TICKET = ChunkTicketType.create("meteor", Vec3i::compareTo, 5*20);

    /// Aka a meteor that is a result of the {@link #detonateScatter()} method
    protected boolean isScatterMeteor = false;


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
        MeteorSpawnEvent.EVENT.invoker().meteorSpawned(this);
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
        particleAnimation();
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

    int particleCooldown = 0;
    //The particle effect that is going to be spawned once every second by the falling meteor
    YPREffect particleEffect;

    //pal vortex minecraft:flame ~ ~ ~ 1 0.01 0.8 0.1 5 3 10 false 3
    /**
     * Spawns the vortex particle effect behind the meteor once every second*/
    //TODO this has simply decided to stop working on its own. AUBDfusvGF hSVIDgyscDyagsd cia
    public void particleAnimation(){
        if(this.getWorld().isClient()){
            return;
        }
        //Initializes the effect. Can't do it before since i need to be sure i'm on the server instead of the client
        if(particleEffect == null){
            //TODO ok maybe an inveted cone?
            ///pal cone minecraft:flame ~ ~ ~ 100 4 2 0.1 0.03 2 2 false true true 3
            /*particleEffect = ConeEffect
                    .builder((ServerWorld) this.getWorld(), ParticleTypes.FLAME, this.getPos())
                    .particlesCone(100).particles(10).strands(2).lengthGrow(0.1f)
                    .radiusGrow(0.03f).inverted(true).randomize(true).angularVelocity(2).solid(false)
                    .rotation(2)
                    .yaw(this.getYaw()).pitch(this.getPitch())
                    .secondaryParticle(ParticleTypes.FLAME)
                    .updatePositions(true).entityOrigin(this)
                    .build();*/
            particleEffect = VortexEffect
                    .builder((ServerWorld) this.getWorld(), ParticleTypes.FLAME, this.getPos())
                    .helixes(10).circles(5).radials(5d).lengthGrow(0.1f).radius(this.getSize())
                    .radiusGrow(0.01f).startRange((float) (this.getSize() * 80) /100)
                    .yaw(this.getYaw()).pitch(this.getPitch())
                    //.updatePositions(true).entityOrigin(this)
                    .build();

        }
        //Don't run before 1 second
        if(particleCooldown > 0){
            particleCooldown--;
            //return;
        }
        particleEffect.setYaw(this.getYaw());
        particleEffect.setPitch(this.getPitch());
        //Updating this because they could change after the meteor is created
        /*particleEffect.setRadius(this.getSize());
        particleEffect.setStartRange((float) (this.getSize() * 80) /100);*/

        /*particleEffect.runFor(10, (a, b) -> {
            OhMyMeteors.LOGGER.info("The postion is: " + a.getOriginPos());
        });*/
        particleCooldown = 20; //sets this to 20 aka 1 second
    }
    
    
    /** Makes this entity explode without creating any structures on impact
     * and then discards this entity*/
    public void detonateSimple(){
        ExplosionBehavior explosionBehavior = new ExplosionBehavior();

        ExplosionBehavior safeExplosion = new ExplosionBehavior() {
            @Override
            public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
                return Optional.of(Blocks.BEDROCK.getBlastResistance());
            }
        };

        if(isScatterMeteor()){
            if(Config.SCATTER_METEOR_GRIEFING){
                this.getWorld().createExplosion(this, this.getDamageSources().explosion(this, this), explosionBehavior, this.getPos(), this.getSize(), true, World.ExplosionSourceType.TNT);
            }else{
                this.getWorld().createExplosion(this, this.getDamageSources().explosion(this, this), safeExplosion, this.getPos(), this.getSize(), false, World.ExplosionSourceType.TNT);
            }
            this.discard();
            return;
        }

        if(Config.METEOR_GRIEFING){
            //TODO add custom  explosion source type
            this.getWorld().createExplosion(this, this.getDamageSources().explosion(this, this), explosionBehavior, this.getPos(), this.getSize(), true, World.ExplosionSourceType.TNT);
        }else{
            this.getWorld().createExplosion(this, this.getDamageSources().explosion(this, this), safeExplosion, this.getPos(), this.getSize(), false, World.ExplosionSourceType.TNT);
        }
        //entity.getWorld().addParticle(ParticleTypes.FLASH, pos.getX(), pos.getY(), pos.getZ(), 0,0,0);
        this.discard();
    }
    
    /** Like {@link #detonateSimple()} but will also spawn the structure of the meteor*/
    public void detonateWithStructure(){
        detonateSimple();
        if(!this.getWorld().isClient()){
            //TODO read the filenames of the files of the /structure/ folder thing and check the folders that have like small medium big ecc
            StructurePlacerAPI placer = new StructurePlacerAPI((StructureWorldAccess) this.getWorld(), OhMyMeteors.getIdentifier("proto_meteor"), this.getBlockPos(), BlockMirror.NONE, BlockRotation.NONE, true, 1f, new BlockPos(0, 0, 0));
            placer.loadStructure();
        }
    }
    
    /**This will detonate the meteor with an explosion like {@link #detonateSimple()}
     * but will also spawn other meteors based on the size of this meteor. 
     * 
     * Meteors will be smaller and be oriented randomly from that point on, but will still go down.
     * */
    //TODO implement
    public void detonateScatter(){
        if(this.getWorld().isClient()){
            return;
        }

        //Can generate a minimum of 1 new meteor up to a number equal half of the size of this meteor
        int scatter_into = this.getRandom().nextBetween(1, this.getSize()/2);
        //this is used to determine the size of the new meteors, which will be smaller than the original
        //each new meteor is going to take up some of the "mass" of the parent one, leaving the rest for the next one
        //and so on.
        int remainingSize = this.getSize()/2+1;

        List<MeteorProjectileEntity> newMeteors = new ArrayList<>();
        for(int i = 0; i<scatter_into; i++){
            //Gets a random number between 1 and the remaining size, making sure to leave at least one size for each new meteor yet to generate)
            int size =  this.getRandom().nextBetween(1, remainingSize-(scatter_into-i));
            MeteorProjectileEntity m = getDownwardsMeteor(this.getPos(), (ServerWorld) this.getWorld(), 1, 10+this.getSize() /2, this.getPos().getY(), size, size, false);
            m.setScatterMeteor(true);
            newMeteors.add(m);
        }

        this.detonateSimple();

        newMeteors.forEach( meteorProjectileEntity -> this.getWorld().spawnEntity(meteorProjectileEntity));

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

            if(this.isScatterMeteor()){
                if(Config.SCATTER_METEOR_STRUCTURE){
                    this.detonateWithStructure();
                }else{
                    this.detonateSimple();
                }
                return;
            }

            if(Config.METEOR_STRUCTURE){
                this.detonateWithStructure();
            }else{
                this.detonateSimple();
            }

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
    
    /**
     * Gets a meteor object to be spawned in, with a velocity oriented dowards and a spawn position already set up
     * */
    public static MeteorProjectileEntity getDownwardsMeteor(Vec3d originPos, ServerWorld world, int min_spawn_d, int max_spawn_d, double spawn_height, int min_size, int max_size, boolean homing){
        MeteorProjectileEntity meteor = new MeteorProjectileEntity(world);

        //The invert is to also have a chance at having negative coordinates, otherwise they would always be positive
        int invert_x = 1;
        if(world.getRandom().nextBoolean()){
            invert_x = -1;
        }

        int invert_y = 1;
        if(world.getRandom().nextBoolean()){
            invert_y = -1;
        }

        meteor.setPos(originPos.getX()+world.getRandom().nextBetween(min_spawn_d, max_spawn_d)*invert_x,
                spawn_height,
                originPos.getZ()+world.getRandom().nextBetween(min_spawn_d, max_spawn_d)*invert_y
        );

        invert_x = 1;
        if(world.getRandom().nextBoolean()){
            invert_x = -1;
        }

        invert_y = 1;
        if(world.getRandom().nextBoolean()){
            invert_y = -1;
        }

        meteor.setSize(world.getRandom().nextBetween(Math.max(0, min_size), Math.min(50, max_size)));

        //TODO world is VERY WIP and only works if the player is rather far down from where the meteor spawns in. Like i might delete world instead
        if(homing){
            Vec3d vec3d = meteor.getRotationVec(1.0F);
            double f = originPos.getX() - (meteor.getX() + vec3d.x * 4.0);
            double g = /*originPos.getBodyY(0.5)*/ originPos.getY() - (0.5 + meteor.getBodyY(0.5));
            double h = originPos.getZ() - (meteor.getZ() + vec3d.z * 4.0);
            Vec3d vec3d2 = new Vec3d(f, g, h);
            meteor.setVelocity(vec3d2.multiply(1f, 0.01f, 1f));
        }else{
            meteor.setVelocity((world.getRandom().nextFloat()/2)*invert_x, -1.0f+world.getRandom().nextFloat(), (world.getRandom().nextFloat()/2)*invert_y);

        }
        return meteor;
    }

    public boolean isScatterMeteor() {
        return isScatterMeteor;
    }

    public void setScatterMeteor(boolean scatterMeteor) {
        isScatterMeteor = scatterMeteor;
    }
}
