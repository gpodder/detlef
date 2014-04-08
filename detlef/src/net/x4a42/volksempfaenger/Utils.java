package net.x4a42.volksempfaenger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.JsonReader;

public class Utils {
	public static String joinArray(Object[] objects, CharSequence sep) {
		if (objects == null) {
			return null;
		}

		StringBuilder buf = new StringBuilder();

		int i = 0;
		while (i < objects.length) {
			if (objects[i] != null) {
				buf.append(objects[i]);
			}
			if (++i < objects.length) {
				buf.append(sep);
			}
		}

		return buf.toString();
	}

	public static String joinArray(long[] longs, CharSequence sep) {
		if (longs == null) {
			return null;
		}

		String[] objects = new String[longs.length];

		for (int i = 0; i < longs.length; i++) {
			objects[i] = String.valueOf(longs[i]);
		}

		return joinArray(objects, sep);
	}

	public static long toUnixTimestamp(Date date) {
		return date != null ? date.getTime() / 1000L : 0;
	}

	public static String normalizeString(String string) {
		return string.replaceAll("\\s+", " ");
	}

	public static String normalizeFilename(String filename) {
		return filename.replaceAll("[^A-Za-z0-9-_\\.]+", "_");
	}

	public static File joinPath(File base, String... children) {
		for (String child : children) {
			base = new File(base, child);
		}
		return base;
	}

	public static File joinPath(String base, String... children) {
		return joinPath(new File(base), children);
	}

	public static File getPodcastLogoFile(Context context, long podcastId) {
		return Utils.joinPath(context.getExternalFilesDir(null), "logos",
				String.valueOf(podcastId));
	}

	public static boolean stringBoolean(String str) {
		return str.equals("true") || str.equals("yes");
	}

	public static String filenameFromUrl(String url) {
		int slashIndex = url.lastIndexOf("/");
		String filename = null;
		if (slashIndex != -1) {
			filename = url.substring(slashIndex + 1);
			if (filename.length() == 0) {
				filename = null;
			}
		}
		return filename;
	}

	public static void internStringArray(String[] strings) {
		for (int i = 0; i < strings.length; i++) {
			if (strings[i] != null) {
				strings[i] = strings[i].intern();
			}
		}
	}

	public static String formatTime(int milliseconds) {
		int seconds = milliseconds / 1000;
		int hours = seconds / 3600;
		int minutes = (seconds / 60) - (hours * 60);
		int seconds2 = seconds - (minutes * 60) - (hours * 3600);
		DecimalFormat format = new DecimalFormat("00");
		return format.format(hours) + ":" + format.format(minutes) + ":"
				+ format.format(seconds2);
	}

	public static String trimmedString(StringBuilder stringBuilder) {
		final int length = stringBuilder.length();
		int start, end;
		for (start = 0; start < length; start++) {
			if (!Character.isWhitespace(stringBuilder.charAt(start))) {
				break;
			}
		}
		for (end = length - 1; end >= start; end--) {
			if (!Character.isWhitespace(stringBuilder.charAt(end))) {
				break;
			}
		}
		return stringBuilder.substring(start, end + 1);
	}

	@TargetApi(16)
	@SuppressWarnings("deprecation")
	public static Notification notificationFromBuilder(Notification.Builder nb) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			return nb.getNotification();
		} else {
			return nb.build();
		}
	}

	// taken from http://stackoverflow.com/a/5599842/731539
	public static String readableFileSize(long size) {

		if (size <= 0) {
			return "0";
		}

		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

		return new DecimalFormat("#,##0.0").format(size
				/ Math.pow(1024, digitGroups))
				+ " " + units[digitGroups];

	}

	public static int dpToPx(Activity activity, int dp) {
		return (int) (dp * getDensity(activity) + 0.5);
	}

	public static int pxToDp(Activity activity, int px) {
		return (int) (px / getDensity(activity) + 0.5);
	}

	private static float getDensity(Activity activity) {
		final DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.density;
	}

	public static Bitmap getPodcastLogoBitmap(Context context, long podcastId) {
		return BitmapFactory.decodeFile(getPodcastLogoFile(context, podcastId).getAbsolutePath());
	}

	public static Bitmap getPodcastLogoBitmap(Context context, long podcastId, int width, int height) {
		return decodeSampledBitmapFromFile(getPodcastLogoFile(context, podcastId), width, height);
	}

	public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getAbsolutePath(), options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
	}

	private static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static int copy(Reader in, Writer out) throws IOException {
		char[] buffer = new char[4096];
		int written = 0, n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
			written += n;
		}
		return written;
	}

	public static int copyStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[4096];
		int written = 0, n = 0;
		while ((n = in.read(buffer)) != -1) {
			out.write(buffer, 0, n);
			written += n;
		}
		return written;

	}

	public static String getJsonStringValue(Reader in, String key) {
		JsonReader json = new JsonReader(in);
		try {
			try {
				json.beginObject();
				while (json.hasNext()) {
					if (json.nextName().equals(key)) {
						return json.nextString();
					} else {
						json.skipValue();
					}
				}
				json.endObject();
			} finally {
				json.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
