package cc.seati.seatic;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Events {
    @SubscribeEvent
    public static void serverStarted(ServerStartedEvent e) {
        SeatiCore.server = e.getServer();
        SeatiCore.ready = true;
        Utils.log.info("Got server instance.");
    }

    @SubscribeEvent
    public static void serverStarting(ServerStartingEvent e) {
        Utils.log.info("""
                   
                   _____            __  _\s
                  / ___/___  ____ _/ /_(_)
                  \\__ \\/ _ \\/ __ `/ __/ /\s
                 ___/ /  __/ /_/ / /_/ / \s
                /____/\\___/\\__,_/\\__/_/  \s
                                         \s
                """);
        Utils.log.info("SeatiCore - v1.0.0");
    }
}
