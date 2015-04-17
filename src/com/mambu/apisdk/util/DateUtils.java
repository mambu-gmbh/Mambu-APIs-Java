package com.mambu.apisdk.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Formats date classes into the ISO standard
 * 
 * @author edanilkis
 * 
 */
public class DateUtils {

	public static String DATE_FORMAT = "yyyy-MM-dd";

	public static SimpleDateFormat FORMAT = new SimpleDateFormat(DATE_FORMAT);

	public static String format(Date date) {
		if (date == null) {
			return null;
		}
		return FORMAT.format(date);
	}
}
