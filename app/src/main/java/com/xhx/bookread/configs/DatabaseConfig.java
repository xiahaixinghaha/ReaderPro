package com.xhx.bookread.configs;

public class DatabaseConfig {
    //动态数据库
    public static final String DB_NAME_folder_files = "folderfiles";
    public static final int DB_VERSION = 2;

    //首页文件夹表
    public static final String FOLDER_TAB_NAME = "tab_folder";
    public static final String COLUMN_FOLDER_LONGID = "file_longid";
    public static final String COLUMN_FOLDER_NAME = "folder_name";
    public static final String COLUMN_FOLDER_PATH = "file_path";
    public static final String searchFolderSql = COLUMN_FOLDER_NAME + " = ?";


    //文件表
    public static final String FILES_TAB_NAME = "tab_files";
    public static final String FILES_TAB_NAME_VOICE = "tab_files_voice";
    public static final String FILES_TAB_NAME_TEXT = "tab_files_text";
    public static final String FILES_TAB_NAME_VIDEO = "tab_files_video";
    public static final String COLUMN_FOLDER_INDEX = "folder_index";
    public static final String COLUMN_FILE_PATH = "file_path";
    public static final String COLUMN_FILE_NAME = "file_name";
    public static final String COLUMN_FILE_TYPE = "file_type";
    public static final String COLUMN_FILE_DATE = "file_date";

    public static final String COLUMN_FILE_CLASSIFY = "file_classify";
    public static final String COLUMN_FILE_LONGID = "file_longid";
    public static final String searchFileSql = COLUMN_FILE_PATH + " = ?";
    public static final String searchFileByIdSql = COLUMN_FILE_LONGID + " > ? AND " + COLUMN_FILE_LONGID + "<= ?";
//    Limit 9 Offset 10
    public static final String searchFileByFolderIndex = COLUMN_FOLDER_INDEX+" = ? LIMIT ? OFFSET ?";
    public static final String searchFileLimit = "LIMIT ? OFFSET ?";

    public static String getQueryByNameSql() {
        return COLUMN_FILE_NAME + " LIKE ? LIMIT ? OFFSET ?";
    }

    public static String getQueryByLimitSql(String tabName,int limit,int offset) {
        return "SELECT * FROM "+tabName+" LIMIT "+limit+" OFFSET "+offset;
    }

    public static String getQueryRandom(String tabName,int limit) {
//        select * from artical_list order by random() limit 5
        return "select * from "+tabName+" where "+COLUMN_FILE_TYPE+" = 'epub' order by random() limit "+limit;

    }

    //文件表
    public static final String EPUB_TAB_NAME = "tab_epub_unzipfiles";
    public static final String COLUMN_novelUrl = "novelUrl";
    public static final String searchEpubSql = COLUMN_novelUrl + " = ?";


}
