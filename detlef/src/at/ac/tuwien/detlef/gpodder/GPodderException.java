package at.ac.tuwien.detlef.gpodder;

import java.io.Serializable;

/**
 * An Exception thrown (or sent) when something in the GPodder backend goes wrong.
 */
public class GPodderException extends Exception implements Serializable {

    private static final long serialVersionUID = 1L;

    public GPodderException(String msg) {
        super(msg);
    }
}
