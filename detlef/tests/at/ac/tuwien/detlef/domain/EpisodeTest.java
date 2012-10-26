package at.ac.tuwien.detlef.domain;

import org.junit.Test;

import junit.framework.TestCase;

public class EpisodeTest extends TestCase {
	
	/**
	 * Despite not being very useful, this is the first test of our project.
	 * So be nice to it :)
	 */
	@Test
	public void testNameSetterAndgetter() {
		String name = "Episode 1";
		Episode episode = new Episode();
		episode.setName(name);
		assertEquals(name, episode.getName());
	}
	
}
