package net.x4a42.volksempfaenger.feedparser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class SubscriptionTree implements Iterable<SubscriptionTree> {
	private ArrayList<SubscriptionTree> children;
	public String url;
	public String title;
	public int depth;
	public int id;

	// Constructor for root element
	public SubscriptionTree() {
		super();
		children = new ArrayList<SubscriptionTree>();
		depth = 0;
	}

	// Constructor for feed element
	public SubscriptionTree(String title, String url) {
		this.title = title;
		this.url = url;
	}

	// Constructor for folder element
	public SubscriptionTree(String title) {
		this.title = title;
		children = new ArrayList<SubscriptionTree>();
	}

	public boolean isFolder() {
		if (children == null) {
			return false;
		} else {
			return true;
		}
	}

	public SubscriptionTree addChild(SubscriptionTree child) {
		if (!isFolder()) {
			return null;
		}
		children.add(child);
		child.depth = depth + 1;
		return child;
	}

	@Override
	public Iterator<SubscriptionTree> iterator() {
		return new Iterator<SubscriptionTree>() {
			Queue<SubscriptionTree> queue = new LinkedList<SubscriptionTree>();

			{
				for (SubscriptionTree child : children) {
					queue.add(child);
					if (child.isFolder()) {
						for (SubscriptionTree subchild : child) {
							queue.add(subchild);
						}
					}
				}
			}

			@Override
			public boolean hasNext() {
				return !queue.isEmpty();
			}

			@Override
			public SubscriptionTree next() {
				return queue.remove();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
