package cc.seati.seatic.Enums;

public enum LogState {
    ERROR("ERROR"),
    WARN("WARN"),
    INFO("INFO");

    private final String txt;

    LogState(String txt) {
        this.txt = txt;
    }

    @Override
    public String toString() {
        return this.txt;
    }
}
