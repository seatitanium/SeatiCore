package cc.seati.seatic;

import cc.seati.seatic.Enums.State;
import com.mojang.logging.LogUtils;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static final Logger logger = LogUtils.getLogger();
    public static final Log fileLogger = new Log();

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
        public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

        public static String getFormattedDate() {
            return sdf.format(new Date());
        }
    }

    public static class files {
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
    }

    public static class resp {
        public static JSONObject buildResponse(State state, String msg, int number) {
            var object = new JSONObject();
            object.put("state", state.toString());
            object.put("msg", msg);
            object.put("data", number);
            return object;
        }

        public static JSONObject buildResponse(State state, String msg, JSONObject data) {
            var object = new JSONObject();
            object.put("state", state.toString());
            object.put("msg", msg);
            object.put("data", data.toString());
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
}
