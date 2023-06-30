package cc.seati.seatic;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import org.slf4j.Logger;

@Mod(SeatiCore.MODID)
public class SeatiCore {
    public static final String MODID = "seatic";
    public static final Log LOG = new Log();
    public static int uptime = 0;
    public static boolean ready = false;
    public static MinecraftServer server;
    public static Server http;
    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("BusyWait")
    private static final Thread timerThread = new Thread(() -> {
        while(true) {
            uptime++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    });

    public SeatiCore() {
        timerThread.start();
        http = new Server(9090);
        MinecraftForge.EVENT_BUS.register(Events.class);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }
}
