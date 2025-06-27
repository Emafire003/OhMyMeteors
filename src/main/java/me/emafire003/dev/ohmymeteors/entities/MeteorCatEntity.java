package me.emafire003.dev.ohmymeteors.entities;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Util;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MeteorCatEntity extends CatEntity {

    private static final TrackedData<String> TYPE_VARIANT =
            DataTracker.registerData(MeteorCatEntity.class, TrackedDataHandlerRegistry.STRING);

    public MeteorCatEntity(EntityType<? extends CatEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    public static DefaultAttributeContainer.Builder createCatAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 15.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.35F)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_BURNING_TIME, 0); //So even if it gets on fire it won't last
    }


    @Override
    public boolean damage(DamageSource source, float amount) {
        if(source.isIn(DamageTypeTags.IS_FIRE)){
            return false;
        }
        return super.damage(source, amount);
    }

    /// Variant stuff

    /* VARIANT */
    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        //TODO mixin into cat class to remove the other line about variants
        builder.add(TYPE_VARIANT, "meteor");
    }

    @Override
    public MeteorCatVariant getVariant() {
        return MeteorCatVariant.byId(getTypeVariant());
    }

    private String getTypeVariant() {
        return this.dataTracker.get(TYPE_VARIANT);
    }

    private void setVariant(MeteorCatVariant variant) {
        this.dataTracker.set(TYPE_VARIANT, variant.getId());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("Variant", this.getTypeVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(TYPE_VARIANT, nbt.getString("Variant"));
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 @Nullable EntityData entityData) {
        setVariant(MeteorCatVariant.DEFAULT);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }
}
