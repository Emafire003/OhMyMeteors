package me.emafire003.dev.ohmymeteors.entities;

import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import me.emafire003.dev.structureplacerapi.StructurePlacerAPI;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;

public class MeteorProjectileEntity extends ExplosiveProjectileEntity {

    public MeteorProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> entityType, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, world);
    }

    public MeteorProjectileEntity(World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, world);
    }

    public MeteorProjectileEntity(double x, double y, double z, Vec3d velocity, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, x, y, z, velocity, world);
    }

    public MeteorProjectileEntity(LivingEntity owner, Vec3d velocity, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, owner, velocity, world);
    }


    /*public MeteorProjectileEntity(EntityType<? extends AbstractFireballEntity> entityType, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, world);
    }

    public MeteorProjectileEntity(EntityType<? extends AbstractFireballEntity> entityType, LivingEntity livingEntity, Vec3d vec3d, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, livingEntity, vec3d, world);
    }

    public MeteorProjectileEntity(EntityType<? extends AbstractFireballEntity> entityType, double d, double e, double f, Vec3d vec3d, World world) {
        super(OMMEntities.METEOR_PROJECTILE_ENTITY, d, e, f, vec3d, world);
    }*/


    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {

        if(!this.getWorld().getBlockState(blockHitResult.getBlockPos()).isAir()){

            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 10, World.ExplosionSourceType.NONE);

            if(!this.getWorld().isClient()){
                OhMyMeteors.LOGGER.warn("UH Helllooooo?:" + blockHitResult.getType());
                StructurePlacerAPI placer = new StructurePlacerAPI((StructureWorldAccess) this.getWorld(), OhMyMeteors.getIdentifier("proto_meteor"), this.getBlockPos(), BlockMirror.NONE, BlockRotation.NONE, true, 1f, new BlockPos(0, 0, 0));
                placer.loadStructure();
            }

            this.discard();
        }
        super.onBlockHit(blockHitResult);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
    }
}
