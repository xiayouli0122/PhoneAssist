package com.yuri.phoneassistant.camera;

import com.yuri.phoneassistant.Log;


public class Exif {
	private static final String TAG = "CameraExif";

	// Returns the degrees in clockwise. Values are 0, 90, 180, or 270.
	public static int getOrientation(byte[] jpeg) {
		int orientation = (int) readTag(jpeg, 0x0112, false);
		switch (orientation) {
		case 1:
			return 0;
		case 3:
			return 180;
		case 6:
			return 90;
		case 8:
			return 270;
		}
		Log.i(TAG, "Orientation not found");
		return 0;
	}

	public static int getGroupIndex(byte[] jpeg) {
		return (int) readTag(jpeg, 0x0220, false);
	}

	public static long getGroupId(byte[] jpeg) {
		return readTag(jpeg, 0x0221, true);
	}

	public static long getFocusValueHigh(byte[] jpeg) {
		return readTag(jpeg, 0x0222, true);
	}

	public static long getFocusValueLow(byte[] jpeg) {
		return readTag(jpeg, 0x0223, true);
	}

	private static long pack(byte[] bytes, int offset, int length,
			boolean littleEndian, boolean isMoveLong) {
		int step = 1;
		if (littleEndian) {
			offset += length - 1;
			step = -1;
		}
		if (isMoveLong) {
			long value = 0;
			while (length-- > 0) {
				value = (value << 8) | (bytes[offset] & 0xFF);
				offset += step;
			}
			return value;
		} else {
			int value = 0;
			while (length-- > 0) {
				value = (value << 8) | (bytes[offset] & 0xFF);
				offset += step;
			}
			return value;
		}
	}

	private static long readTag(byte[] jpeg, int targetTag, boolean isMoveLong) {
		if (jpeg == null) {
			return 0;
		}
		int offset = 0;
		int length = 0;
		// ISO/IEC 10918-1:1993(E)
		while (offset + 3 < jpeg.length && (jpeg[offset++] & 0xFF) == 0xFF) {
			int marker = jpeg[offset] & 0xFF;
			// Check if the marker is a padding.
			if (marker == 0xFF) {
				continue;
			}
			offset++;
			// Check if the marker is SOI or TEM.
			if (marker == 0xD8 || marker == 0x01) {
				continue;
			}
			// Check if the marker is EOI or SOS.
			if (marker == 0xD9 || marker == 0xDA) {
				break;
			}
			// Get the length and check if it is reasonable.
			length = (int) pack(jpeg, offset, 2, false, false);
			if (length < 2 || offset + length > jpeg.length) {
				Log.e(TAG, "Invalid length");
				return 0;
			}
			// Break if the marker is EXIF in APP1.
			if (marker == 0xE1
					&& length >= 8
					&& (int) pack(jpeg, offset + 2, 4, false, false) == 0x45786966
					&& (int) pack(jpeg, offset + 6, 2, false, false) == 0) {
				offset += 8;
				length -= 8;
				break;
			}
			// Skip other markers.
			offset += length;
			length = 0;
		}
		// JEITA CP-3451 Exif Version 2.2
		if (length > 8) {
			// Identify the byte order.
			int tag = (int) pack(jpeg, offset, 4, false, false);
			if (tag != 0x49492A00 && tag != 0x4D4D002A) {
				Log.e(TAG, "Invalid byte order");
				return 0;
			}
			boolean littleEndian = (tag == 0x49492A00);
			// Get the offset and check if it is reasonable.
			int count = (int) pack(jpeg, offset + 4, 4, littleEndian, false) + 2;
			if (count < 10 || count > length) {
				Log.e(TAG, "Invalid offset");
				return 0;
			}
			offset += count;
			length -= count;
			// Get the count and go through all the elements.
			count = (int) pack(jpeg, offset - 2, 2, littleEndian, false);
			while (count-- > 0 && length >= 12) {
				// Get the tag and check if it is orientation.
				tag = (int) pack(jpeg, offset, 2, littleEndian, false);
				if (tag == targetTag) {
					// We do not really care about type and count, do we?
					if (isMoveLong) {
						long longVale = pack(jpeg, offset + 8, 4, littleEndian,
								true);
						return longVale;
					} else {
						int intValue = (int) pack(jpeg, offset + 8, 2,
								littleEndian, false);
						return intValue;
					}
				}
				offset += 12;
				length -= 12;
			}
		}
		return -1;
	}
}
