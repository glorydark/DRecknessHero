package testgame;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import com.sun.istack.internal.NotNull;
import gameapi.GameAPI;
import gameapi.room.Room;
import gameapi.utils.GameRecord;
import testgame.scripts.CustomSkill;

public class Window {
    public static void showPlayerRoomListWindow(@NotNull Player player){
        if(!BaseEvent.playerFormWindowSimpleHashMap.containsKey(player)) {
            FormWindowSimple simple = new FormWindowSimple(MainClass.language.getTranslation(player, "form.roomSelector.join"), MainClass.language.getTranslation(player, "form.roomSelector.content"));
            for (Room room : MainClass.roomListHashMap) {
                simple.addButton(new ElementButton(room.getRoomName()));
            }
            BaseEvent.playerFormWindowSimpleHashMap.put(player, simple);
            player.showFormWindow(simple);
        }
    }

    public static void showPlayerSkillSelectWindow(@NotNull Player player) {
        if(!BaseEvent.playerFormWindowSimpleHashMap.containsKey(player)) {
            FormWindowSimple simple = new FormWindowSimple(MainClass.language.getTranslation(player, "room.joinItem.jobSelector.name"), MainClass.language.getTranslation(player, "form.jobSelector.content"));
            for(CustomSkill skill: MainClass.skills.values()){
                simple.addButton(new ElementButton(skill.getCustomName()));
            }
            BaseEvent.playerFormWindowSimpleHashMap.put(player, simple);
            player.showFormWindow(simple);
        }
    }

    public static void showPlayerHistoryWindow(@NotNull Player player){
        if(!BaseEvent.playerFormWindowSimpleHashMap.containsKey(player)) {
            FormWindowSimple window = new FormWindowSimple(MainClass.language.getTranslation(player, "room.joinItem.history.name"), "");
            window.setContent(MainClass.language.getTranslation(player, "form.history.content", GameRecord.getGameRecord("DRecknessHero", player.getName(), "win"), GameRecord.getGameRecord("DRecknessHero", player.getName(), "lose")));
            BaseEvent.playerFormWindowSimpleHashMap.put(player, window);
            player.showFormWindow(window);
        }
    }

    public static void showVoteForMap(@NotNull Player player){
        if(!BaseEvent.playerFormWindowSimpleHashMap.containsKey(player)) {
            FormWindowSimple window = new FormWindowSimple(MainClass.language.getTranslation(player, "room.joinItem.mapSelector.name"), MainClass.language.getTranslation(player, "form.mapSelector.content"));
            for(String map: MainClass.maps){
                window.addButton(new ElementButton(map));
            }
            BaseEvent.playerFormWindowSimpleHashMap.put(player, window);
            player.showFormWindow(window);
        }
    }
}
