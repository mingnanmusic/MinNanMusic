package org.loader.music.utils;

import android.util.Log;

/**
 * 2015年8月15日 16:34:37
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class L {
	private static final boolean debug = true;
	
	public static void l(String tag, Object msg) {
		Log.i(tag, msg.toString());
		//l(tag + "-->" + msg);
	}
	
	public static void l(Object msg) {
//		if(!debug) return;

	}
}
