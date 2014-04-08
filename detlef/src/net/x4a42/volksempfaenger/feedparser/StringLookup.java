package net.x4a42.volksempfaenger.feedparser;

import net.x4a42.volksempfaenger.feedparser.Enums.AtomRel;
import net.x4a42.volksempfaenger.feedparser.Enums.Mime;
import net.x4a42.volksempfaenger.feedparser.Enums.Namespace;
import net.x4a42.volksempfaenger.feedparser.Enums.Tag;
import net.x4a42.volksempfaenger.feedparser.Enums.GpodderKey;

public class StringLookup {
	public static Namespace lookupNamespace(String input) {
		try {
			final int length = input.length();
			if (length == 0) {
				return Namespace.NONE;
			}
			if (input.startsWith("http://", 0)) {
				switch (input.charAt(7)) {
				case 'b':
					if (input.startsWith("ackend.userland.com/RSS2", 8)) {
						if (length == 32) {
							return Namespace.RSS;
						}
						return Namespace.UNKNOWN;
					} else {
						return Namespace.UNKNOWN;
					}
				case 'p':
					if (input.startsWith("url.org/rss/1.0/modules/content/", 8)) {
						if (length == 40) {
							return Namespace.RSS_CONTENT;
						}
						return Namespace.UNKNOWN;
					} else {
						return Namespace.UNKNOWN;
					}
				case 'w':
					if (input.startsWith("ww.", 8)) {
						switch (input.charAt(11)) {
						case 'i':
							if (input.startsWith(
									"tunes.com/dtds/podcast-1.0.dtd", 12)) {
								if (length == 42) {
									return Namespace.ITUNES;
								}
								return Namespace.UNKNOWN;
							} else {
								return Namespace.UNKNOWN;
							}
						case 'w':
							if (input.startsWith("3.org/", 12)) {
								switch (input.charAt(18)) {
								case '2':
									if (input.startsWith("005/Atom", 19)) {
										if (length == 27) {
											return Namespace.ATOM;
										}
										return Namespace.UNKNOWN;
									} else {
										return Namespace.UNKNOWN;
									}
								case '1':
									if (input.startsWith("999/xhtml", 19)) {
										if (length == 28) {
											return Namespace.XHTML;
										}
										return Namespace.UNKNOWN;
									} else {
										return Namespace.UNKNOWN;
									}
								default:
									return Namespace.UNKNOWN;
								}
							} else {
								return Namespace.UNKNOWN;
							}
						default:
							return Namespace.UNKNOWN;
						}
					} else {
						return Namespace.UNKNOWN;
					}
				default:
					return Namespace.UNKNOWN;
				}
			} else {
				return Namespace.UNKNOWN;
			}
		} catch (IndexOutOfBoundsException e) {
			return Namespace.UNKNOWN;
		}
	}

