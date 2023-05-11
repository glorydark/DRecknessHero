package testgame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.*;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import gameapi.GameAPI;
import gameapi.arena.Arena;
import gameapi.effect.EasyEffect;
import gameapi.listener.base.GameListenerRegistry;
import gameapi.room.Room;
import gameapi.room.RoomRule;
import gameapi.room.RoomStatus;
import gameapi.utils.Language;
import testgame.scripts.CustomSkill;
import testgame.scripts.TriggerListener;

import java.io.File;
import java.util.*;

public class MainClass extends PluginBase {

    public static List<Room> roomListHashMap = new ArrayList<>();
    public static List<String> maps = new ArrayList<>();
    public static HashMap<String, List<EasyEffect>> effectHashMap = new HashMap<>();

    public static HashMap<String, CustomSkill> skills = new HashMap<>();
    public static String path = null;
    public static Map<String, Object> scoreboardCfg = new HashMap<>();
    public static boolean skillEnabled;

    public static boolean enableScoreboard = false;

    public static Language language = new Language("DRecknessHero");

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onDisable() {
        roomListHashMap.clear();
        maps.clear();
        effectHashMap.clear();
        scoreboardCfg.clear();
        super.onDisable();
    }

    @Override
    public void onEnable() {
        this.getLogger().info("RecknessHero enabled!");
        this.getLogger().info("This plugin is free of charge. You can find it on MineBBS. Author:Glorydark！");
        path = getDataFolder().getPath();
        this.loadLanguage();
        this.saveResource("blockaddons.yml",false);
        this.saveResource("rooms.yml",false);
        this.saveResource("maps.yml",false);
        this.saveResource("skills.yml",false);
        this.saveResource("scoreboard.yml",false);
        this.loadRooms();
        this.loadBlockAddons();
        this.loadScoreboardSetting();
        this.saveResource("skills/DRecknessHero_SpeedUp.json", false);
        this.saveResource("skills/DRecknessHero_JumpBoost.json", false);
        this.saveResource("skills/DRecknessHero_Builder.json", false);
        Config config = new Config(path+"/skills.yml");
        skillEnabled = config.getBoolean("enabled", true);
        if(skillEnabled) {
            this.getServer().getPluginManager().registerEvents(new TriggerListener(), this);
            for (String fileName : new ArrayList<>(config.getStringList("scripts"))) {
                File file = new File(this.getDataFolder().getPath() + "/skills/" + fileName + ".json");
                if (file.exists()) {
                    CustomSkill skill = new CustomSkill(file);
                    skill.loadSkill();
                }
            }
        }
        Config mapsCfg = new Config(path+"/maps.yml");
        maps.addAll(mapsCfg.getKeys(false));
        GameAPI.addLoadedGame("DRecknessHero");

        this.getServer().getPluginManager().registerEvents(new BaseEvent(),this);
        GameListenerRegistry.registerEvents("DRecknessHero", new GameEvent(), this);
        this.getServer().getCommandMap().register("DRecknessHero",new GameCommand("drh"));
    }

    public void loadLanguage(){
        this.saveResource("languages/zh_CN.properties", false);
        this.saveResource("languages/en_US.properties", false);
        language.addLanguage(new File(path+"/languages/zh_CN.properties"));
        language.addLanguage(new File(path+"/languages/en_US.properties"));
        language.setDefaultLanguage("zh_CN");
    }

    public void loadScoreboardSetting(){
        this.getLogger().info(language.getText("scoreboard.setting.loading"));
        Config config = new Config(this.getDataFolder()+"/scoreboard.yml",Config.YAML);
        scoreboardCfg = config.getAll();
        enableScoreboard = config.getBoolean("enabled", false);
        this.getLogger().info(language.getText("scoreboard.setting.loadedSuccessfully"));
    }

    public static String getScoreboardSetting(String key){
        if(scoreboardCfg.containsKey(key)){
            return String.valueOf(scoreboardCfg.get(key));
        }else{
            return "null";
        }
    }

    public void loadBlockAddons(){
        Config config = new Config(this.getDataFolder()+"/blockaddons.yml",Config.YAML);
        effectHashMap = new HashMap<>();
        for(String string: config.getKeys(false)){
            this.getLogger().info(language.getText("blockAddon.item.loading", string));
            for(Room room:roomListHashMap){
                RoomRule roomRule = room.getRoomRule();
                roomRule.canBreakBlocks.add(string);
                room.setRoomRule(roomRule);
                this.getLogger().info(language.getText("blockAddon.item.loadedSuccessfully", string));
            }
            List<EasyEffect> effectList = new ArrayList<>();
            for(String effectStr: config.getStringList(string+".effects")) {
                String[] effectSplit = effectStr.split(":");
                if(effectSplit.length == 3) {
                    EasyEffect effect = new EasyEffect(Integer.parseInt(effectSplit[0]),Integer.parseInt(effectSplit[1]),Integer.parseInt(effectSplit[2]));
                    this.getLogger().info(effect.toString());
                    effectList.add(effect);
                }
            }
            effectHashMap.put(string,effectList);
        }
        this.getLogger().info(language.getText("blockAddon.all.loaded"));
    }

