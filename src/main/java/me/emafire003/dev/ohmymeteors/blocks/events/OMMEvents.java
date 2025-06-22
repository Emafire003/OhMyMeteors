package me.emafire003.dev.ohmymeteors.blocks.events;

import me.emafire003.dev.ohmymeteors.blocks.basic_laser.BasicMeteorLaserBlock;

public class OMMEvents {

    public static void registerEvents(){
        MeteorSpawnEvent.EVENT.register(meteor -> BasicMeteorLaserBlock.awakeLasers());
    }
}