	public static Tag lookupAtomTag(String input) {
		try {
			final int length = input.length();
			switch (input.charAt(0)) {
			case 'c':
				if (input.startsWith("ontent", 1)) {
					if (length == 7) {
						return Tag.ATOM_CONTENT;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'f':
				if (input.startsWith("eed", 1)) {
					if (length == 4) {
						return Tag.ATOM_FEED;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'e':
				if (input.startsWith("ntry", 1)) {
					if (length == 5) {
						return Tag.ATOM_ENTRY;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'i':
				switch (input.charAt(1)) {
				case 'c':
					if (input.startsWith("on", 2)) {
						if (length == 4) {
							return Tag.ATOM_ICON;
						}
						return Tag.UNKNOWN;
					} else {
						return Tag.UNKNOWN;
					}
				case 'd':
					if (length == 2) {
						return Tag.ATOM_ID;
					}
					return Tag.UNKNOWN;
				default:
					return Tag.UNKNOWN;
				}
			case 'l':
				if (input.startsWith("ink", 1)) {
					if (length == 4) {
						return Tag.ATOM_LINK;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 's':
				if (input.startsWith("ubtitle", 1)) {
					if (length == 8) {
						return Tag.ATOM_SUBTITLE;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'p':
				if (input.startsWith("ublished", 1)) {
					if (length == 9) {
						return Tag.ATOM_PUBLISHED;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 't':
				if (input.startsWith("itle", 1)) {
					if (length == 5) {
						return Tag.ATOM_TITLE;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'u':
				if (input.startsWith("pdated", 1)) {
					if (length == 7) {
						return Tag.ATOM_UPDATED;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			default:
				return Tag.UNKNOWN;
			}
		} catch (IndexOutOfBoundsException e) {
			return Tag.UNKNOWN;
		}
	}

	public static Tag lookupRssTag(String input) {
		try {
			final int length = input.length();
			switch (input.charAt(0)) {
			case 'c':
				if (input.startsWith("hannel", 1)) {
					if (length == 7) {
						return Tag.RSS_CHANNEL;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'g':
				if (input.startsWith("uid", 1)) {
					if (length == 4) {
						return Tag.RSS_GUID;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'd':
				if (input.startsWith("escription", 1)) {
					if (length == 11) {
						return Tag.RSS_DESCRIPTION;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'e':
				if (input.startsWith("nclosure", 1)) {
					if (length == 9) {
						return Tag.RSS_ENCLOSURE;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'i':
				switch (input.charAt(1)) {
				case 't':
					if (input.startsWith("em", 2)) {
						if (length == 4) {
							return Tag.RSS_ITEM;
						}
						return Tag.UNKNOWN;
					} else {
						return Tag.UNKNOWN;
					}
				case 'm':
					if (input.startsWith("age", 2)) {
						if (length == 5) {
							return Tag.RSS_IMAGE;
						}
						return Tag.UNKNOWN;
					} else {
						return Tag.UNKNOWN;
					}
				default:
					return Tag.UNKNOWN;
				}
			case 'l':
				if (input.startsWith("ink", 1)) {
					if (length == 4) {
						return Tag.RSS_LINK;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'r':
				if (input.startsWith("ss", 1)) {
					if (length == 3) {
						return Tag.RSS_TOPLEVEL;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'p':
				if (input.startsWith("ubDate", 1)) {
					if (length == 7) {
						return Tag.RSS_PUB_DATE;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 't':
				if (input.startsWith("itle", 1)) {
					if (length == 5) {
						return Tag.RSS_TITLE;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'u':
				if (input.startsWith("rl", 1)) {
					if (length == 3) {
						return Tag.RSS_URL;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			default:
				return Tag.UNKNOWN;
			}
		} catch (IndexOutOfBoundsException e) {
			return Tag.UNKNOWN;
		}
	}

	public static Tag lookupITunesTag(String input) {
		try {
			final int length = input.length();
			switch (input.charAt(0)) {
			case 's':
				if (input.startsWith("ummary", 1)) {
					if (length == 7) {
						return Tag.ITUNES_SUMMARY;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			case 'i':
				if (input.startsWith("mage", 1)) {
					if (length == 5) {
						return Tag.ITUNES_IMAGE;
					}
					return Tag.UNKNOWN;
				} else {
					return Tag.UNKNOWN;
				}
			default:
				return Tag.UNKNOWN;
			}
		} catch (IndexOutOfBoundsException e) {
			return Tag.UNKNOWN;
		}
	}

	public static AtomRel lookupAtomRel(String input) {
		try {
			final int length = input.length();
			switch (input.charAt(0)) {
			case 's':
				if (input.startsWith("elf", 1)) {
					if (length == 4) {
						return AtomRel.SELF;
					}
					return AtomRel.UNKNOWN;
				} else {
					return AtomRel.UNKNOWN;
				}
			case 'p':
				if (input.startsWith("ayment", 1)) {
					if (length == 7) {
						return AtomRel.PAYMENT;
					}
					return AtomRel.UNKNOWN;
				} else {
					return AtomRel.UNKNOWN;
				}
			case 'a':
				if (input.startsWith("lternate", 1)) {
					if (length == 9) {
						return AtomRel.ALTERNATE;
					}
					return AtomRel.UNKNOWN;
				} else {
					return AtomRel.UNKNOWN;
				}
			case 'e':
				if (input.startsWith("nclosure", 1)) {
					if (length == 9) {
						return AtomRel.ENCLOSURE;
					}
					return AtomRel.UNKNOWN;
				} else {
					return AtomRel.UNKNOWN;
				}
			default:
				return AtomRel.UNKNOWN;
			}
		} catch (IndexOutOfBoundsException e) {
			return AtomRel.UNKNOWN;
		}
	}

	public static Mime lookupMime(String input) {
		try {
			final int length = input.length();
			if (input.startsWith("text/", 0)) {
				switch (input.charAt(5)) {
				case 'x':
					if (input.startsWith("html", 6)) {
						if (length == 10) {
							return Mime.XHTML;
						}
						return Mime.UNKNOWN;
					} else {
						return Mime.UNKNOWN;
					}
				case 'h':
					if (input.startsWith("tml", 6)) {
						if (length == 9) {
							return Mime.HTML;
						}
						return Mime.UNKNOWN;
					} else {
						return Mime.UNKNOWN;
					}
				default:
					return Mime.UNKNOWN;
				}
			} else {
				return Mime.UNKNOWN;
			}
		} catch (IndexOutOfBoundsException e) {
			return Mime.UNKNOWN;
		}
	}

	public static GpodderKey lookupGpodderKey(String input) {
		try {
			final int length = input.length();
			switch (input.charAt(0)) {
			case 'd':
				if (input.startsWith("escription", 1)) {
					if (length == 11) {
						return GpodderKey.DESCRIPTION;
					}
					return GpodderKey.UNKNOWN;
				} else {
					return GpodderKey.UNKNOWN;
				}
			case 's':
				if (input.startsWith("caled_logo_url", 1)) {
					if (length == 15) {
						return GpodderKey.SCALED_LOGO_URL;
					}
					return GpodderKey.UNKNOWN;
				} else {
					return GpodderKey.UNKNOWN;
				}
			case 'w':
				if (input.startsWith("ebsite", 1)) {
					if (length == 7) {
						return GpodderKey.WEBSITE;
					}
					return GpodderKey.UNKNOWN;
				} else {
					return GpodderKey.UNKNOWN;
				}
			case 't':
				if (input.startsWith("itle", 1)) {
					if (length == 5) {
						return GpodderKey.TITLE;
					}
					return GpodderKey.UNKNOWN;
				} else {
					return GpodderKey.UNKNOWN;
				}
			case 'u':
				if (input.startsWith("rl", 1)) {
					if (length == 3) {
						return GpodderKey.URL;
					}
					return GpodderKey.UNKNOWN;
				} else {
					return GpodderKey.UNKNOWN;
				}
			default:
				return GpodderKey.UNKNOWN;
			}
		} catch (IndexOutOfBoundsException e) {
			return GpodderKey.UNKNOWN;
		}
	}
}
