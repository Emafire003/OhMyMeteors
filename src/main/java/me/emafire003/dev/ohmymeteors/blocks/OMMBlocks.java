package me.emafire003.dev.ohmymeteors.blocks;

import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import me.emafire003.dev.ohmymeteors.blocks.laser.MeteorLaserBlock;
import me.emafire003.dev.ohmymeteors.blocks.laser.MeteorLaserBlockEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;

public class OMMBlocks {

    public static final Block METEOR_LASER = registerBlock("meteor_laser",
            new MeteorLaserBlock(AbstractBlock.Settings.create()
                    .strength(0.5f, 0.5f)
                    .luminance(value -> 1) //Makes a little bit of light
                    .solid()
                    .sounds(BlockSoundGroup.COPPER_GRATE)
                    .requiresTool()
            ), ItemGroups.REDSTONE, Items.REDSTONE_LAMP);

    private static Block registerBlock(String name, Block block, RegistryKey<ItemGroup> tab, Item add_after) {
        Block the_block = Registry.register(Registries.BLOCK, OhMyMeteors.getIdentifier(name), block);
        Item the_item = Registry.register(Registries.ITEM, OhMyMeteors.getIdentifier(name), new BlockItem(block, new Item.Settings()));
        ItemGroupEvents.modifyEntriesEvent(tab).register(content -> content.addAfter(add_after, the_item));
        return the_block;
    }

    public static final BlockEntityType<MeteorLaserBlockEntity> METEOR_LASER_BLOCK_ENTITY =
            register("meteor_laser", MeteorLaserBlockEntity::new, METEOR_LASER);

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
            Block... blocks
    ) {
        return Registry.register(Registries.BLOCK_ENTITY_TYPE, OhMyMeteors.getIdentifier(name),

                FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build()
        );
    }

    public static void registerBlocks(){

    }
}
