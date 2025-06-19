package me.emafire003.dev.ohmymeteors.config;

import com.mojang.datafixers.util.Pair;
import me.emafire003.dev.ohmymeteors.OhMyMeteors;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static me.emafire003.dev.ohmymeteors.OhMyMeteors.LOGGER;

public class Config {
    public static SimpleConfig CONFIG;
    private static ConfigProvider configs;

    private static final int ver = 1;
    public static int VERSION;


    public static int NATURAL_METEOR_MIN_SIZE = 1; //as in the ones that spawn from the sky not the ones you can summon
    public static int NATURAL_METEOR_MAX_SIZE = 5;

    public static int MIN_METEOR_SPAWN_DISTANCE = 2; //As in a radius of blocks around the player in which the meteor won't spawn in (but remember that it can have an angled trajectory)
    public static int MAX_METEOR_SPAWN_DISTANCE = 25; //TODO probably increase it back to 50?

    public static int METEOR_SPAWN_HEIGHT = 300; //At which y should meteors spawn?

    public static boolean SHOULD_BYPASS_LEAVES = true; //Should the meteor bypass leaves instead of exploding midair om them?

    //TODO how should this work? A percentage? A fraction? For now i think i'm gonna go with 1 on the number here.
    public static int METEOR_SPAWN_CHANCE = 20000;

    public static boolean INCREASE_SPAWN_AT_NIGHT = false;
    public static int METEOR_NIGHT_SPAWN_CHANCE = 10000;

    public static boolean HAVE_INTERVAL_BETWEEN_METEORS = true;
    public static int MIN_METEOR_INTERVAL_TIME = 20; //In seconds

    //TODO implement homing mechanism
    public static boolean HOMING_METEORS = false; //These would not spawn with a random direction but aim towards a selected player

    public static boolean ALLOW_TARGET_SAME_PLAYER = true; //Would allow a meteor to appear around a player that has already spawned the last meteor close to them
    /*public static Map<String, Object> OPTIONS = Map.of(
            ""
    );*/


    public static void handleVersionChange(){
        int version_found = CONFIG.getOrDefault("version", ver);
        if(version_found != ver){
            LOGGER.warn("DIFFERENT CONFIG VERSION DETECTED, updating...");
            HashMap<String, String> config_old = CONFIG.getConfigCopy();
            try {
                CONFIG.delete();
                CONFIG = SimpleConfig.of(OhMyMeteors.MOD_ID + "_config").provider(configs).request();
                HashMap<Pair<String, ?>, Pair<String, ?>> sub_map = new HashMap<>();

                CONFIG.getConfigCopy().forEach((key, value) -> sub_map.put(new Pair<>(key, value),  new Pair<>(key, config_old.get(key))));
                CONFIG.updateValues(sub_map);
            } catch (IOException e) {
                LOGGER.info("Could not delete config file");
                e.printStackTrace();
            }
        }
    }

    public static void registerConfigs() {
        configs = new ConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(OhMyMeteors.MOD_ID + "_config").provider(configs).request();

        handleVersionChange();

        try{
            assignConfigs();
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.warn("ERROR! The config could not be read, generating a new one...");

            File source = OhMyMeteors.PATH.resolve(OhMyMeteors.MOD_ID + "_config.yml").toFile();
            File target = OhMyMeteors.PATH.resolve(OhMyMeteors.MOD_ID + "_corruptedorold_config.yml").toFile();
            try{
                FileUtils.copyFile(source, target);
                if(CONFIG.delete()){
                    LOGGER.info("Config deleted successfully");
                }else{
                    LOGGER.error("The config could not be deleted");
                }
            } catch (IOException f) {
                f.printStackTrace();
            }
            CONFIG = SimpleConfig.of(OhMyMeteors.MOD_ID + "_config").provider(configs).request();
            assignConfigs();
            LOGGER.warn("Generated a new config file, make sure to configure it again!");
        }

        LOGGER.info("All " + configs.getConfigsList().size() + " have been set properly");
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("version", ver), "The version of the config. DO NOT CHANGE IT :D");

        configs.addKeyValuePair(new Pair<>("spacer", "spacer"), "");

        configs.addKeyValuePair(new Pair<>("min_meteor_spawn_distance", 2), "Expressed in blocks, represents the min distance (as in a radius) from the origin of the meteor " +
                "(like a player) in which the meteor wont' spawn in. (Remember that it has an angled trajectory so it could end up in that area regardless)");

        configs.addKeyValuePair(new Pair<>("max_meteor_spawn_distance", 25), "Expressed in blocks, represents the max distance (as in a radius) from the origin of the meteor " +
                "(like a player) in which a meteor can spawn in. (Remember that it has an angled trajectory so it could end up in that area regardless)");
/*
        public static int METEOR_SPAWN_HEIGHT = 300; //At which y should meteors spawn?

        public static boolean SHOULD_BYPASS_LEAVES = true; //Should the meteor bypass leaves instead of exploding midair om them?

        //TODO how should this work? A percentage? A fraction? For now i think i'm gonna go with 1 on the number here.
        public static int METEOR_SPAWN_CHANCE = 20000;

        public static boolean INCREASE_SPAWN_AT_NIGHT = false;
        public static int METEOR_NIGHT_SPAWN_CHANCE = 10000;
*/

    }

    public static void reloadConfig(){
        registerConfigs();
        LOGGER.info("All " + configs.getConfigsList().size() + " have been reloaded properly");

    }

    private static void assignConfigs() {

        VERSION = CONFIG.getOrDefault("version", ver);

        //AREA_OF_SEARCH_FOR_ENTITIES = CONFIG.getOrDefault("area_of_search_for_entities", 12);
    }
}

