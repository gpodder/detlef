package at.ac.tuwien.detlef.gpodder.events;

public class ConnectionErrorEvent {
    public int code;
    public ConnectionErrorEvent(int code) {
        this.code = code;
    }
}
