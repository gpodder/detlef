package net.x4a42.volksempfaenger.feedparser;

public class NotAFeedException extends FeedParserException {

	private static final long serialVersionUID = 1L;

	public NotAFeedException() {
	}

	public NotAFeedException(String detailMessage) {
		super(detailMessage);
	}

	public NotAFeedException(Throwable throwable) {
		super(throwable);
	}

	public NotAFeedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
