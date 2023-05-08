package testgame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.item.*;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import gameapi.arena.Arena;
import gameapi.effect.EasyEffect;
import gameapi.event.block.RoomBlockBreakEvent;
import gameapi.event.player.RoomPlayerInteractEvent;
import gameapi.event.room.RoomEndEvent;
import gameapi.event.room.RoomGameEndEvent;
import gameapi.event.room.RoomGameStartEvent;
import gameapi.listener.RoomGameProcessingListener;
import gameapi.listener.RoomPreStartListener;
import gameapi.listener.base.annotations.GameEventHandler;
import gameapi.listener.base.interfaces.GameListener;
import gameapi.room.Room;
import gameapi.room.RoomStatus;
import gameapi.scoreboard.ScoreboardTools;
import gameapi.sound.SoundTools;
import gameapi.utils.GameRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static testgame.MainClass.path;

public class GameEvent implements GameListener {

    @GameEventHandler
    public void RoomGameStartEvent(RoomGameStartEvent event){
        Room room = event.getRoom();
        for(Player p:room.getPlayers()){
            Item pickaxe = Item.get(Item.DIAMOND_PICKAXE);
            pickaxe.setCount(1);
            pickaxe.setCustomName("英雄之镐");
            p.getInventory().setItem(0, pickaxe);
            if(MainClass.skillEnabled) {
                MainClass.skills.get((String) room.getPlayerProperties(p.getName(), "skill1")).giveSkillItem(p, true);
            }
            event.getRoom().setRoomProperties("drh_passedPlayers", new ArrayList<>());
            SoundTools.playResourcePackOggMusic(p, "game_begin");
        }
    }

    @GameEventHandler
    public void ceremony(RoomGameEndEvent event){
        List<Player> players = (List<Player>) event.getRoom().getRoomProperties("drh_passedPlayers");
        for(Player p: event.getRoom().getPlayers()){
            if(players.contains(p)){
                GameRecord.addGameRecord("DRecknessHero",p.getName(), "win",1);
                p.sendMessage("§l§e您已成功完成比赛 §l§a"+GameRecord.getGameRecord("DRecknessHero",p.getName(),"win")+" §l§e次");
                p.sendTitle("比赛结束","恭喜您获得了第"+(players.indexOf(p)+1)+"名",10,20,10);
                SoundTools.playResourcePackOggMusic(p,"winning");
                event.getRoom().executeWinCommands(p);
            }else{
                GameRecord.addGameRecord("DRecknessHero",p.getName(), "lose",1);
                p.sendMessage("§l§e您未完成比赛 §l§c"+GameRecord.getGameRecord("DRecknessHero",p.getName(),"lose")+" §l§e次");
                p.sendTitle("比赛结束","您未完成比赛！",10,20,10);
                SoundTools.playResourcePackOggMusic(p, "game_over");
                event.getRoom().executeLoseCommands(p);
            }
        }
    }

    @GameEventHandler
    public void end(RoomEndEvent event){
        if(event.getRoom().getTemporary()){
            if(event.getRoom().getTemporary()){
                MainClass.roomListHashMap.remove(event.getRoom());
            }
        }
    }

    @GameEventHandler
    public void RoomGameProcessingListener(RoomGameProcessingListener event){
        Room room = event.getRoom();
        int lastSec = room.getGameTime() - room.getTime();
        for(Player p:room.getPlayers()){
            if(lastSec < room.getGameTime()){
                if (((List<Player>) event.getRoom().getRoomProperties("drh_passedPlayers")).contains(p)) {
                    if(MainClass.enableScoreboard) {
                        ScoreboardTools.drawScoreBoardEntry(p, MainClass.getScoreboardSetting("scoreboard_objective_name"), MainClass.getScoreboardSetting("scoreboard_display_name"), MainClass.getScoreboardSetting("rank_format").replace("%rank%", String.valueOf(((List<Player>) event.getRoom().getRoomProperties("drh_passedPlayers")).indexOf(p) + 1)), MainClass.getScoreboardSetting("time_format").replace("%time%", ScoreboardTools.secToTime(lastSec)));
                    }else{
                        p.sendActionBar("剩余时间：" +ScoreboardTools.secToTime(lastSec) + "\n您获得了第"+(((List<Player>) event.getRoom().getRoomProperties("drh_passedPlayers")).indexOf(p) + 1)+"名");
                    }
                } else {
                    if(MainClass.enableScoreboard) {
                        ScoreboardTools.drawScoreBoardEntry(p, MainClass.getScoreboardSetting("scoreboard_objective_name"), MainClass.getScoreboardSetting("scoreboard_display_name"), MainClass.getScoreboardSetting("time_format").replace("%time%", ScoreboardTools.secToTime(lastSec)));
                    }else{
                        p.sendActionBar("剩余时间：" + ScoreboardTools.secToTime(lastSec));
                    }
                }
            }
        }
    }

    @GameEventHandler
    public void RoomPreStartListener(RoomPreStartListener event){
        Room room = event.getRoom();
        if(room.getWaitTime() - room.getTime() == 5){
            if(room.getTemporary()) {
                Map<String, Integer> map = (Map<String, Integer>) room.getRoomProperties("mapRanks");
                List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
                list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
                if (list.size() > 0) {
                    Map.Entry<String, Integer> first = list.get(0);
                    if(loadRoomMap(room, first.getKey())) {
                        room.getPlayers().forEach(player -> {
                            player.sendMessage("已选择地图： "+ first.getKey()+"【"+first.getValue()+"票】");
                            player.teleportImmediate(room.getWaitSpawn().getLocation(), null);
                        });
                    }else{
                        room.setRoomStatus(RoomStatus.ROOM_STATUS_Ceremony);
                    }
                }else{
                    room.setRoomStatus(RoomStatus.ROOM_STATUS_Ceremony);
                }
            }
        }
    }

