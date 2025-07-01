package me.emafire003.dev.ohmymeteors.items;

import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class OMMItems {

    public static final Item METEORIC_CHUNK = registerItem("meteoric_chunk",
            new MeteoricChunk(new Item.Settings().maxCount(64)));

    public static final Item METEORIC_ALLOY = registerItem("meteoric_alloy",
            new Item(new Item.Settings().maxCount(64)));

    public static final Item FOCUSING_LENSES = registerItem("focusing_lenses",
            new Item(new Item.Settings().maxCount(16)));

    private static Item registerItem(String name, Item item){
        return Registry.register(Registry.ITEM, OhMyMeteors.getIdentifier(name), item);
    }

    public static void registerItems(){
        OhMyMeteors.LOGGER.debug("Registering items...");
    }
}
