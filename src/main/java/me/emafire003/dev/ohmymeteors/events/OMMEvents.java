package me.emafire003.dev.ohmymeteors.events;

import me.emafire003.dev.ohmymeteors.blocks.advanced_laser.AdvancedMeteorLaserBlock;
import me.emafire003.dev.ohmymeteors.blocks.basic_laser.BasicMeteorLaserBlock;

public class OMMEvents {

    public static void registerEvents(){
        MeteorSpawnEvent.EVENT.register(meteor -> {
            BasicMeteorLaserBlock.awakeLasers();
            AdvancedMeteorLaserBlock.awakeLasers();
        });
    }
}
