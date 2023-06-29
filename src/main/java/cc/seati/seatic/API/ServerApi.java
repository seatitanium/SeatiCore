package cc.seati.seatic.API;

import cc.seati.seatic.Enums.State;
import cc.seati.seatic.SeatiCore;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import static cc.seati.seatic.Utils.resp.*;

public class ServerApi {
    public static @NotNull JSONObject getMods(Request q, Response a) {
        var modIds = ModList.get().getMods().stream().map(IModInfo::getModId).toList();
        return buildResponse(State.OK, "", new JSONObject(modIds));
    }

    public static @NotNull JSONObject getUptime(Request q, Response a) {
        return buildResponse(State.OK, "", SeatiCore.uptime);
    }

    public static @NotNull JSONObject getPlayerCount(Request q, Response a) {
        if (SeatiCore.ready) {
            var server = SeatiCore.server;
            return buildResponse(State.OK, "", server.getPlayerCount());
        } else {
            return buildResponse(State.NG, "Server is not ready.");
        }
    }

    public static @NotNull JSONObject getVersion(Request q, Response a) {
        var mcAndForgeVersion = FMLLoader.versionInfo().mcAndForgeVersion();
        return buildResponse(State.OK, "", new JSONObject(mcAndForgeVersion));
    }

}
