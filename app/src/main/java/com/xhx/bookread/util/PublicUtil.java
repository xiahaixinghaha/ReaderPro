package com.xhx.bookread.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.xhx.bookread.activity.MBaseActivity;
import com.xhx.bookread.activity.ReadPDFActivity;
import com.xhx.bookread.activity.VideoAndVoicePlayerActivity;
import com.xhx.bookread.bean.BookshelfNovelDbData;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.configs.DatabaseConfig;
import com.xhx.bookread.newepubread.MEpubUtils;
import com.xhx.bookread.newepubread.ReadActivity;

import java.io.File;
import java.util.List;

public class PublicUtil {


    public static Uri pathToUri(String filePath) {
        File picPath = new File(filePath);
        Uri uri = null;
        if (picPath.exists()) {
            uri = Uri.fromFile(picPath);
        }
        return uri;
    }

    public static void fileFenLei(List<FilesBean> filesBeans,
                                  List<FilesBean> bookFiles,
                                  List<FilesBean> videoFiles,
                                  List<FilesBean> voiceFiles) {
        for (FilesBean filesBean : filesBeans) {
            for (String bookType : FileTypeUtil.BOOK_TYPES) {
                if (filesBean.getFileName().endsWith(bookType)) {
                    bookFiles.add(filesBean);
                }
            }
            for (String videoType : FileTypeUtil.VIDEO_TYPES) {
                if (filesBean.getFileName().endsWith(videoType)) {
                    videoFiles.add(filesBean);
                }
            }
            for (String voiceType : FileTypeUtil.VOICE_TYPES) {
                if (filesBean.getFileName().endsWith(voiceType)) {
                    voiceFiles.add(filesBean);
                }
            }

        }
    }

    public static void openFile(MBaseActivity activity, FilesBean filesBean) {
        switch (FileTypeUtil.fileTypeFenLei(filesBean.getFileName())) {
            case FileTypeUtil.CLASSIFY_BOOK:
                String filePath = filesBean.getFilePath();
                if (!new File(filePath).exists()) {
                    activity.showToast("文件不存在或格式错误！");
                    return;
                }
                if (filePath.endsWith(FileTypeUtil.TYPE_TXT)) {
                    // TXT

                } else if (filePath.endsWith(FileTypeUtil.TYPE_PDF)) {
                    // PDF
                    ReadPDFActivity.start(activity, filesBean);
                } else if (filePath.endsWith(FileTypeUtil.TYPE_EPUB)) {
                    // EPub
//                    ReadEPubActivity.start(activity, filesBean);
//                    ReadEPubActivity1.start(activity, filesBean);
//                    ReadActivity.start(activity, filesBean);
                    openEpubFile(activity, filesBean.getFilePath());
                } else if (filePath.endsWith(FileTypeUtil.TYPE_CHM)) {
                    // CHM
//            ReadCHMActivity.start(this, filePath);
                }
                break;
            case FileTypeUtil.CLASSIFY_VOICE:
            case FileTypeUtil.CLASSIFY_VIDEO:
                VideoAndVoicePlayerActivity.startVVPlayerActivity(activity, filesBean);
                break;
        }


    }

    private static void openEpubFile(Context context, String filePath) {

        List<BookshelfNovelDbData> list = BookshelfNovelDbData.find(BookshelfNovelDbData.class, DatabaseConfig.searchEpubSql, new String[]{filePath});
        List<BookshelfNovelDbData> list1 = BookshelfNovelDbData.listAll(BookshelfNovelDbData.class);
        if (list != null && list.size() > 0) {
            startReadActivity(context, list.get(0));
        } else {
            firstOpenHandle(context, filePath);
        }

    }

    private static void firstOpenHandle(Context context, String filePath) {
       new MEpubUtils(context). unZipEpub(filePath);
    }


    public static void startReadActivity(Context context, BookshelfNovelDbData bookshelfNovelDbData) {
        Intent intent = new Intent(context, ReadActivity.class);
        // 小说 url  文件路径
        intent.putExtra(ReadActivity.KEY_NOVEL_URL, bookshelfNovelDbData.getNovelUrl());
        // 小说名 fileName
        intent.putExtra(ReadActivity.KEY_NAME, bookshelfNovelDbData.getName());
        // 小说封面 url
        intent.putExtra(ReadActivity.KEY_COVER, bookshelfNovelDbData.getCover());
        // 小说类型
        intent.putExtra(ReadActivity.KEY_TYPE, bookshelfNovelDbData.getType());
        // 开始阅读的位置
        intent.putExtra(ReadActivity.KEY_CHAPTER_INDEX, bookshelfNovelDbData.getChapterIndex());
        intent.putExtra(ReadActivity.KEY_POSITION, bookshelfNovelDbData.getPosition());
        intent.putExtra(ReadActivity.KEY_SECOND_POSITION, bookshelfNovelDbData.getSecondPosition());
        context.startActivity(intent);
    }

}
