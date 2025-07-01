package me.emafire003.dev.ohmymeteors.events;

import me.emafire003.dev.ohmymeteors.entities.MeteorProjectileEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface MeteorSpawnEvent {
    Event<MeteorSpawnEvent> EVENT = EventFactory.createArrayBacked(MeteorSpawnEvent.class, (listeners) -> (MeteorProjectileEntity meteor) -> {
        for (MeteorSpawnEvent listener : listeners) {
           listener.meteorSpawned(meteor);
        }
    });

    void meteorSpawned(MeteorProjectileEntity meteor);
}
