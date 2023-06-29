package cc.seati.seatic;

import cc.seati.seatic.API.ServerApi;
import spark.Filter;
import spark.Spark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import static cc.seati.seatic.Utils.resp.ng;
import static spark.Spark.*;

public class Server {
    public static String indexPageStr;

    public Server(int p, String indexPage) {
        Utils.log.info("Starting initial web server - powered by Spark.");
        indexPageStr = indexPage;
        port(p);
        initExceptionHandler((e) -> Utils.log.error("HTTP 服务出现问题：" + e.getMessage()));
        after((Filter) (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST");
        });
    }


    public static void error() {
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

    public static void stop() {
        Spark.stop();
    }

    public static void map() {
        if (indexPageStr != null) {
            get("/", (req, res) -> {
                res.type("text/html");
                try {
                    return Files.readString(Path.of(indexPageStr));
                } catch (InvalidPathException | IOException e) {
                    Utils.log.warn("Index page is not found. Returning 404.");
                    halt(404);
                    e.printStackTrace();
                }
                return null;
            });
        }
        path("/api", () -> {
            before("/*", (q, a) -> {
                // pathInfo example return: /example/foo
                Utils.fileLogger.info(String.format("access %s", q.pathInfo()));
            });
            path("/server", () -> {
                get("/uptime", ServerApi::getUptime);
                get("/count-players", ServerApi::getPlayerCount);
                get("/mods", ServerApi::getMods);
                get("/version", ServerApi::getVersion);
            });
        });
    }
}
