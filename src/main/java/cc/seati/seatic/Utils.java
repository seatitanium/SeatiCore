package cc.seati.seatic;

import cc.seati.seatic.Enums.State;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.ForgeCommand;
import org.json.JSONObject;
import org.slf4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {
    public static final Logger logger = LogUtils.getLogger();
    public static final Log fileLogger = new Log();

    @SuppressWarnings("BusyWait")
    public static class device {
        public static final SystemInfo si = new SystemInfo();
        public static final OperatingSystem os = si.getOperatingSystem();
        public static final HardwareAbstractionLayer hal = si.getHardware();
        public static final CentralProcessor cpu = hal.getProcessor();
        public static final GlobalMemory ram = hal.getMemory();
        public static double cpuLoad = 0.0;
        public static long serverUptime = 0;
        public static Thread cpuLoadCalcThread;
        public static Thread uptimeCalcThread;

        private static void startUptimeCalc() {
            uptimeCalcThread = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        serverUptime++;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Utils.log.error("Uptime calculation is interrupted.");
                }
            });
            uptimeCalcThread.start();
        }

        private static void startCpuLoadCalc() {
            cpuLoadCalcThread = new Thread(() -> {
                while (true) {
                    long[] prevTicks = cpu.getSystemCpuLoadTicks();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
                }
            });
            cpuLoadCalcThread.start();
        }

        private static long ramMax() {
            return ram.getTotal();
        }

        private static long ramAvail() {
            return ram.getAvailable();
        }

        private static long mean(long[] values) {
            return Arrays.stream(values).sum() / values.length;
        }

        public static long getServerUptime() {
            return serverUptime;
        }

        public static double getServerCPULoad() {
            return cpuLoad;
        }

        public static double getServerRAMLoad() {
            return (double) (1 - (ramAvail() / ramMax()));
        }

        // code from forge tps command
        public static double getServerTps(ServerLevel dimension) {
            var times = SeatiCore.server.getTickTime(dimension.dimension());
            if (times == null) {
                return 0D;
            }
            var meanTickTime = mean(SeatiCore.server.tickTimes) * 1.0e-6d;
            return Math.min(1000 / meanTickTime, 20);
        }

        public static Map<String, Double> getServerTpsOfAllDimensions() {
            var result = new HashMap<String, Double>();
            var dims = SeatiCore.server.getAllLevels();
            for (var l : dims) {
                result.put(l.dimension().toString(), getServerTps(l));
            }
            return result;
        }

    }

    public static class log {
        public static void info(String str) {
            logger.info(str);
            fileLogger.info(str);
        }

        public static void error(String str) {
            logger.error(str);
            fileLogger.error(str);
        }

        public static void warn(String str) {
            logger.warn(str);
            fileLogger.warn(str);
        }
    }

    public static class format {
        public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        public static final SimpleDateFormat sdfFile = new SimpleDateFormat("yyyy-MM-dd@HH_mm_ss");

        public static String getFormattedDate() {
            return sdf.format(new Date());
        }

        public static String getFormattedDateForFile() {
            return sdfFile.format(new Date());
        }
    }

    public static class files {
        // cwd does not end with /
        public static final String cwd = System.getProperty("user.dir");

        public static boolean touch(String url) throws IOException {
            var file = new File(url);
            if (!file.exists()) {
                return file.createNewFile();
            }
            return true;
        }

        public static boolean mkdir(String url) throws IOException {
            if (!url.endsWith("/")) {
                url += "/";
            }
            var dir = new File(url);
            if (!dir.exists()) {
                return dir.mkdirs();
            }
            return true;
        }

        public static int count(String url) {
            try {
                return Objects.requireNonNull(new File(cwd + url).list()).length;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }

    public static class resp {
        public static JSONObject buildResponse(State state, String msg, int number) {
            var object = new JSONObject();
            object.put("state", state.toString());
            object.put("msg", msg);
            object.put("data", number);
            return object;
        }

        public static JSONObject buildResponse(State state, String msg, String data) {
            var object = new JSONObject();
            object.put("state", state.toString());
            object.put("msg", msg);
            object.put("data", data);
            return object;
        }

        public static JSONObject buildResponse(State state, String msg, JSONObject data) {
            var object = new JSONObject();
            object.put("state", state.toString());
            object.put("msg", msg);
            object.put("data", data);
            return object;
        }

        public static JSONObject buildResponse(State state, String msg) {
            var object = new JSONObject();
            object.put("state", state.toString());
            object.put("msg", msg);
            return object;
        }

        public static String ng(String msg) {
            return buildResponse(State.NG, msg).toString();
        }

        public static String ok(String msg) {
            return buildResponse(State.OK, msg).toString();
        }
    }

    public static class player {
        public static boolean isOp(ServerPlayer p) {
            if (!SeatiCore.ready) {
                Utils.log.warn("Calling server instance before server was ready.");
                return false;
            }
            return SeatiCore.server.getOperatorUserPermissionLevel()
                    == SeatiCore.server.getProfilePermissions(p.getGameProfile());
        }
    }
}
