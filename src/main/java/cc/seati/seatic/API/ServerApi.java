package cc.seati.seatic.API;

import cc.seati.seatic.Enums.State;
import cc.seati.seatic.SeatiCore;
import cc.seati.seatic.Utils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forgespi.language.IModInfo;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.util.*;

import static cc.seati.seatic.Utils.resp.*;

public class ServerApi {
    public static final MinecraftServer server = SeatiCore.server;
    public static final Boolean serverReady = SeatiCore.ready;

    public static class util {
        public static List<ServerPlayer> getOnlinePlayerList() {
            if (!serverReady) {
                return new ArrayList<>();
            }
            return server.getPlayerList().getPlayers().stream().filter(player -> !player.hasDisconnected()).toList();
        }

        public static boolean setWhitelistFor(String username) {
            return Utils.server.performCommand(String.format("whitelist add %s", username));
        }

        public static int countOfflinePlayer() {
            return Utils.files.count("/world/playerdata");
        }

        public static JSONObject getOnlinePlayerDetails(ServerPlayer p) {
            var objectMain = new JSONObject();
            /* objectMain construction */
            objectMain.put("name", p.getGameProfile().getName());
            objectMain.put("lastActionTime", p.getLastActionTime());
            objectMain.put("uuid", p.getStringUUID());
            objectMain.put("online", !p.hasDisconnected());
            objectMain.put("isOp", Utils.server.isOpPlayer(p));
            try {
                var lastDeathLoc = p.getLastDeathLocation().orElseThrow().pos();
                objectMain.put("lastDeathLocation", String.format("(%s, %s, %s)", lastDeathLoc.getX(), lastDeathLoc.getY(), lastDeathLoc.getZ()));
            } catch (NoSuchElementException e) {
                objectMain.put("lastDeathLocation", "(?, ?, ?)");
            }
            var objectPersonal = new JSONObject();
            /* objectPersonal construction */
            objectPersonal.put("wonGame", p.wonGame);
            objectPersonal.put("latency", p.latency);
            objectPersonal.put("experienceLevel", p.experienceLevel);
            objectPersonal.put("maxHealth", p.getMaxHealth());
            var objectStats = new JSONObject();
            /* objectStats construction */
            var stats = p.getStats();
            var distances = new JSONObject();
            var common = new JSONObject();
            common.put("playTime", stats.getValue(Stats.CUSTOM, Stats.PLAY_TIME));
            common.put("deathCount", stats.getValue(Stats.CUSTOM, Stats.DEATHS));
            common.put("damageTaken", stats.getValue(Stats.CUSTOM, Stats.DAMAGE_TAKEN));
            common.put("damageDealtResisted", stats.getValue(Stats.CUSTOM, Stats.DAMAGE_DEALT_RESISTED));
            common.put("fishCaught", stats.getValue(Stats.CUSTOM, Stats.FISH_CAUGHT));
            common.put("bellRing", stats.getValue(Stats.CUSTOM, Stats.BELL_RING));
            common.put("leaveGame", stats.getValue(Stats.CUSTOM, Stats.LEAVE_GAME));
            common.put("playerKills", stats.getValue(Stats.CUSTOM, Stats.PLAYER_KILLS));
            common.put("sleepInBed", stats.getValue(Stats.CUSTOM, Stats.SLEEP_IN_BED));
            common.put("mobKills", stats.getValue(Stats.CUSTOM, Stats.MOB_KILLS));
            common.put("enchantItem", stats.getValue(Stats.CUSTOM, Stats.ENCHANT_ITEM));
            common.put("mineStone", stats.getValue(Stats.BLOCK_MINED, Blocks.STONE));
            common.put("mineBedrock", stats.getValue(Stats.BLOCK_MINED, Blocks.BEDROCK));
            distances.put("walkOneCM", stats.getValue(Stats.CUSTOM, Stats.WALK_ONE_CM));
            distances.put("boatOneCM", stats.getValue(Stats.CUSTOM, Stats.BOAT_ONE_CM));
            distances.put("avaiteOneCM", stats.getValue(Stats.CUSTOM, Stats.AVIATE_ONE_CM));
            distances.put("fallOneCM", stats.getValue(Stats.CUSTOM, Stats.FALL_ONE_CM));
            distances.put("flyOneCM", stats.getValue(Stats.CUSTOM, Stats.FLY_ONE_CM));
            objectStats.put("distances", distances);
            objectStats.put("common", common);
            /* finalize */
            objectMain.put("personal", objectPersonal);
            objectMain.put("stats", objectStats);
            return objectMain;
        }
    }

    public static JSONObject getMods(Request q, Response a) {
        var modIds = ModList.get().getMods().stream().map(IModInfo::getDisplayName).toList();
        return buildResponse(State.OK, "", new JSONObject(modIds));
    }

    public static JSONObject getUptime(Request q, Response a) {
        return buildResponse(State.OK, "", (int) Utils.device.getServerUptime());
    }

    public static JSONObject getOnlinePlayerCount(Request q, Response a) {
        if (!serverReady) {
            return buildResponse(State.NG, "Server is not ready.");
        }
        return buildResponse(State.OK, "", util.getOnlinePlayerList().size());
    }

    public static JSONObject getTotalPlayerCount(Request q, Response a) {
        if (!serverReady) {
            return buildResponse(State.NG, "Server is not ready.");
        }
        return buildResponse(State.OK, "", util.countOfflinePlayer());
    }

    public static JSONObject getVersion(Request q, Response a) {
        var mcAndForgeVersion = FMLLoader.versionInfo().mcAndForgeVersion();
        return buildResponse(State.OK, "", mcAndForgeVersion);
    }

    public static JSONObject getOnlinePlayerList(Request q, Response a) {
        var list = util.getOnlinePlayerList();
        var result = new JSONObject();
        for (var p : list) {
            result.put(p.getGameProfile().getName(), util.getOnlinePlayerDetails(p));
        }
        return buildResponse(State.OK, "", result);
    }

    public static JSONObject addWhitelist(Request q, Response a) {
        var username = q.params().get("username");
        if (util.setWhitelistFor(username)) {
            return buildResponse(State.OK);
        } else {
            return buildResponse(State.NG);
        }
    }

    public static JSONObject dispatchCommand(Request q, Response a) {
        var command = q.params().get("command");
        return Utils.server.performCommand(command) ? buildResponse(State.OK) : buildResponse(State.NG);
    }
}
