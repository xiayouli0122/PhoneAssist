package com.yuri.phoneassistant;

import java.text.DecimalFormat;

public class StringUtils {
    private static DecimalFormat s_decimal2Format = new DecimalFormat("#.00");

    
    
    
    public static String kbytes2FileSizeString(float fileSize) {
	return bytes2FileSizeString(fileSize * 1024);
    }

    public static String bytes2FileSizeString(float fileSize) {
	if (fileSize < 1024) {
	    return fileSize + "Byte";
	} else if (fileSize < Math.pow(1024, 2)) {
	    return s_decimal2Format.format(fileSize / 1024) + "KB";
	} else if (fileSize < Math.pow(1024, 3)) {
	    return s_decimal2Format.format(fileSize / Math.pow(1024, 2)) + "MB";
	} else if (fileSize < Math.pow(1024, 4)) {
	    return s_decimal2Format.format(fileSize / Math.pow(1024, 3)) + "GB";
	} else {
	    return s_decimal2Format.format(fileSize / Math.pow(1024, 4)) + "TB";
	}
    }

}
