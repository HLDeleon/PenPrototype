package com.android.samplesmartpen.utilities;

import android.os.Environment;

public class Const {
    public static String SAMPLE_FOLDER_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SamplePen";


    public class Setting {
        public final static String KEY_PASSWORD = "password";
        public final static String KEY_PEN_COLOR = "pen_color";
        public final static String KEY_AUTO_POWER_ON = "auto_power_onoff";
        public final static String KEY_BEEP = "beep_onoff";
        public final static String KEY_AUTO_POWER_OFF_TIME = "auto_power_off_time";
        public final static String KEY_SENSITIVITY = "sensitivity";
        public final static String KEY_OFFLINE_DATA_SAVE = "offlinedata_save";
        public final static String KEY_HOVER_MODE = "hover_onoff";
        public final static String KEY_PEN_CAP_ON = "pencap_onoff";

        public final static String KEY_MAC_ADDRESS = "mac_address";


        public final static String EXTRA_DEVICE_ADDRESS = "device_address";
        public final static String EXTRA_IS_BLUETOOTH_LE = "is_bluetooth_le";
    }

    public class JsonTag {
        public final static String STRING_PROTOCOL_VERSION = "protocol_version";
        public final static String INT_TIMEZONE_OFFSET = "timezone";
        public final static String LONG_TIMETICK = "timetick";
        public final static String INT_MAX_FORCE = "force_max";
        public final static String INT_BATTERY_STATUS = "battery";
        public final static String INT_MEMORY_STATUS = "used_memory";
        public final static String INT_PEN_COLOR = "pen_tip_color";
        public final static String BOOL_AUTO_POWER_ON = "auto_power_onoff";
        public final static String BOOL_PEN_CAP_ON = "pencap_onoff";

        public final static String BOOL_ACCELERATION_SENSOR = "acceleration_sensor_onoff";
        public final static String BOOL_HOVER = "hover_mode";
        public final static String BOOL_OFFLINE_DATA_SAVE = "offlinedata_save";
        public final static String BOOL_BEEP = "beep";
        public final static String INT_AUTO_POWER_OFF_TIME = "auto_power_off_time";
        public final static String INT_PEN_SENSITIVITY = "sensitivity";

        public final static String INT_TOTAL_SIZE = "total_size";
        public final static String INT_SENT_SIZE = "sent_size";
        public final static String INT_RECEIVED_SIZE = "received_size";

        public final static String INT_SECTION_ID = "section_id";
        public final static String INT_OWNER_ID = "owner_id";
        public final static String INT_NOTE_ID = "note_id";
        public final static String INT_PAGE_ID = "page_id";
        public final static String STRING_FILE_PATH = "file_path";

        public final static String INT_PASSWORD_RETRY_COUNT = "retry_count";
        public final static String INT_PASSWORD_RESET_COUNT = "reset_count";

        public final static String BOOL_RESULT = "result";
    }

    public class Broadcast {
        public static final String PEN_ADDRESS = "pen_address";

        public static final String ACTION_PEN_MESSAGE = "action_pen_message";
        public static final String MESSAGE_TYPE = "message_type";
        public static final String CONTENT = "content";

        public static final String ACTION_SYMBOL_ACTION = "symbol_action";
        public static final String ACTION_WRITE_PAGE_CHANGED = "write_page_changed";
        public static final String EXTRA_SECTION_ID = "sectionId";
        public static final String EXTRA_OWNER_ID = "ownerId";
        public static final String EXTRA_BOOKCODE_ID = "bookcodeId";
        public static final String EXTRA_PAGE_NUMBER = "page_number";
        public static final String EXTRA_SYMBOL_ID = "symbolId";

        public static final String ACTION_PEN_DOT = "action_pen_dot";
        public static final String ACTION_OFFLINE_STROKES = "action_offline_strokes";
        public static final String EXTRA_DOT = "dot";
        public static final String EXTRA_OFFLINE_STROKES = "offline_strokes";
//		public static final String SECTION_ID = "sectionId";
//		public static final String OWNER_ID = "ownerId";
//		public static final String NOTE_ID = "noteId";
//		public static final String PAGE_ID = "pageId";
//		public static final String X = "x";
//		public static final String Y = "y";
//		public static final String FX = "fx";
//		public static final String FY = "fy";
//		public static final String PRESSURE = "pressure";
//		public static final String TIMESTAMP = "timestamp";
//		public static final String TYPE = "type";
//		public static final String COLOR = "color";

        public static final String ACTION_PEN_UPDATE = "action_firmware_update";

        public static final String ACTION_RECTANGLE_CHECK = "rectangle_checked";
        public static final String ACTION_PEN_DOT_DOWN = "action_pen_dot_down";
        public static final String ACTION_PEN_DOT_MOVE = "action_pen_dot_move";
        public static final String ACTION_PEN_DOT_UP = "action_pen_dot_up";

    }

    public static final String HOST = "http://192.168.1.135/NeoSmartpen/";
    public static final String PHP_CATEGORY_LIST = HOST + "categorylist.php";
    public static final String PHP_SETS_LIST = HOST + "sets.php?ID=%d";

    public static final String SET_ZIP_FILE_FORMAT = "%06d.zip";
    public static final String SET_ZIP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SamplePen/data/zsets";
    public static final String SET_ZIP_FILE = SET_ZIP_DIR + "/" + SET_ZIP_FILE_FORMAT;
    public static final String SET_CONTENT_FILE_FORMAT = "%d_%d.mp3";
    public static final String SET_CONTENT_FILE_IMAGE_FORMAT = "%d_%d.jpg";

    public static final byte[] NEOLAB_PROFILE_PASS = {(byte) 0x6B, (byte) 0xCA, (byte) 0x6B, (byte) 0x50, (byte) 0x5D, (byte) 0xEC, (byte) 0xA7, (byte) 0x8C};
    public static int SetID = 0, CategoryID = 0;
}
