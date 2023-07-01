package cc.seati.seatic.API;

import static cc.seati.seatic.Utils.resp.*;

import cc.seati.seatic.Enums.State;
import cc.seati.seatic.Utils;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class DeviceApi {
    public static JSONObject ramLoad(Request q, Response a) {
        return buildResponse(State.OK, "", Utils.device.getServerRAMLoad() + "%");
    }

    public static JSONObject cpuLoad(Request q, Response a) {
        return buildResponse(State.OK, "", Utils.device.getServerCPULoad() + "%");
    }

    public static JSONObject getTps(Request q, Response a) {
        return buildResponse(State.OK, "", new JSONObject(Utils.device.getServerTpsOfAllDimensions()));
    }
}
