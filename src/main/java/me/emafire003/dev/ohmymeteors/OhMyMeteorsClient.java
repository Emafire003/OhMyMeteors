package me.emafire003.dev.ohmymeteors;

import me.emafire003.dev.ohmymeteors.blocks.OMMBlocks;
import me.emafire003.dev.ohmymeteors.entities.OMMEntities;
import me.emafire003.dev.ohmymeteors.entities.client.MeteorProjectileEntityModel;
import me.emafire003.dev.ohmymeteors.entities.client.MeteorProjectileEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class OhMyMeteorsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        EntityModelLayerRegistry.registerModelLayer(MeteorProjectileEntityModel.METEOR, MeteorProjectileEntityModel::getTexturedModelData);
        EntityRendererRegistry.register(OMMEntities.METEOR_PROJECTILE_ENTITY, MeteorProjectileEntityRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(OMMBlocks.BASIC_METEOR_LASER, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(OMMBlocks.ADVANCED_METEOR_LASER, RenderLayer.getTranslucent());
    }

}
