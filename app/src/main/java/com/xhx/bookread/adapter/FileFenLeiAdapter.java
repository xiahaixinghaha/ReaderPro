package com.xhx.bookread.adapter;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.pdfsearch.R;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.util.FileTypeUtil;
import com.xhx.bookread.util.epubinfo.BookModel;
import com.xhx.bookread.util.epubinfo.ReadEpubHeadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @作者: njb
 * @时间: 2019/9/11 20:31
 * @描述: pdf文件适配器类
 */
public class FileFenLeiAdapter extends BaseQuickAdapter<FilesBean, BaseViewHolder> {
    public List<FilesBean> checkedBean = new ArrayList<>();
    private boolean checkAll = false;
    boolean mCheckBoxShow=false;
    public FileFenLeiAdapter(@Nullable List<FilesBean> data,boolean isCheckBoxShow) {
        super(R.layout.item_fenlei_feiles, data);
        this.mCheckBoxShow=isCheckBoxShow;
    }

    @Override
    protected void convert(BaseViewHolder helper, FilesBean item) {
        if (item == null) {
            return;
        }
        helper.setText(R.id.tvDateTime, item.getAddDate());
        ImageView ivFileImg = helper.getView(R.id.ivFileImg);
        CheckBox checkBox = helper.getView(R.id.checkbox);
        if (mCheckBoxShow){
            checkBox.setVisibility(View.VISIBLE);
        }else {
            checkBox.setVisibility(View.GONE);
        }

        checkBox.setChecked(checkAll);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkedBean.add(item);
                } else {
                    checkedBean.remove(item);
                }
            }
        });
        BookModel bookModel = null;
        if (item.getFilePath().endsWith(FileTypeUtil.TYPE_EPUB)) {
            bookModel = ReadEpubHeadInfo.getePubBook(item.getFilePath());
            if (bookModel != null) {
                helper.setText(R.id.tvFileName, bookModel.getName());
                ivFileImg.setImageBitmap(BitmapFactory.decodeFile(bookModel.getCover()));
                Log.i("epub", (bookModel.getCover() != null ? "有封面  " : "无封面  ") + bookModel.getName() + "  书封面图片路径：" + bookModel.getCover());
            } else {
                ivFileImg.setImageResource(R.mipmap.png_book);
            }
        } else if (item.getFilePath().endsWith(FileTypeUtil.TYPE_MP4)) {
            helper.setText(R.id.tvFileName, item.getFileName());
            Glide.with(getContext()).load(item.getFilePath()).into(ivFileImg);
        } else {
            helper.setText(R.id.tvFileName, item.getFileName());
            int fl = FileTypeUtil.fileTypeFenLei(item.getFileName());
            int pngResId = R.mipmap.png_book;
            switch (fl) {
                case FileTypeUtil.CLASSIFY_BOOK:
                    pngResId = R.mipmap.png_book;
                    break;
                case FileTypeUtil.CLASSIFY_VOICE:
                    pngResId = R.mipmap.png_voice;
                    break;
                case FileTypeUtil.CLASSIFY_VIDEO:
                    pngResId = R.mipmap.png_video;
                    break;
                default:
                    break;
            }
            ivFileImg.setImageResource(pngResId);
        }
    }

    public void checkAll(boolean b) {
        this.checkAll = b;

        notifyDataSetChanged();
    }



}
