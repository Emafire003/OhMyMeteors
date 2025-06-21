package me.emafire003.dev.ohmymeteors.blocks.laser;

import me.emafire003.dev.ohmymeteors.blocks.OMMBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class MeteorLaserBlockEntity extends BlockEntity {
    public MeteorLaserBlockEntity(BlockPos pos, BlockState state) {
        super(OMMBlocks.METEOR_LASER_BLOCK_ENTITY, pos, state);
    }
}
