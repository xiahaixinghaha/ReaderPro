package com.xhx.bookread.adapter;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.pdfsearch.R;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.bean.FilesBeanText;
import com.xhx.bookread.util.FileTypeUtil;
import com.xhx.bookread.util.epubinfo.BookModel;
import com.xhx.bookread.util.epubinfo.ReadEpubHeadInfo;

import java.util.List;

/**
 * @作者: njb
 * @时间: 2019/9/11 20:31
 * @描述: pdf文件适配器类
 */
public class BottomTuiJianAdapter extends BaseQuickAdapter<FilesBeanText, BaseViewHolder> {

    public BottomTuiJianAdapter(@Nullable List<FilesBeanText> data) {
        super(R.layout.item_bottom_tj, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, FilesBeanText item) {
        if (item == null) {
            return;
        }
        ImageView ivFileImg = helper.getView(R.id.ivFileImg);
        BookModel bookModel = null;
        bookModel = ReadEpubHeadInfo.getePubBook(item.getFilePath());
        if (bookModel != null) {
            ivFileImg.setImageBitmap(BitmapFactory.decodeFile(bookModel.getCover()));
            Log.i("epub", (bookModel.getCover() != null ? "有封面  " : "无封面  ") + bookModel.getName() + "  书封面图片路径：" + bookModel.getCover());
        } else {
            ivFileImg.setImageResource(R.mipmap.png_book);
        }
    }

}
