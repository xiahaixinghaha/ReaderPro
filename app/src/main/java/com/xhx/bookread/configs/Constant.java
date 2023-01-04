package com.xhx.bookread.configs;

import androidx.annotation.StringDef;

import com.xhx.bookread.App;
import com.xhx.bookread.util.AppUtils;
import com.xhx.bookread.util.FileUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Constant {
    public static String PATH_DATA = FileUtils.createRootPath(App.instance) + "/cache";
    public static String PATH_TXT = PATH_DATA + "/book/";
    public static String PATH_EPUB = PATH_DATA + "/epub";
    public static String PATH_COLLECT = FileUtils.createRootPath(AppUtils.getAppContext()) + "/collect";
    public static final String ISBYUPDATESORT = "isByUpdateSort";
    public static final String ISNIGHT = "isNight";
    public static String BASE_PATH = AppUtils.getAppContext().getCacheDir().getPath();
    public static final String FLIP_STYLE = "flipStyle";
    /* 文件存储 */
    public static final String EPUB_SAVE_PATH = PATH_DATA + "/epubFile";
    // 该小说已从本地删除
    public static final String NOT_FOUND_FROM_LOCAL = "该小说已从本地删除";

    @StringDef({
            Gender.MALE,
            Gender.FEMALE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Gender {
        String MALE = "male";

        String FEMALE = "female";
    }
}
