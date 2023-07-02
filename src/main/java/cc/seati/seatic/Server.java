package cc.seati.seatic;

import cc.seati.seatic.API.DeviceApi;
import cc.seati.seatic.API.ServerApi;
import spark.Filter;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

import static cc.seati.seatic.Utils.resp.*;
import static spark.Spark.*;

public class Server {
    public static String indexPageStr = "";

    public Server(int p) {
        Utils.log.info("Starting internal web server - powered by Spark.");
        port(p);
        webSocket("/console", ChatWS.class);
        initIndex();
        error();
        initExceptionHandler((e) -> Utils.log.error("HTTP 服务出现问题：" + e.getMessage()));
        after((Filter) (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST");
        });
        map();
    }

    public void initIndex() {
        var index = new File(Utils.files.cwd  + "/seatic/index.html");
        if (index.exists()) {
            try {
                indexPageStr = Files.readString(index.toPath());
            } catch (IOException | InvalidPathException e) {
                e.printStackTrace();
                Utils.log.error("Error in initIndex: cannot read index.html.");
            }
        }
    }

    public void error() {
        notFound((req, res) -> {
            res.type("application/json");
            return ng("http-404");
        });
        internalServerError((req, res) -> {
            res.type("application/json");
            return ng("http-500");
        });
        exception(Exception.class, (e, req, res) -> {
            Utils.log.error("An internal error occurred when processing the request. Detail: " + e.getMessage());
            e.printStackTrace();
        });
    }

    public void stop() {
        Spark.stop();
    }

    public void map() {
        if (indexPageStr != null) {
            get("/", (req, res) -> {
                res.type("text/html");
                return indexPageStr;
            });
        }
        path("/api", () -> {
            before("/*", (q, a) -> {
                a.type("application/json");
                // pathInfo example return: /example/foo
                Utils.fileLogger.info(String.format("access %s", q.pathInfo()));
            });
            path("/server", () -> {
                get("/uptime", ServerApi::getUptime);
                get("/count-online-players", ServerApi::getOnlinePlayerCount);
                get("/count-total-players", ServerApi::getTotalPlayerCount);
                get("/mods", ServerApi::getMods);
                get("/version", ServerApi::getVersion);
                get("/player-list", ServerApi::getOnlinePlayerList);
            });
            path("/device", () -> {
                get("/tps", DeviceApi::getTps);
                get("/ramload", DeviceApi::ramLoad);
                get("/cpuload", DeviceApi::cpuLoad);
            });
        });
    }
}