    public boolean loadRoomMap(Room room, String map){
        Config config = new Config(path+"/maps.yml", Config.YAML);
        if (config.exists(map + ".LoadWorld")) {
            String backup = config.getString(map + ".LoadWorld", "null");
            room.setRoomLevelBackup(backup);
            room.setRoomName(backup);
            if (Server.getInstance().getLevelByName(config.getString(map)) == null) {
                String newName = room.getGameName() + "_" + backup + "_" + UUID.randomUUID();
                if (Arena.copyWorldAndLoad(newName, backup)) {
                    if (Server.getInstance().isLevelLoaded(newName)) {
                        Server.getInstance().getLevelByName(newName).setAutoSave(false);
                        if(config.exists(map+".WaitSpawn")){
                            room.setWaitSpawn(config.getString(map+".WaitSpawn").replace(backup, newName));
                        }else{
                            return false;
                        }
                        if(config.exists(map+".StartSpawn")){
                            room.addStartSpawn(config.getString(map+".StartSpawn").replace(backup, newName));
                        }else{
                            return false;
                        }
                        if(!room.getTemporary()){
                            if(config.exists(map+".WaitTime")){
                                room.setWaitTime(config.getInt(map+".WaitTime"));
                            }else{
                                return false;
                            }
                        }
                        if(config.exists(map+".GameTime")){
                            room.setGameTime(config.getInt(map+".GameTime"));
                        }else{
                            return false;
                        }
                        room.setPlayLevel(Server.getInstance().getLevelByName(newName));
                        room.setEndSpawn(Server.getInstance().getDefaultLevel().getSpawnLocation().getLocation());
                        room.setWinConsoleCommands(new ArrayList<>(config.getStringList(map+".WinCommands")));
                        room.setLoseConsoleCommands(new ArrayList<>(config.getStringList(map+".FailCommands")));
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    //others
    @GameEventHandler
    public void touch(RoomPlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        Room room = event.getRoom();
        if (item instanceof ItemBookEnchanted && item.getCustomName().equals("§l§c退出房间")) {
            room.removePlayer(player, true);
            player.sendMessage("§l§c您已退出房间！");
            return;
        }

        if (item instanceof ItemEmerald && item.getCustomName().equals("§l§a历史战绩")) {
            Window.showPlayerHistoryWindow(player);
            return;
        }

        if (item instanceof ItemTotem && item.getCustomName().equals("§l§e选择职业")) {
            if (MainClass.skillEnabled) {
                Window.showPlayerSkillSelectWindow(player);
                return;
            }
        }

        if (item instanceof ItemPaper && item.getCustomName().equals("§l§e选择地图")) {
            Window.showVoteForMap(player);
            return;
        }

        if (event.getBlock().getId() == Block.EMERALD_BLOCK && room.getRoomStatus() == RoomStatus.ROOM_STATUS_GameStart) {
            List<Player> roomFinishPlayers = ((List<Player>) event.getRoom().getRoomProperties("drh_passedPlayers"));
            if (!roomFinishPlayers.contains(event.getPlayer())) {
                int lastSec = room.getGameTime() - room.getTime();
                ScoreboardTools.drawScoreBoardEntry(event.getPlayer(), MainClass.getScoreboardSetting("scoreboard_objective_name"), MainClass.getScoreboardSetting("scoreboard_display_name"), MainClass.getScoreboardSetting("rank_format").replace("%rank%", String.valueOf(((List<Player>) event.getRoom().getRoomProperties("drh_passedPlayers")).indexOf(event.getPlayer()) + 1)), MainClass.getScoreboardSetting("time_format").replace("%time%", ScoreboardTools.secToTime(lastSec)));
                if (room.getTime() < room.getGameTime() - 15) {
                    room.setTime(room.getGameTime() - 15);
                }
                for (Player p : room.getPlayers()) {
                    p.sendMessage(TextFormat.LIGHT_PURPLE + "%s 到达终点！".replace("%s", event.getPlayer().getName()));
                }
                ((List<Player>) event.getRoom().getRoomProperties("drh_passedPlayers")).add(player);
            }
        }
    }


    @GameEventHandler
    public void breakBlocks(RoomBlockBreakEvent event) {
        Room room = event.getRoom();
        if (room.getRoomStatus() != RoomStatus.ROOM_STATUS_GameStart) {
            return;
        }
        List<EasyEffect> effectList = MainClass.getBlockAddonsInit(event.getBlock());
        if (effectList != null) {
            for (EasyEffect effect : effectList) {
                effect.giveEffect(event.getPlayer());
                event.getPlayer().sendMessage("获得" + cn.nukkit.potion.Effect.getEffect(effect.getId()).getName() + "*" + effect.getAmplifier() + "*" + effect.getDuration() / 20 + "秒");
            }
        }
        if (event.getBlock().getId() == Block.RED_MUSHROOM_BLOCK) {
            Item item = Item.get(Block.REDSTONE_BLOCK);
            item.setCount(5);
            event.getPlayer().getInventory().addItem(item);
            event.getPlayer().sendMessage("获得5个屏障方块！");
        }
        event.setDrops(new Item[0]);
        event.setDropExp(0);
    }
}
