package at.ac.tuwien.detlef.gpodder.events;

public class AuthCheckResultEvent {
    public int code;

    public AuthCheckResultEvent(int code) {
        this.code = code;
    }
}
