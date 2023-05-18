package testgame;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import gameapi.GameAPI;
import gameapi.room.Room;
import gameapi.room.RoomRule;

import java.util.HashMap;
import java.util.Map;

public class GameCommand extends Command {
    public GameCommand(String name) {
        super(name,"暴走英雄","/drh");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if(strings.length > 0){
            switch (strings[0]){
                case "joinrandom":
                    if(sender.isPlayer()) {
                        Player player = (Player) sender;
                        for (Room room : MainClass.roomListHashMap) {
                            if (room.isTemporary()) {
                                if(MainClass.processJoin(room, player)) {
                                    return true;
                                }
                            }
                        }
                        MainClass.processJoin(loadRoom(), player);
                    }
                    break;
                case "join":
                    if(strings.length > 1) {
                        if (Server.getInstance().getPlayer(sender.getName()) != null) {
                            Player player = Server.getInstance().getPlayer(sender.getName());
                            Room room = Room.getRoom("DRecknessHero", strings[1]);
                            MainClass.processJoin(room, player);
                        } else {
                            sender.sendMessage(MainClass.language.getTranslation("error.useInGame"));
                        }
                    }
                    break;
                case "quit":
                    if (Server.getInstance().getPlayer(sender.getName()) != null) {
                        Player player = Server.getInstance().getPlayer(sender.getName());
                        Room room = Room.getRoom(player);
                        if(room != null){
                            room.removePlayer(player, GameAPI.saveBag);
                            player.sendMessage(MainClass.language.getTranslation(player, "command.quit.success"));
                        }else{
                            player.sendMessage(MainClass.language.getTranslation(player, "command.quit.failed"));
                        }
                    } else {
                        sender.sendMessage(MainClass.language.getTranslation("error.useInGame"));
                    }
                    break;
                case "list":
                    sender.sendMessage(MainClass.language.getTranslation(sender, "command.list.title"));
                    for(Room room:MainClass.roomListHashMap){
                        switch (room.getRoomStatus()){
                            case ROOM_STATUS_Ceremony:
                            case ROOM_STATUS_GameEnd:
                            case ROOM_STATUS_End:
                                sender.sendMessage(MainClass.language.getTranslation(sender, "command.list.item.endApproaching", room.getRoomName()));
                                break;
                            case ROOM_STATUS_PreStart:
                            case ROOM_STATUS_GameStart:
                                sender.sendMessage(MainClass.language.getTranslation(sender, "command.list.item.start", room.getRoomName()));
                                break;
                            case ROOM_STATUS_GameReadyStart:
                            case ROOM_STATUS_NextRoundPreStart:
                            case ROOM_STATUS_WAIT:
                                sender.sendMessage(MainClass.language.getTranslation(sender, "command.list.item.wait", room.getRoomName(),room.getPlayers().size(),room.getMaxPlayer()));
                                break;
                        }
                    }
                    break;
                case "help":
                    sender.sendMessage(MainClass.language.getTranslation(sender, "command.help.title"));
                    sender.sendMessage(MainClass.language.getTranslation(sender, "command.help.join"));
                    sender.sendMessage(MainClass.language.getTranslation(sender, "command.help.quit"));
                    sender.sendMessage(MainClass.language.getTranslation(sender, "command.help.list"));
                    break;
            }
        }else{
            if (Server.getInstance().getPlayer(sender.getName()) != null) {
                Window.showPlayerRoomListWindow((Player) sender);
            } else {
                sender.sendMessage(MainClass.language.getTranslation("error.useInGame"));
            }
        }
        return true;
    }

    public Room loadRoom(){
        RoomRule roomRule = new RoomRule(0);
        roomRule.allowBreakBlock = false;
        roomRule.allowPlaceBlock = false;
        roomRule.allowFallDamage = false;
        roomRule.allowDamagePlayer = false;
        roomRule.allowHungerDamage = false;
        roomRule.allowFoodLevelChange = false;
        roomRule.canBreakBlocks.add("100:14");
        roomRule.canPlaceBlocks.add("152:0");
        roomRule.canBreakBlocks.addAll(MainClass.effectHashMap.keySet());
        Room room = new Room("DRecknessHero", roomRule, "", 1);
        room.setTemporary(true);
        room.setResetMap(false);
        room.setRoomRule(roomRule);
        room.setMinPlayer(1);
        room.setWaitTime(10);
        room.setGameWaitTime(10);
        Map<String, Integer> mapRanks = new HashMap<>();
        for(String mapName: MainClass.maps){
            mapRanks.put(mapName, 0);
        }
        room.setRoomProperties("mapRanks", mapRanks);
        MainClass.roomListHashMap.add(room);
        Room.loadRoom(room);
        return room;
    }
}
