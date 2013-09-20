package net.x4a42.volksempfaenger.feedparser;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import net.x4a42.volksempfaenger.feedparser.Enums.GpodderKey;
import android.util.JsonReader;
import android.util.JsonToken;

public class GpodderJsonReader {
	public final static String KEY_TITLE = "title";
	public final static String KEY_URL = "url";
	public final static String KEY_DESCRIPTION = "description";
	public final static String KEY_WEBSITE = "website";
	public final static String KEY_SCALED_LOGO = "scaled_logo_url";

	private final JsonReader reader;
	private final GpodderJsonReaderListener listener;

	public GpodderJsonReader(Reader in, GpodderJsonReaderListener listener) {
		reader = new JsonReader(in);
		this.listener = listener;
	}

	public void read() throws IOException {
		try {
			readPodcastArray();
		} finally {
			reader.close();
		}
	}

	public void readPodcastArray() throws IOException {
		reader.beginArray();
		while (reader.hasNext()) {
			readPodcast();
		}
		reader.endArray();
	}

	public void readPodcast() throws IOException {
		HashMap<String, String> podcast = new HashMap<String, String>();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			GpodderKey key = StringLookup.lookupGpodderKey(name);
			switch (key) {
			case DESCRIPTION:
				if (reader.peek() == JsonToken.STRING) {
					podcast.put(KEY_DESCRIPTION, reader.nextString());
				} else {
					podcast.put(KEY_DESCRIPTION, "");
					reader.skipValue();
				}
				break;
			case SCALED_LOGO_URL:
				if (reader.peek() == JsonToken.STRING) {
					podcast.put(KEY_SCALED_LOGO, reader.nextString());
				} else {
					podcast.put(KEY_SCALED_LOGO, "");
					reader.skipValue();
				}
				break;
			case TITLE:
				if (reader.peek() == JsonToken.STRING) {
					podcast.put(KEY_TITLE, reader.nextString());
				} else {
					podcast.put(KEY_TITLE, "");
					reader.skipValue();
				}
				break;
			case URL:
				if (reader.peek() == JsonToken.STRING) {
					podcast.put(KEY_URL, reader.nextString());
				} else {
					podcast.put(KEY_URL, "");
					reader.skipValue();
				}
				break;
			case WEBSITE:
				if (reader.peek() == JsonToken.STRING) {
					podcast.put(KEY_WEBSITE, reader.nextString());
				} else {
					podcast.put(KEY_WEBSITE, "");
					reader.skipValue();
				}
				break;
			case UNKNOWN:
				reader.skipValue();
				break;
			}
		}
		reader.endObject();
		listener.onPodcast(podcast);
	}
}
