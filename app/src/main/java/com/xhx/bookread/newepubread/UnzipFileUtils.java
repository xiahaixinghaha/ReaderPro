package com.xhx.bookread.newepubread;

import android.util.Log;

import com.xhx.bookread.bean.BookshelfNovelDbData;
import com.xhx.bookread.configs.Constant;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

public class UnzipFileUtils {
    String TAG = "UnzipFileUtils";

    private OpfData mOpfData;

    /**
     * 解压 epub 文件，解析 opf 文件后，返回 OpfData
     *
     * @param filePath
     */
    public void unZipEpub(final String filePath) {
        File file = new File(filePath);
        final String savePath = Constant.EPUB_SAVE_PATH + "/" + file.getName();
        File saveFile = new File(savePath);
        if (saveFile.exists()) {
            getOpfData(savePath);
            if (mOpfData != null) {
                unZipEpubSuccess(filePath, mOpfData);
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EpubUtils.unZip(filePath, savePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    unZipEpubError("解压失败，可能文件被加密");
                    return;
                }
                getOpfData(savePath);
                if (mOpfData != null) {
                    unZipEpubSuccess(filePath, mOpfData);
                }
            }
        }).start();
    }

    private void getOpfData(String savePath) {
        try {
            // 先得到 opf 文件的位置
            String opfPath = EpubUtils.getOpfPath(savePath);
            Log.d(TAG, "unZipEpub: opfPath = " + opfPath);
            // 解析 opf 文件
            mOpfData = EpubUtils.parseOpf(opfPath);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            unZipEpubError("解压失败，Xml 解析出错");
        } catch (IOException e) {
            e.printStackTrace();
            unZipEpubError("解压失败，I/O 错误");
        }
    }

    private void unZipEpubError(final String errorMsg) {
        Log.d(TAG, "unZipEpubError: 导入失败----" + errorMsg);
    }

    public void unZipEpubSuccess(String filePath, OpfData opfData) {
        // 将书籍信息写入数据库
        File file = new File(filePath);

        BookshelfNovelDbData dbData = new BookshelfNovelDbData(filePath, file.getName(),
                opfData.getCover(), 0, 0, 2);
        dbData.save();

    }
}
