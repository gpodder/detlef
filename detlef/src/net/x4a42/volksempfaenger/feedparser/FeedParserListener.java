package net.x4a42.volksempfaenger.feedparser;

public interface FeedParserListener {
	/**
	 * Gets called at the end of the feed, when all metadata is available.
	 * 
	 * @param feed
	 *            Data of the feed. {@see Feed.items} may be null or empty.
	 */
	public void onFeed(Feed feed);

	/**
	 * Gets called at the end of a new feed item.
	 * 
	 * @param feedItem
	 *            Data of the feedItem. {@see FeedItem.feed} may not contain all
	 *            data at this point. {@see FeedItem.enclosures} may be null or
	 *            empty.
	 */
	public void onFeedItem(FeedItem feedItem);

	/**
	 * Gets called on a new enclosure and belongs to the next feedItem, which
	 * means that onFeedItem is called after this.
	 * 
	 * @param enclosure
	 *            Data of the enclosure. {@see feedItem} may not contain data at
	 *            this point.
	 */
	public void onEnclosure(Enclosure enclosure);
}
