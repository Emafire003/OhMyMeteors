package me.emafire003.dev.ohmymeteors.blocks.events;

import me.emafire003.dev.ohmymeteors.blocks.laser.MeteorLaserBlock;

public class OMMEvents {

    public static void registerEvents(){
        MeteorSpawnEvent.EVENT.register(meteor -> MeteorLaserBlock.awakeLasers());
    }
}
