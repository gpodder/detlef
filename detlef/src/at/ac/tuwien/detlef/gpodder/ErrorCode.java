package at.ac.tuwien.detlef.gpodder;

/**
 * Contains the error codes for failures reported by the
 * {@link PodderService}.
 */
public class ErrorCode {
    public static final int SUCCESS = 0;

    /** Error code raised if the URL scheme is not allowed. */
    public static final int INVALID_URL_SCHEME = 1;

    /** Error code raised if the URL is formatted incorrectly. */
    public static final int MALFORMED_URL = 2;

    /** Error code raised if there has been a problem with input/output. */
    public static final int IO_PROBLEM = 3;

    /** Error code raised if sending the result failed. */
    public static final int SENDING_RESULT_FAILED = 4;

    /** Error code raised if sending the request failed. This code is not
     *  sent by the service, but may be sent by the plumbing layer (e.g.
     *  {@link GPodderSync}) if the message to the service cannot be sent. */
    public static final int SENDING_REQUEST_FAILED = 5;

    /** Error code raised if authentication fails. */
    public static final int AUTHENTICATION_FAILED = 6;

    /** Error code raised if a file was not found. */
    public static final int FILE_NOT_FOUND = 7;

    /** Error code raised if the error is unknown. */
    public static final int UNKNOWN_ERROR = 8;

    /** Error code raised if an HTTP response with an unexpected code has
     *  been received. */
    public static final int UNEXPECTED_HTTP_RESPONSE = 9;

    /** Error code raised if the device is currently offline. */
    public static final int OFFLINE = 10;

    public static final int GENERIC_FAILURE = 11;

    public static boolean failed(int code) {
        return (code != SUCCESS);
    }
}