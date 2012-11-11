package at.ac.tuwien.detlef.gpodder;

import java.io.Serializable;

/**
 * An Exception thrown (or sent) when something in the GPodder backend goes wrong.
 */
public class GPodderException extends Exception implements Serializable {
    public GPodderException(String msg) {
        super(msg);
    }
}
