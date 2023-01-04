package com.xhx.bookread.adapter;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.pdfsearch.R;
import com.xhx.bookread.bean.FolderBean;


import java.util.List;

public class HomeFeiLeiAdapter extends BaseQuickAdapter<FolderBean, BaseViewHolder> {

    private final int[] itemBgRes = new int[]{R.drawable.bg_home_item_1, R.drawable.bg_home_item_2, R.drawable.bg_home_item_3,
            R.drawable.bg_home_item_4, R.drawable.bg_home_item_5, R.drawable.bg_home_item_6};
    private final int[] iconRes = new int[]{R.mipmap.png_fenlei_1, R.mipmap.png_fenlei_2, R.mipmap.png_fenlei_3,
            R.mipmap.png_fenlei_4, R.mipmap.png_fenlei_5, R.mipmap.png_fenlei_6};


    public HomeFeiLeiAdapter(List<FolderBean> pics) {
        super(R.layout.item_home_fenlei, pics);
    }

    @Override
    protected void convert(BaseViewHolder helper,  FolderBean item) {

        ImageView ivItemImg = helper.getView(R.id.ivItemImg);
        TextView tvItemImg = helper.getView(R.id.tvItemImg);
        LinearLayout viewRoot=helper.getView(R.id.viewRoot);
        int index = getItemPosition(item) % 6;
        ivItemImg.setImageResource(iconRes[index]);
        viewRoot.setBackgroundResource(itemBgRes[index]);
        tvItemImg.setText(item.getFolderName());
    }
}

