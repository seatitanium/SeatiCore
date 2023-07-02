package cc.seati.seatic;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.json.JSONObject;

public class Events {
    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent e) {
        SeatiCore.server = e.getServer();
        SeatiCore.ready = true;
        Utils.log.info("SeatiCore has got the server instance.");
    }

    @SubscribeEvent
    public static void serverStarting(ServerStartingEvent e) {
        Utils.log.info("""
                   
                   _____            __  _\s
                  / ___/___  ____ _/ /_(_)
                  \\__ \\/ _ \\/ __ `/ __/ /\s
                 ___/ /  __/ /_/ / /_/ / \s
                /____/\\___/\\__,_/\\__/_/  \s
                
                SeatiCore - v1.0.0
                """);
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent e) {
        var player = e.getPlayer();
        var object = new JSONObject();
        var posObject = new JSONObject();
        posObject.put("x", player.getX());
        posObject.put("y", player.getY());
        posObject.put("z", player.getZ());
        object.put("username", e.getUsername());
        object.put("message", e.getMessage());
        object.put("uuid", player.getStringUUID());
        object.put("isOp", Utils.server.isOpPlayer(player));
        object.put("position", posObject);
        ChatWS.sendChatMessage(object.toString());
    }
}
