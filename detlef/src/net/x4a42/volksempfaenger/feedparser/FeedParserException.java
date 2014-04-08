package net.x4a42.volksempfaenger.feedparser;

public class FeedParserException extends Exception {

	private static final long serialVersionUID = 1L;

	public FeedParserException() {
	}

	public FeedParserException(String detailMessage) {
		super(detailMessage);
	}

	public FeedParserException(Throwable throwable) {
		super(throwable);
	}

	public FeedParserException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

}
