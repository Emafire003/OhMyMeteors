package me.emafire003.dev.ohmymeteors.blocks.basic_laser;

import me.emafire003.dev.ohmymeteors.blocks.OMMBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class BasicMeteorLaserBlockEntity extends BlockEntity {
    public BasicMeteorLaserBlockEntity(BlockPos pos, BlockState state) {
        super(OMMBlocks.BASIC_METEOR_LASER_BLOCK_ENTITY, pos, state);
    }
}
