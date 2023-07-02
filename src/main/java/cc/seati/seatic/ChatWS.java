package cc.seati.seatic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@WebSocket
public class ChatWS {
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    @OnWebSocketConnect
    public void connected(Session s) {
        s.setIdleTimeout(1800000L); // 30 mins
        Utils.log.info("ws: Session started for " + s.getRemoteAddress().getAddress().getHostAddress() + ".");
        sessions.add(s);
    }

    @OnWebSocketClose
    public void closed(Session s, int statusCode, String reason) {
        Utils.log.info(String.format("ws: Session closed with statusCode: %s and reason: %s.", statusCode, reason));
        sessions.remove(s);
    }

    @OnWebSocketError
    public void error(Session s, Throwable e) {
        e.printStackTrace();
        Utils.log.error("ws: An error occurred. Details: " + e.getMessage());
    }

    @OnWebSocketMessage
    public void message(Session s, String message) {
        JSONObject object;
        try {
            object = new JSONObject(message);
        } catch (JSONException e) {
            e.printStackTrace();
            try {
                s.getRemote().sendString("ws: The json string you sent is malformed.");
            } catch (IOException r) {
                r.printStackTrace();
                Utils.log.error("ws: Unable to send error message to remote.");
            }
            return;
        }
        var username = object.getString("username");
        var text = object.getString("text");

        SeatiCore.server.getPlayerList().getPlayers().forEach(p -> {
            p.sendSystemMessage(
                    Component.empty()
                            .withStyle(ChatFormatting.GREEN)
                            .append(Component.literal("[Web] ")
                                    .withStyle(ChatFormatting.RESET)
                                    .append(Component.literal(String.format("<%s> ", username)))
                                    .append(Component.literal(text))
                            ));
        });
    }

    public static void sendChatMessage(String msg) {
        sessions.forEach(s -> {
            try {
                s.getRemote().sendString(msg);
            } catch (IOException e) {
                e.printStackTrace();
                Utils.log.error("ws: Failed to send some message to remote.");
            }
        });
    }
}
