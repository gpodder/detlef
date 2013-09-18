package at.ac.tuwien.detlef.gpodder.events;

public class RegisterDeviceResultEvent {
    public int code;
    public String id;
    public RegisterDeviceResultEvent(int code, String id) {
        this.code = code;
        this.id = id;
    }

}
