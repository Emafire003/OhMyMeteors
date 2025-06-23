package me.emafire003.dev.ohmymeteors.blocks.advanced_laser;

import me.emafire003.dev.ohmymeteors.blocks.OMMBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class AdvancedMeteorLaserBlockEntity extends BlockEntity {
    public AdvancedMeteorLaserBlockEntity(BlockPos pos, BlockState state) {
        super(OMMBlocks.ADVANCED_METEOR_LASER_BLOCK_ENTITY, pos, state);
    }
}
