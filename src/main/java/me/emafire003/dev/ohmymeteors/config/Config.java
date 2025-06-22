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

    //TODO add option for annoucing meteors spawns in chat

    public static int NATURAL_METEOR_MIN_SIZE = 1; //as in the ones that spawn from the sky not the ones you can summon
    public static int NATURAL_METEOR_MAX_SIZE = 7;

    public static int MIN_METEOR_SPAWN_DISTANCE = 2; //As in a radius of blocks around the player in which the meteor won't spawn in (but remember that it can have an angled trajectory)
    public static int MAX_METEOR_SPAWN_DISTANCE = 30; //TODO probably increase it back to 50?

    public static int METEOR_SPAWN_HEIGHT = 300; //At which y level should meteors spawn?

    public static boolean SHOULD_BYPASS_LEAVES = true; //Should the meteor bypass leaves instead of exploding midair om them?

    //TODO how should this work? A percentage? A fraction? For now i think i'm gonna go with 1 on the number here.
    public static int METEOR_SPAWN_CHANCE = 20000;

    public static boolean MODIFY_SPAWN_CHANCE_AT_NIGHT = false;
    public static int METEOR_NIGHT_SPAWN_CHANCE = 10000;

    public static boolean SHOULD_COOLDOWN_BETWEEN_METEORS = true;
    public static int MIN_METEOR_COOLDOWN_TIME = 20; //In seconds

    //TODO this is VERY WIP and only works if the player is rather far down from where the meteor spawns in. Like i might delete this instead
    public static boolean HOMING_METEORS = false; //These would not spawn with a random direction but aim towards a selected player

    public static boolean ANNOUNCE_METEOR_SPAWN = false;
    public static boolean ANNOUNCE_METEOR_DESTROYED = false;
    public static boolean ACTIONBAR_ANNOUNCEMENTS = true;

    public static boolean METEOR_GRIEFING = true;
    public static boolean SCATTER_METEOR_GRIEFING = true;

    public static boolean METEOR_STRUCTURE = true;
    public static boolean SCATTER_METEOR_STRUCTURE = true;

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
        configs.addKeyValuePair(new Pair<>("meteor_spawn_height", 300), "The world height (y level) at which meteors spawn in");
        configs.addKeyValuePair(new Pair<>("meteor_spawn_chance", 20000), "Expressed as '1 in <x>' chances of spawning a meteor. For example, by default it has a chance of 1 in 20000");
        configs.addKeyValuePair(new Pair<>("modify_spawn_chance_at_night", false),"Should the spawn rate be different during the night?");
        configs.addKeyValuePair(new Pair<>("meteor_night_spawn_chance", 10000),"The chance for a meteor to spawn at night if enabled. Expressed as in 1 in x chances.");

        configs.addKeyValuePair(new Pair<>("natural_meteor_min_size", 1),"The smallest size a natural meteor can have when spawned in. Cannot go below 1");
        configs.addKeyValuePair(new Pair<>("natural_meteor_max_size", 5),"The biggest size a natural meteor can have when spawned in. Cannot go above 50.");

        configs.addKeyValuePair(new Pair<>("spacer", "spacer"), "");

        configs.addKeyValuePair(new Pair<>("should_bypass_leaves", true),"Should meteors bypass leaves blocks instead of colliding with them midair?");
        configs.addKeyValuePair(new Pair<>("homing_meteors", false),"Should meteors be (more or less) directed towards the nearest player?");

        configs.addKeyValuePair(new Pair<>("announce_meteor_spawn", false),"Should players get a message in chat/hotbar when a meteor spawns?");
        configs.addKeyValuePair(new Pair<>("announce_meteor_destroyed", false),"Should players get a message in chat/hotbar when a meteor is destroyed?");
        configs.addKeyValuePair(new Pair<>("actionbar_announcements", true),"Should the above announcement be displayed above the hotbar in the actionbar or in chat?");


        configs.addKeyValuePair(new Pair<>("spacer", "spacer"), "");

        configs.addKeyValuePair(new Pair<>("should_cooldown_between_meteors", true),"Should there be a cooldown between a meteor spawning one meteor and then another?");
        configs.addKeyValuePair(new Pair<>("min_meteor_cooldown_time", 20),"The minimum time interval (in seconds) between spawning a meteor and then another");

        configs.addKeyValuePair(new Pair<>("meteor_griefing", true),"Should meteors be able to destroy blocks on impact?");
        configs.addKeyValuePair(new Pair<>("scatter_meteor_griefing", true),"Should the meteors that come out of a bigger meteor when it's broken be able to destroy blocks on impact?");

        configs.addKeyValuePair(new Pair<>("meteor_structure", true),"Should meteors spawn the meteor structure after impact?");
        configs.addKeyValuePair(new Pair<>("scatter_meteor_structure", true),"Should the meteors that come out of a bigger meteor when it's broken be able to destroy spawn structures on impact?");


    }

    public static void reloadConfig(){
        registerConfigs();
        LOGGER.info("All " + configs.getConfigsList().size() + " have been reloaded properly");

    }

    private static void assignConfigs() {

        VERSION = CONFIG.getOrDefault("version", ver);

        MIN_METEOR_SPAWN_DISTANCE = CONFIG.getOrDefault("min_meteor_spawn_distance", 2);
        MAX_METEOR_SPAWN_DISTANCE = CONFIG.getOrDefault("max_meteor_spawn_distance", 30);
        MIN_METEOR_SPAWN_DISTANCE = CONFIG.getOrDefault("min_meteor_spawn_distance", 2);
        METEOR_SPAWN_HEIGHT = CONFIG.getOrDefault("meteor_spawn_height", 300);
        METEOR_SPAWN_CHANCE = CONFIG.getOrDefault("meteor_spawn_chance", 20000);
        MODIFY_SPAWN_CHANCE_AT_NIGHT = CONFIG.getOrDefault("modify_spawn_chance_at_night", false);
        METEOR_NIGHT_SPAWN_CHANCE = CONFIG.getOrDefault("meteor_night_spawn_chance", 10000);
        NATURAL_METEOR_MIN_SIZE = CONFIG.getOrDefault("natural_meteor_min_size", 1);
        NATURAL_METEOR_MAX_SIZE = CONFIG.getOrDefault("natural_meteor_max_size", 5);

        SHOULD_BYPASS_LEAVES = CONFIG.getOrDefault("should_bypass_leaves", true);
        HOMING_METEORS = CONFIG.getOrDefault("homing_meteors", false);

        SHOULD_COOLDOWN_BETWEEN_METEORS = CONFIG.getOrDefault("should_cooldown_between_meteors", true);
        MIN_METEOR_COOLDOWN_TIME = CONFIG.getOrDefault("min_meteor_cooldown_time", 20);

        ANNOUNCE_METEOR_SPAWN = CONFIG.getOrDefault("announce_meteor_spawn", false);
        ANNOUNCE_METEOR_DESTROYED = CONFIG.getOrDefault("announce_meteor_destroyed", false);

        ACTIONBAR_ANNOUNCEMENTS = CONFIG.getOrDefault("actionbar_announcements", true);

        METEOR_GRIEFING = CONFIG.getOrDefault("meteor_griefing", true);
        SCATTER_METEOR_GRIEFING = CONFIG.getOrDefault("scatter_meteor_griefing", true);
        METEOR_STRUCTURE = CONFIG.getOrDefault("meteor_structure", true);
        SCATTER_METEOR_STRUCTURE = CONFIG.getOrDefault("scatter_meteor_structure", true);
    }
}

