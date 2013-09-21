package at.ac.uibk.akari.utils;

public class StringUtils {

	public static String convertSecondsToTimeString(final long timeSeconds) {
		StringBuffer buffer = new StringBuffer();
		long mSeconds = timeSeconds;
		long mMinutes = mSeconds / 60;
		int mHours = (int) (mMinutes / 60);
		int mDays = mHours / 24;

		int days = mDays; // days
		int hours = mHours % 24; // hours
		int minutes = (int) (mMinutes % 60); // minutes
		int seconds = (int) (mSeconds % 60); // seconds

		if (days > 0) {
			buffer.append(String.format("%02d:", days));
		}
		if ((days > 0) || (hours > 0)) {
			buffer.append(String.format("%02d:", hours));
		}
		buffer.append(String.format("%02d:", minutes));
		buffer.append(String.format("%02d", seconds));

		return buffer.toString();
	}

}
