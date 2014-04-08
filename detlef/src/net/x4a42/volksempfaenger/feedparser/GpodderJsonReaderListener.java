package net.x4a42.volksempfaenger.feedparser;

import java.util.HashMap;

public interface GpodderJsonReaderListener {
	void onPodcast(HashMap<String, String> podcast);
}
