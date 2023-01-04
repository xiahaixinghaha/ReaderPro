package com.xhx.bookread.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.pdfsearch.R;
import com.xhx.bookread.bean.FolderBean;

import java.util.List;

public class FenLeiPopAdapter extends BaseQuickAdapter<FolderBean, BaseViewHolder> {

    public FenLeiPopAdapter(List<FolderBean> folderBeans) {
        super(R.layout.item_pop_fenlei, folderBeans);
    }

    @Override
    protected void convert(BaseViewHolder helper, FolderBean folderBean) {
        helper.setText(R.id.tvPopItemName, folderBean.getFolderName());
    }
}

