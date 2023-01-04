package com.xhx.bookread.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.pdfsearch.R;
import com.xhx.bookread.bean.FolderBean;

public class ViewHomeItem extends RelativeLayout {
    Context mContext;
    FolderBean mFolderBean;

    public ViewHomeItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        inflate(mContext, R.layout.item_home_fenlei_view, this);

    }

    public void setTxtAndImg(String str, int imgRes, int bgRes) {
        ImageView ivItemImg = findViewById(R.id.ivItemImg);
        TextView tvItemImg = findViewById(R.id.tvItemImg);
        ivItemImg.setImageResource(imgRes);
        tvItemImg.setText(str);
        View rlRoot=findViewById(R.id.rlRoot);
        rlRoot.setBackgroundResource(bgRes);
    }

    public void bindData(FolderBean folderBean) {
        this.mFolderBean = folderBean;
    }
    public FolderBean getFolderBean(){
        return mFolderBean;
    }


}
