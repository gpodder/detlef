package net.x4a42.volksempfaenger.feedparser;

import java.util.ArrayList;
import java.util.List;

public class Feed {
	public long localId;
	public boolean firstSync; // TODO put this somewhere else
	public String url, title, website, description, image;
	public List<FeedItem> items = new ArrayList<FeedItem>();
}
