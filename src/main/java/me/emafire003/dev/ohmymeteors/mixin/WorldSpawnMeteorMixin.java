package me.emafire003.dev.ohmymeteors.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import me.emafire003.dev.ohmymeteors.entities.MeteorProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class WorldSpawnMeteorMixin extends World implements StructureWorldAccess {

    @Shadow @Nullable public abstract ServerPlayerEntity getRandomAlivePlayer();

    @Shadow public abstract boolean spawnEntity(Entity entity);

    @Shadow public abstract RandomSequencesState getRandomSequences();

    @Unique
    int meteorCooldown = 0;

    @Inject(method = "tick", at = @At(value = "TAIL"))
    public void tickSpawnMeteor(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        if(OhMyMeteors.debugSpawningOff){
            return;
        }

        if(meteorCooldown > 0){
            meteorCooldown = meteorCooldown - 1;
        }
        if(this.getRandom().nextBetween(0, 100) == 59){

            PlayerEntity p = this.getRandomAlivePlayer();

            if(p == null){
                //for some reason it won't detetct that there is player online sometimes
                //TODO maybe use loaded chunkc instead somehow?
                return;
            }

            Vec3d playerPos = p.getPos();
            //TODO add possibility to track directly the player aka send the meteord towards them
            MeteorProjectileEntity meteor = new MeteorProjectileEntity(this);

            int invert_x = 1;
            if(this.getRandom().nextBoolean()){
                invert_x = -1;
            }

            int invert_y = 1;
            if(this.getRandom().nextBoolean()){
                invert_y = -1;
            }

            meteor.setPos(playerPos.getX()+this.getRandom().nextBetween(0, 50)*invert_x,
                    300,
                    playerPos.getZ()+this.getRandom().nextBetween(0, 50)*invert_y
            );

            invert_x = 1;
            if(this.getRandom().nextBoolean()){
                invert_x = -1;
            }

            invert_y = 1;
            if(this.getRandom().nextBoolean()){
                invert_y = -1;
            }

            meteor.setSize(this.getRandom().nextBetween(1, 5));
            meteor.setVelocity((this.getRandom().nextFloat()/2)*invert_x, -1.0f+this.getRandom().nextFloat(), (this.getRandom().nextFloat()/2)*invert_y);

            //TODO debug remove
            if (true){
                p.teleport(meteor.getX(), p.getY(), meteor.getZ(), false);
            }

            this.spawnEntity(meteor);
            meteorCooldown = 20*50; //One every 50 seconds
        }
    }

    protected WorldSpawnMeteorMixin(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }
}
