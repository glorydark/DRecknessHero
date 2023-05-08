package testgame;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowSimple;
import gameapi.room.Room;
import testgame.scripts.CustomSkill;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BaseEvent implements Listener {
    public static ConcurrentHashMap<Player, FormWindowSimple> playerFormWindowSimpleHashMap = new ConcurrentHashMap<>();

    @EventHandler
    public void GuiRespondedEvent(PlayerFormRespondedEvent event) {
        if (event.getResponse() == null) {
            return;
        }
        if (BaseEvent.playerFormWindowSimpleHashMap.containsKey(event.getPlayer())) {
            if (event.getWindow() != BaseEvent.playerFormWindowSimpleHashMap.get(event.getPlayer())) {
                return;
            }
            if (!(event.getWindow() instanceof FormWindowSimple)) {
                return;
            }
            playerFormWindowSimpleHashMap.remove(event.getPlayer());
            String title = ((FormWindowSimple) event.getWindow()).getTitle();
            FormResponseSimple formResponseSimple = (FormResponseSimple) event.getResponse();
            Player player = event.getPlayer();
            switch (title) {
                case "§l§e选择房间":
                    Room room = Room.getRoom("DRecknessHero", formResponseSimple.getClickedButton().getText());
                    MainClass.processJoin(room, player);
                    break;
                case "§l§e选择技能":
                    Room room1 = Room.getRoom("DRecknessHero", player);
                    if (room1 != null) {
                        player.sendMessage("您已选择技能: " + formResponseSimple.getClickedButton().getText());
                        room1.setPlayerProperties(player.getName(), "skill1", MainClass.skills.values().toArray(new CustomSkill[0])[formResponseSimple.getClickedButtonId()].getIdentifier());
                    }
                    break;
                case "§l§e选择地图":
                    Room room2 = Room.getRoom("DRecknessHero", player);
                    if (room2 != null) {
                        Map<String, Integer> map = (Map<String, Integer>) room2.getRoomProperties("mapRanks");
                        map.put(formResponseSimple.getClickedButton().getText(), map.getOrDefault(formResponseSimple.getClickedButton().getText(), 0) + 1);
                        room2.setRoomProperties("mapRanks", map);
                        player.sendMessage("您为地图【" + formResponseSimple.getClickedButton().getText() + "】投上一票！");
                        player.getInventory().setItem(1, new BlockAir().toItem());
                    }
                    break;
            }
        }
    }
}
