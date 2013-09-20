package net.x4a42.volksempfaenger.feedparser;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Locale;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.x4a42.volksempfaenger.Utils;
import net.x4a42.volksempfaenger.feedparser.Enums.AtomRel;
import net.x4a42.volksempfaenger.feedparser.Enums.Mime;
import net.x4a42.volksempfaenger.feedparser.Enums.Namespace;
import net.x4a42.volksempfaenger.feedparser.Enums.Tag;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FeedParser {

	public static void parseEvented(Reader reader, FeedParserListener listener)
			throws FeedParserException, IOException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser;
			FeedHandler handler = new FeedHandler(listener);
			parser = factory.newSAXParser();
			parser.parse(new InputSource(reader), handler);
			if (!handler.isFeed) {
				throw new NotAFeedException();
			}
		} catch (ParserConfigurationException e) {
			throw new FeedParserException(e);
		} catch (SAXException e) {
			throw new FeedParserException(e);
		} catch (NullPointerException e) {
			throw new FeedParserException("NullPointerException inside Parser",
					e);
		}
	}

	public static Feed parse(Reader reader) throws FeedParserException,
			IOException {
		LegacyFeedParserListener listener = new LegacyFeedParserListener();
		FeedParser.parseEvented(reader, listener);
		return listener.feed;
	}

	private static class LegacyFeedParserListener implements FeedParserListener {
		public Feed feed;

		private final ArrayList<FeedItem> feedItems = new ArrayList<FeedItem>();
		private final ArrayList<Enclosure> enclosures = new ArrayList<Enclosure>();

		@Override
		public void onFeedItem(FeedItem feedItem) {
			feedItems.add(feedItem);
			feedItem.enclosures.addAll(enclosures);
			enclosures.clear();
		}

		@Override
		public void onFeed(Feed feed) {
			this.feed = feed;
			feed.items.addAll(feedItems);
			feedItems.clear();
		}

		@Override
		public void onEnclosure(Enclosure enclosure) {
			enclosures.add(enclosure);
		}

	}

	private static class FeedHandler extends DefaultHandler {
		public boolean isFeed = false;

		private final Feed feed = new Feed();
		private FeedItem feedItem = new FeedItem();
		private Enclosure enclosure = new Enclosure();

		private final Stack<Tag> parents = new Stack<Tag>();
		private boolean skipMode = false;
		private boolean xhtmlMode = false;
		private boolean currentRssItemHasHtml = false;
		private boolean currentItemHasITunesSummaryAlternative = false;
		private boolean currentAtomItemHasPublished = false;
		private boolean hasITunesImage = false;
		private int skipDepth = 0;
		private final StringBuilder buffer = new StringBuilder();

		private static final String ATOM_ATTR_HREF = "href";
		private static final String ATOM_ATTR_REL = "rel";
		private static final String ATOM_ATTR_TYPE = "type";
		private static final String ATOM_ATTR_LENGTH = "length";
		private static final String ATOM_ATTR_TITLE = "title";

		private static final String RSS_ATTR_URL = "url";
		private static final String RSS_ATTR_TYPE = "type";
		private static final String RSS_ATTR_LENGTH = "length";

		private final FeedParserListener listener;

		public FeedHandler(FeedParserListener listener) {
			super();
			this.listener = listener;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if (skipMode) {
				skipDepth++;
			} else {
				Namespace ns = getNamespace(uri);
				Tag tag = getTag(ns, localName);

				if (!isFeed) {
					// is current element one of the toplevel elements
					if (((ns == Namespace.ATOM) && tag == Tag.ATOM_FEED)
							|| ((ns == Namespace.NONE) && tag == Tag.RSS_TOPLEVEL)
							|| ((ns == Namespace.RSS) && tag == Tag.RSS_TOPLEVEL)) {
						isFeed = true;
					}
				}

				if (ns == Namespace.ATOM) {
					onStartTagAtom(tag, atts);
				} else if (ns == Namespace.NONE || ns == Namespace.RSS
						|| ns == Namespace.RSS_CONTENT) {
					onStartTagRss(tag, atts);
				} else if (ns == Namespace.XHTML && xhtmlMode) {
					onStartTagXHtml(localName, atts);
				} else if (ns == Namespace.ITUNES) {
					onStartTagITunes(tag, atts);
				} else {
					skipMode = true;
					skipDepth = 0;
					return;
				}
				parents.push(tag);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (skipMode) {
				return;
			}
			if (!safePeek(Tag.UNKNOWN) || xhtmlMode) {
				if (safePeek(Tag.RSS_DESCRIPTION) && currentRssItemHasHtml) {
					// we already have an HTML version of this, so just ignore
					// the plaintext
					return;
				}
				buffer.append(ch, start, length);
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (skipMode) {
				if (skipDepth == 0) {
					skipMode = false;
				} else {
					skipDepth--;
				}
			} else {
				Namespace ns = getNamespace(uri);
				Tag tag;
				try {
					tag = parents.pop();
				} catch (EmptyStackException e) {
					return;
				}

				if (ns == Namespace.ATOM) {
					onEndTagAtom(tag);
				} else if (ns == Namespace.NONE || ns == Namespace.RSS
						|| ns == Namespace.RSS_CONTENT) {
					onEndTagRss(tag);
				} else if (ns == Namespace.XHTML && xhtmlMode) {
					onEndTagXHtml(localName);
				} else if (ns == Namespace.ITUNES) {
					onEndTagITunes(tag);
				}
				if (tag != Tag.UNKNOWN) {
					// clear buffer
					buffer.setLength(0);
				}
			}
		}

		private void onStartTagAtom(Tag tag, Attributes atts) {
			switch (tag) {
			case ATOM_ENTRY:
				feedItem = new FeedItem();
				feedItem.feed = feed;
				currentItemHasITunesSummaryAlternative = false;
				currentAtomItemHasPublished = false;
				break;
			case ATOM_CONTENT:
				if (atts.getValue(ATOM_ATTR_TYPE).equals("xhtml")) {
					xhtmlMode = true;
				}
			case ATOM_LINK:
				String relString = atts.getValue(ATOM_ATTR_REL);
				AtomRel rel = AtomRel.UNKNOWN;
				if (relString != null) {
					rel = getAtomRel(relString);
					relString = null;
				}
				switch (rel) {
				case ENCLOSURE:
					if (safePeek(Tag.ATOM_ENTRY)) {
						enclosure = new Enclosure();
						enclosure.feedItem = feedItem;
						enclosure.url = atts.getValue(ATOM_ATTR_HREF);
						enclosure.mime = atts.getValue(ATOM_ATTR_TYPE);
						enclosure.title = atts.getValue(ATOM_ATTR_TITLE);

						String length = atts.getValue(ATOM_ATTR_LENGTH);
						enclosure.size = safeParseLong(length);
						onEnclosure();
					}
					break;
				case ALTERNATE:
					String mimeString = atts.getValue(ATOM_ATTR_TYPE);
					Mime type = Mime.UNKNOWN;
					if (mimeString != null) {
						type = getMime(mimeString);
						mimeString = null;
					}
					if (safePeek(Tag.ATOM_ENTRY)) {
						if (type == Mime.UNKNOWN || type == Mime.HTML
								|| type == Mime.XHTML) {
							// actually there can be multiple
							// "alternate links"
							// this uses the LAST alternate link as the
							// URL for
							// the FeedItem
							feedItem.url = atts.getValue(ATOM_ATTR_HREF);
						}
					} else if (safePeek(Tag.ATOM_FEED)) {
						if (type == Mime.UNKNOWN || type == Mime.HTML
								|| type == Mime.XHTML) {
							// same issue as above with multiple
							// alternate links
							feed.website = atts.getValue(ATOM_ATTR_HREF);
						}
					}
					break;
				case SELF:
					if (safePeek(Tag.ATOM_FEED)) {
						feed.url = atts.getValue(ATOM_ATTR_HREF);
					}
					break;
				case PAYMENT:
					if (safePeek(Tag.ATOM_ENTRY) || safePeek(Tag.RSS_ITEM)) {
						String url = atts.getValue(ATOM_ATTR_HREF);
						try {
							if (new URL(url).getHost().equals("flattr.com")) {
								feedItem.flattrUrl = url;
							}
						} catch (MalformedURLException e) {
							// ignore if url is malformed
						}
					}
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}

		}

		private void onStartTagRss(Tag tag, Attributes atts) {
			switch (tag) {
			case RSS_ITEM:
				feedItem = new FeedItem();
				feedItem.feed = feed;
				currentRssItemHasHtml = false;
				currentItemHasITunesSummaryAlternative = false;
				break;
			case RSS_ENCLOSURE:
				if (safePeek(Tag.RSS_ITEM)) {
					enclosure = new Enclosure();
					enclosure.feedItem = feedItem;
					enclosure.url = atts.getValue(RSS_ATTR_URL);
					enclosure.mime = atts.getValue(RSS_ATTR_TYPE);

					String length = atts.getValue(RSS_ATTR_LENGTH);
					enclosure.size = safeParseLong(length);
					onEnclosure();
				}
				break;
			default:
				break;
			}
		}

		private void onStartTagXHtml(String name, Attributes atts) {
			buffer.append("<");
			buffer.append(name);
			for (int i = 0; i < atts.getLength(); i++) {
				buffer.append(" ");
				buffer.append(atts.getLocalName(i));
				buffer.append("=\"");
				// escape double quotes (hope this works)
				buffer.append(atts.getValue(i).replaceAll("\"", "\\\""));
				buffer.append("\"");
			}
			buffer.append(">");
		}

		private void onStartTagITunes(Tag tag, Attributes atts) {
			if (tag == Tag.ITUNES_IMAGE
					&& (safePeek(Tag.RSS_CHANNEL) || safePeek(Tag.ATOM_FEED))) {
				feed.image = atts.getValue("href");
				hasITunesImage = true;
			}
		}

		private void onEndTagAtom(Tag tag) {
			switch (tag) {
			case ATOM_TITLE:
				if (safePeek(Tag.ATOM_FEED)) {
					// feed title
					feed.title = Utils.trimmedString(buffer);
				} else if (safePeek(Tag.ATOM_ENTRY)) {
					// entry title
					feedItem.title = Utils.trimmedString(buffer);
				}
				break;
			case ATOM_CONTENT:
				if (xhtmlMode) {
					xhtmlMode = false;
				}
				if (safePeek(Tag.ATOM_ENTRY)) {
					feedItem.description = Utils.trimmedString(buffer);
					currentItemHasITunesSummaryAlternative = true;
				}
				break;
			case ATOM_PUBLISHED:
				if (safePeek(Tag.ATOM_ENTRY)) {
					try {
						feedItem.date = parseAtomDate(buffer.toString());
						currentAtomItemHasPublished = true;
					} catch (IndexOutOfBoundsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case ATOM_UPDATED:
				if (safePeek(Tag.ATOM_ENTRY) && !currentAtomItemHasPublished) {
					try {
						feedItem.date = parseAtomDate(buffer.toString());
					} catch (IndexOutOfBoundsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case ATOM_SUBTITLE:
				feed.description = Utils.trimmedString(buffer);
				break;
			case ATOM_ENTRY:
				onFeedItem();
				break;
			case ATOM_ID:
				if (safePeek(Tag.ATOM_ENTRY)) {
					feedItem.itemId = Utils.trimmedString(buffer);
				}
				break;
			case ATOM_ICON:
				if (safePeek(Tag.ATOM_FEED) && !hasITunesImage) {
					feed.image = Utils.trimmedString(buffer);
				}
				break;
			case ATOM_FEED:
				onFeed();
				break;
			default:
				break;
			}
		}

		private void onEndTagRss(Tag tag) {
			switch (tag) {
			case RSS_TITLE:
				if (safePeek(Tag.RSS_CHANNEL)) {
					feed.title = Utils.trimmedString(buffer);
				} else if (safePeek(Tag.RSS_ITEM)) {
					feedItem.title = Utils.trimmedString(buffer);
				}
				break;
			case RSS_PUB_DATE:
				if (safePeek(Tag.RSS_ITEM)) {
					try {
						feedItem.date = parseRssDate(buffer.toString());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case RSS_LINK:
				if (safePeek(Tag.RSS_ITEM)) {
					feedItem.url = Utils.trimmedString(buffer);
				} else if (safePeek(Tag.RSS_CHANNEL)) {
					feed.website = Utils.trimmedString(buffer);
				}
				break;
			case RSS_DESCRIPTION:
				if (!currentRssItemHasHtml) {
					if (safePeek(Tag.RSS_ITEM)) {
						feedItem.description = Utils.trimmedString(buffer);
						currentItemHasITunesSummaryAlternative = true;
					} else if (safePeek(Tag.RSS_CHANNEL)) {
						feed.description = Utils.trimmedString(buffer);
					}
				}
				break;
			case RSS_CONTENT_ENCODED:
				currentRssItemHasHtml = true;
				if (safePeek(Tag.RSS_ITEM)) {
					feedItem.description = Utils.trimmedString(buffer);
					currentItemHasITunesSummaryAlternative = true;
				} else if (safePeek(Tag.RSS_CHANNEL)) {
					feed.description = Utils.trimmedString(buffer);
				}
				break;
			case RSS_ITEM:
				onFeedItem();
				currentRssItemHasHtml = false;
				break;
			case RSS_GUID:
				if (safePeek(Tag.RSS_ITEM)) {
					feedItem.itemId = Utils.trimmedString(buffer);
				}
				break;
			case RSS_URL:
				if (safePeek(Tag.RSS_IMAGE) && !hasITunesImage) {
					Tag copy;
					try {
						copy = parents.pop();
					} catch (EmptyStackException e) {
						return;
					}
					if (safePeek(Tag.RSS_CHANNEL)) {
						feed.image = Utils.trimmedString(buffer);
					}
					parents.push(copy);
				}
			case RSS_CHANNEL:
				onFeed();
				break;
			default:
				break;
			}

		}

		private void onEndTagXHtml(String name) {
			buffer.append("</");
			buffer.append(name);
			buffer.append(">");
		}

		private void onEndTagITunes(Tag tag) {
			if (tag == Tag.ITUNES_SUMMARY
					&& (safePeek(Tag.ATOM_ENTRY) || safePeek(Tag.RSS_ITEM))
					&& !currentItemHasITunesSummaryAlternative) {
				feedItem.description = Utils.trimmedString(buffer);
			}
		}

		private Date parseAtomDate(String datestring)
				throws java.text.ParseException, IndexOutOfBoundsException {
			datestring = datestring.trim().toUpperCase(Locale.getDefault());
			// dirty version - write a new one TODO
			// Modified version of http://cokere.com/RFC3339Date.txt
			/*
			 * I was working on an Atom (http://www.w3.org/2005/Atom) parser and
			 * discovered that I could not parse dates in the format defined by
			 * RFC 3339 using the SimpleDateFormat class. The reason was the ':'
			 * in the time zone. This code strips out the colon if it's there
			 * and tries four different formats on the resulting string
			 * depending on if it has a time zone, or if it has a fractional
			 * second part. There is a probably a better way to do this, and a
			 * more proper way. But this is a really small addition to a
			 * codebase (You don't need a jar, just throw this function in some
			 * static Utility class if you have one).
			 * 
			 * Feel free to use this in your code, but I'd appreciate it if you
			 * keep this note in the code if you distribute it. Thanks!
			 * 
			 * For people who might be googling: The date format parsed by this
			 * goes by: atomDateConstruct, xsd:dateTime, RFC3339 and is
			 * compatable with: ISO.8601.1988, W3C.NOTE-datetime-19980827 and
			 * W3C.REC-xmlschema-2-20041028 (that I know of)
			 * 
			 * 
			 * Copyright 2007, Chad Okere (ceothrow1 at gmail dotcom) OMG NO
			 * WARRENTY EXPRESSED OR IMPLIED!!!1
			 */

			// if there is no time zone, we don't need to do any special
			// parsing.
			if (datestring.charAt(datestring.length() - 1) == 'Z') {
				try {
					// spec for RFC3339
					return formats[4].parse(datestring);
				} catch (java.text.ParseException pe) {
					// try again with optional decimals
					// spec for RFC3339 (with fractional seconds)
					return formats[5].parse(datestring);
				}
			}

			// step one, split off the timezone.
			String firstpart = datestring.substring(0,
					datestring.lastIndexOf('-'));
			String secondpart = datestring.substring(datestring
					.lastIndexOf('-'));

			// step two, remove the colon from the timezone offset
			secondpart = secondpart.substring(0, secondpart.indexOf(':'))
					+ secondpart.substring(secondpart.indexOf(':') + 1);
			datestring = firstpart + secondpart;
			try {
				return formats[6].parse(datestring);// spec for RFC3339
			} catch (java.text.ParseException pe) {
				// try again with optional decimals
				// spec for RFC3339 (with fractional seconds)
				return formats[7].parse(datestring);
			}
		}

		private static final SimpleDateFormat formats[] = new SimpleDateFormat[] {
				new SimpleDateFormat("d MMM yy HH:mm z", Locale.US),
				new SimpleDateFormat("d MMM yy HH:mm:ss z", Locale.US),
				new SimpleDateFormat("d MMM yyyy HH:mm z", Locale.US),
				new SimpleDateFormat("d MMM yyyy HH:mm:ss z", Locale.US),

				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
						Locale.US),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ", Locale.US) };

		static {
			formats[5].setLenient(true);
			formats[7].setLenient(true);
		}

		private Date parseRssDate(String datestring) throws ParseException {
			// dirty version - write a new one TODO
			SimpleDateFormat format;

			int commaPos = datestring.indexOf(',');
			if (commaPos > -1) {
				// remove weekday if present
				datestring = datestring.substring(commaPos + 1);
			}
			datestring = datestring.trim();

			if (datestring.length() > 8 && datestring.charAt(8) == ' ') {
				if (datestring.length() > 16 && datestring.charAt(14) == ' ') {
					format = formats[0];
				} else {
					format = formats[1];
				}
			} else {
				if (datestring.length() > 16 && datestring.charAt(16) == ' ') {
					format = formats[2];
				} else {
					format = formats[3];
				}
			}

			return format.parse(datestring);
		}

		private static Namespace getNamespace(String nsString) {
			return StringLookup.lookupNamespace(nsString);
		}

		private static Tag getTag(Namespace ns, String tagString) {
			switch (ns) {
			case ATOM:
				return StringLookup.lookupAtomTag(tagString);
			case RSS:
			case NONE:
				return StringLookup.lookupRssTag(tagString);
			case RSS_CONTENT:
				if (tagString.equals("encoded")) {
					return Tag.RSS_CONTENT_ENCODED;
				} else {
					return Tag.UNKNOWN;
				}
			case ITUNES:
				return StringLookup.lookupITunesTag(tagString);
			default:
				return Tag.UNKNOWN;
			}
		}

		private static AtomRel getAtomRel(String relString) {
			return StringLookup.lookupAtomRel(relString);
		}

		private static Mime getMime(String mimeString) {
			return StringLookup.lookupMime(mimeString);
		}

		private void onEnclosure() {
			listener.onEnclosure(enclosure);
		}

		private void onFeedItem() {
			if (feedItem.itemId == null) {
				if (feedItem.url != null) {
					feedItem.itemId = feedItem.url;
				} else if (feedItem.title != null) {
					feedItem.itemId = feedItem.title;
				} else {
					return;
				}
			}
			if (feedItem.date == null) {
				return;
			}
			listener.onFeedItem(feedItem);
		}

		private void onFeed() {
			listener.onFeed(feed);
		}

		private long safeParseLong(String number) {
			if (number != null) {
				try {
					return Long.parseLong(number.trim());
				} catch (NumberFormatException e) {
					return 0;
				}
			}
			return 0;
		}

		private boolean safePeek(Tag tag) {
			try {
				Tag parent = parents.peek();
				if (tag == parent) {
					return true;
				} else {
					return false;
				}
			} catch (EmptyStackException e) {
				return false;
			}
		}
	}
}
