package me.emafire003.dev.ohmymeteors.entities;

import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.Registry;

public class OMMEntities {

    /*public static final EntityType<MeteorProjectileEntity> METEOR_PROJECTILE_ENTITY = Registry.register(
            Registry.ENTITY_TYPE,
            OhMyMeteors.getIdentifier("meteor_projectile"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, MeteorProjectileEntity::new)
                    .dimensions(EntityDimensions.changing(0.9F, 0.9F)).build());*/



    /*public static final EntityType<MeteorProjectileEntity> METEOR_PROJECTILE_ENTITY = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MOD_ID, "meteor_projectile"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, MeteorProjectileEntity::new).dimensions(EntityDimensions.fixed(1.4f, 2.7f)).trackRangeBlocks(10).build()
    );*/
/*
    public static final EntityType<EarthGolemEntity> EARTH_GOLEM = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(MOD_ID, "earth_golem"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, EarthGolemEntity::new).dimensions(EntityDimensions.fixed(1.4f, 2.7f)).trackRangeBlocks(10).build()
    );*/


    public static final EntityType<MeteorProjectileEntity> METEOR_PROJECTILE_ENTITY = Registry.register(
            Registry.ENTITY_TYPE,
            OhMyMeteors.getIdentifier("meteor_projectile"),
            EntityType.Builder.<MeteorProjectileEntity>create(MeteorProjectileEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.9F, 0.9F).build("meteor_projectile")
    );

    public static final EntityType<MeteorCatEntity> METEOR_KITTY_CAT = Registry.register(Registries.ENTITY_TYPE,
            OhMyMeteors.getIdentifier("meteor_cat"),
            EntityType.Builder.<MeteorCatEntity>create(MeteorCatEntity::new, SpawnGroup.MISC)
                    .setDimensions(0.9F, 0.9F).build());

    public static void registerEntities(){
        FabricDefaultAttributeRegistry.register(METEOR_KITTY_CAT, MeteorCatEntity.createCatAttributes());
    }
}