    public void loadRooms(){
        Config config = new Config(path+"/rooms.yml",Config.YAML);
        for(String s:config.getKeys(false)) {
            this.loadRoom(config.getString(s+".map", ""), s,config.getInt(s+".min", 1),config.getInt(s+".max", 16));
        }
    }

    public void loadRoom(String map, String roomName, Integer min, Integer max){
        RoomRule roomRule = new RoomRule(0);
        Config config = new Config(path+"/maps.yml", Config.YAML);
        roomRule.canBreakBlocks.add("100:14");
        roomRule.canPlaceBlocks.add("152:0");
        roomRule.allowFoodLevelChange = false;
        roomRule.canBreakBlocks.addAll(effectHashMap.keySet());
        Room room = new Room("DRecknessHero", roomRule, "", 1);
        room.setRoomName(roomName);
        if (config.exists(map + ".LoadWorld")) {
            String backup = config.getString(map + ".LoadWorld");
            room.setRoomLevelBackup(backup);
            String newName = room.getGameName() + "_" + backup + "_" + UUID.randomUUID();
            if (Arena.copyWorldAndLoad(newName, backup)) {
                if (Server.getInstance().isLevelLoaded(newName)) {
                    Server.getInstance().getLevelByName(newName).setAutoSave(false);
                    this.getLogger().info(language.getText("room.loading", backup));

                    if(config.exists(map+".WaitSpawn")){
                        room.setWaitSpawn(config.getString(map+".WaitSpawn").replace(backup, newName));
                    }else{
                        this.getLogger().info(language.getText("room.loadedFailed.error.waitSpawn", map));
                        return;
                    }

                    if(config.exists(map+".StartSpawn")){
                        room.addStartSpawn(config.getString(map+".StartSpawn").replace(backup, newName));
                    }else{
                        this.getLogger().info(language.getText("room.loadedFailed.error.startSpawn", map));
                        return;
                    }

                    if(config.exists(map+".WaitTime")){
                        room.setWaitTime(config.getInt(map+".WaitTime"));
                    }else{
                        this.getLogger().info(language.getText("room.loadedFailed.error.waitTime", map));
                        return;
                    }

                    if(config.exists(map+".GameTime")){
                        room.setGameTime(config.getInt(map+".GameTime"));
                    }else{
                        this.getLogger().info(language.getText("room.loadedFailed.error.gameTime", map));
                        return;
                    }
                    room.setMinPlayer(min);
                    room.setMaxPlayer(max);
                    room.setEndSpawn(Server.getInstance().getDefaultLevel().getSpawnLocation().getLocation());
                    Room.loadRoom(room);
                    room.setPlayLevel(Server.getInstance().getLevelByName(newName));
                    roomListHashMap.add(room);
                    room.setRoomStatus(RoomStatus.ROOM_STATUS_WAIT);
                    room.setWinConsoleCommands(new ArrayList<>(config.getStringList(map+".WinCommands")));
                    room.setLoseConsoleCommands(new ArrayList<>(config.getStringList(map+".FailCommands")));
                    this.getLogger().info(language.getText("room.loadedSuccessfully", map));
                } else {
                    this.getLogger().info(language.getText("room.loadedFailed.error.loadMap", map));
                }
            } else {
                this.getLogger().info(language.getText("room.loadedFailed.error.copyMap", map));
            }
        } else {
            this.getLogger().info(language.getText("room.loadedFailed.error.mapNotFound", map));
        }
    }

    public static boolean processJoin(Room room, Player p){
        if(room != null){
            if(room.addPlayer(p)) {
                p.getInventory().clearAll();
                p.getUIInventory().clearAll();
                p.setGamemode(2);
                Item addItem1 = new ItemBookEnchanted();
                addItem1.setCustomName(language.getText("room.joinItem.quit.name"));
                p.getInventory().setItem(0, addItem1);

                Item addItem2 = new ItemEmerald();
                addItem2.setCustomName(language.getText("room.joinItem.history.name"));
                p.getInventory().setItem(7, addItem2);

                if (skillEnabled) {
                    room.setPlayerProperties(p.getName(), "skill1", "DRecknessHero_SpeedUp");
                    Item addItem3 = new ItemTotem(0);
                    addItem3.setCustomName(language.getText("room.joinItem.jobSelector.name"));
                    p.getInventory().setItem(8, addItem3);
                }

                if (room.getTemporary()) {
                    Item addItem4 = new ItemPaper(0);
                    addItem4.setCustomName(language.getText("room.joinItem.mapSelector.name"));
                    p.getInventory().setItem(1, addItem4);
                }
                return true;
            }
        }else{
            p.sendMessage(language.getText("error.roomNotFound"));
        }
        return false;
    }

    public static List<EasyEffect> getBlockAddonsInit(Block block){
        int blockid = block.getId();
        int blockmeta = block.getDamage();
        String s = blockid+":"+blockmeta;
        for(String string: MainClass.effectHashMap.keySet()){
            if(s.equals(string)){
                return MainClass.effectHashMap.get(string);
            }
        }
        return null;
    }
}
