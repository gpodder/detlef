package net.x4a42.volksempfaenger.feedparser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedItem {
	public String title, url, description, itemId, flattrUrl;
	public Feed feed;
	public Date date;
	public List<Enclosure> enclosures = new ArrayList<Enclosure>();
}
