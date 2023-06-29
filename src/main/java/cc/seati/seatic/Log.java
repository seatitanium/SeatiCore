package cc.seati.seatic;

import cc.seati.seatic.Enums.LogState;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class Log {
    public static final String logDir = "./seatic/logs/";
    public static String dateStr;
    public static File currentLogFile;

    public Log() {
        this.updateDate();
        initFiles();
    }

    private void initFiles() {
        var filename = String.format("logs-%s.log", dateStr);
        try {
            if (!Utils.files.mkdir(logDir)) {
                Utils.log.error("Error in Log::initFiles - Cannot create log directories.");
            }
            if (!Utils.files.touch(logDir + filename)) {
                Utils.log.error("Error in Log::initFiles - Cannot create latest log file.");
            }
        } catch (IOException e) {
            Utils.log.error("IOException in Log::initFiles - Details: " + e.getMessage());
            e.printStackTrace();
        }
        // be sure to modify the cursor variable.
        currentLogFile = new File(logDir + filename);
    }

    private void updateDate() {
        dateStr = Utils.format.getFormattedDate();
    }

    public int getLines() {
        try {
            var reader = new BufferedReader(new FileReader(currentLogFile));
            int totalLines = 0;
            while (reader.readLine() != null) totalLines++;
            reader.close();
            return totalLines;
        } catch (FileNotFoundException e) {
            Utils.log.error("Critical: Cannot reinitialize files after some log file was deleted. Message: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Utils.log.error("IOException in Log::checkLineLimit - Cannot read lines.");
            e.printStackTrace();
        }
        return 0;
    }

    public void write(LogState state, String str) {
        this.updateDate();
        if (!currentLogFile.exists()) {
            this.initFiles();
        }
        var lines = this.getLines();
        if (lines > 1000) {
            this.initFiles();
        }
        try {
            Files.write(currentLogFile.toPath(), List.of(String.format("[%s][%s] %s", dateStr, state.toString(), str)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void info(String str) {
        this.write(LogState.INFO, str);
    }

    public void error(String str) {
        this.write(LogState.ERROR, str);
    }

    public void warn(String str) {
        this.write(LogState.WARN, str);
    }
}
