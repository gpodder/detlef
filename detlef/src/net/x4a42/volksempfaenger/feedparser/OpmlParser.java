package net.x4a42.volksempfaenger.feedparser;

import java.io.IOException;
import java.io.Reader;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OpmlParser {

	public static SubscriptionTree parse(Reader reader) {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser;
		OpmlHandler handler = new OpmlHandler();

		try {
			parser = factory.newSAXParser();
			parser.parse(new InputSource(reader), handler);
			return removeEmptyFolders(handler.getTree());
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static SubscriptionTree removeEmptyFolders(SubscriptionTree tree) {
		return tree; // TODO
	}

	private static class OpmlHandler extends DefaultHandler {
		private SubscriptionTree tree = new SubscriptionTree("root");
		private Stack<SubscriptionTree> path = new Stack<SubscriptionTree>();
		private Stack<Integer> depthStack = new Stack<Integer>();
		private Integer depth = 0;

		public OpmlHandler() {
			super();
			path.push(tree);
		}

		private final static String OPML_OUTLINE = "outline";
		private final static String OPML_XML_URL = "xmlUrl";
		private final static String OPML_TITLE = "title";
		private static final String OPML_TEXT = "text";

		public SubscriptionTree getTree() {
			return tree;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) {
			if (!uri.isEmpty()) {
				// opml does not have a namespace
				// ignore all elements which have one
				return;
			}

			if (localName.equals(OPML_OUTLINE)) {
				final String url = atts.getValue(OPML_XML_URL);
				String title = atts.getValue(OPML_TITLE);
				final String text = atts.getValue(OPML_TEXT);
				SubscriptionTree newChild;
				if (url != null) {
					if (title == null) {
						if (text != null) {
							title = text;
						} else {
							title = url;
						}
					}
					newChild = new SubscriptionTree(title, url);
					path.peek().addChild(newChild);
				} else if (title != null) {
					newChild = new SubscriptionTree(title);
					path.peek().addChild(newChild);
					path.push(newChild);
					depthStack.push(depth);
				}
			}
			depth++;
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			depth--;
			if (!depthStack.isEmpty() && depthStack.peek().equals(depth)) {
				depthStack.pop();
				path.pop();
			}
		}
	}

}
