package me.emafire003.dev.ohmymeteors.blocks;

import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import net.minecraft.state.property.BooleanProperty;

public class OMMProperties {

    public static final BooleanProperty SHOW_AREA = BooleanProperty.of("show_area");

    public static void registerBlockProperties(){
        OhMyMeteors.LOGGER.debug("Registering OhMyMeteors block properties...");
    }
}
