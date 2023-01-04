package com.xhx.bookread.util;

import android.text.TextUtils;

import com.xhx.bookread.bean.FilesBean;

public class FileTypeUtil {
    public static final String TYPE_EPUB = ".epub";
    public static final String TYPE_CHM = ".chm";
    public static final String TYPE_PDF = ".pdf";
    public static final String TYPE_TXT = ".txt";
    public static final String TYPE_MP3 = ".mp3";
    public static final String TYPE_MP4 = ".mp4";
    public static final String TYPE_ZIP = ".zip";
    public static final String[] BOOK_TYPES = new String[]{TYPE_EPUB, TYPE_PDF, TYPE_TXT,TYPE_CHM};
    public static final String[] VOICE_TYPES = new String[]{TYPE_MP3};
    public static final String[] VIDEO_TYPES = new String[]{TYPE_MP4};
    public static final int CLASSIFY_BOOK = 1;
    public static final int CLASSIFY_VOICE = 2;
    public static final int CLASSIFY_VIDEO = 3;

    public static int fileTypeFenLei(String fn) {
        int fenLei = 0;
        if (TextUtils.isEmpty(fn)) return -1;
        for (String bookType : BOOK_TYPES) {
            if (fn.endsWith(bookType)) {
                fenLei = CLASSIFY_BOOK;
            }
        }
        for (String voiceType : VOICE_TYPES) {
            if (fn.endsWith(voiceType)) {
                fenLei = CLASSIFY_VOICE;
            }
        }
        for (String videoType : VIDEO_TYPES) {
            if (fn.endsWith(videoType)) {
                fenLei = CLASSIFY_VIDEO;
            }
        }
        return fenLei;
    }
}
