package me.emafire003.dev.ohmymeteors.entities;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

//TODO if we really want variants, we need to have
public enum MeteorCatVariant {
    DEFAULT("meteor");

    private final String id;

    MeteorCatVariant(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static MeteorCatVariant byId(String id) {
        return valueOf(id);
    }
}

public record MeteorCatVariant(Identifier texture) {
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<net.minecraft.entity.passive.CatVariant>> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.CAT_VARIANT);
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> TABBY = of("tabby");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> BLACK = of("black");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> RED = of("red");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> SIAMESE = of("siamese");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> BRITISH_SHORTHAIR = of("british_shorthair");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> CALICO = of("calico");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> PERSIAN = of("persian");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> RAGDOLL = of("ragdoll");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> WHITE = of("white");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> JELLIE = of("jellie");
    public static final RegistryKey<net.minecraft.entity.passive.CatVariant> ALL_BLACK = of("all_black");

    private static RegistryKey<net.minecraft.entity.passive.CatVariant> of(String id) {
        return RegistryKey.of(RegistryKeys.CAT_VARIANT, Identifier.ofVanilla(id));
    }

    public static net.minecraft.entity.passive.CatVariant registerAndGetDefault(Registry<net.minecraft.entity.passive.CatVariant> registry) {
        register(registry, TABBY, "textures/entity/cat/tabby.png");
        register(registry, BLACK, "textures/entity/cat/black.png");
        register(registry, RED, "textures/entity/cat/red.png");
        register(registry, SIAMESE, "textures/entity/cat/siamese.png");
        register(registry, BRITISH_SHORTHAIR, "textures/entity/cat/british_shorthair.png");
        register(registry, CALICO, "textures/entity/cat/calico.png");
        register(registry, PERSIAN, "textures/entity/cat/persian.png");
        register(registry, RAGDOLL, "textures/entity/cat/ragdoll.png");
        register(registry, WHITE, "textures/entity/cat/white.png");
        register(registry, JELLIE, "textures/entity/cat/jellie.png");
        return register(registry, ALL_BLACK, "textures/entity/cat/all_black.png");
    }

    private static net.minecraft.entity.passive.CatVariant register(Registry<net.minecraft.entity.passive.CatVariant> registry, RegistryKey<net.minecraft.entity.passive.CatVariant> key, String textureId) {
        return Registry.register(registry, key, new net.minecraft.entity.passive.CatVariant(Identifier.ofVanilla(textureId)));
    }
}