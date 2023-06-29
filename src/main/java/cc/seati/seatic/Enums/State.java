package cc.seati.seatic.Enums;

public enum State {
    OK("ok"),
    NG("ng");

    private final String text;

    State(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
