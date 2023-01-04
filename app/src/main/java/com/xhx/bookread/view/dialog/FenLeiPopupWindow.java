package com.xhx.bookread.view.dialog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.pdfsearch.R;
import com.xhx.bookread.adapter.FenLeiPopAdapter;
import com.xhx.bookread.bean.FolderBean;

import java.util.List;

public class FenLeiPopupWindow extends PopupWindow {

    private View m_view;
    RecyclerView rvFenFei;
    Activity mActivity;

    public FenLeiPopupWindow(Activity activity, List<FolderBean> folderBeans, final OnMenuItemClickListener listener) {
        super(activity);
        this.mActivity = activity;
        // 初始化view
        m_view = LayoutInflater.from(activity).inflate(R.layout.layout_fenlei_pop, null);
        rvFenFei = m_view.findViewById(R.id.rvFenFei);
        RecyclerView rvFenFei = m_view.findViewById(R.id.rvFenFei);
        rvFenFei.setLayoutManager(new LinearLayoutManager(activity));
        FenLeiPopAdapter fenLeiPopAdapter = new FenLeiPopAdapter(folderBeans);
        View view = LayoutInflater.from(activity).inflate(R.layout.item_pop_fenlei, null);
        TextView tvPopItemName = view.findViewById(R.id.tvPopItemName);
        tvPopItemName.setText("全部");
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(null,-1);
                dismiss();
            }
        });
        fenLeiPopAdapter.addHeaderView(view);

        rvFenFei.setAdapter(fenLeiPopAdapter);
        fenLeiPopAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                FolderBean folderBean = (FolderBean) adapter.getItem(position);
                listener.onItemClick(folderBean, position);
                dismiss();
            }
        });

        // 设置自定义PopupWindow的View
        this.setContentView(m_view);
        // 设置自定义PopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置自定义PopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置自定义PopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置自定义PopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.popWinAnimBot);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置自定义PopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        // popupwindow消失，恢复透明度
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.alpha = 1f;
                activity.getWindow().setAttributes(lp);
            }
        });


    }

    /**
     * 显示dlg，window用来设置背景变暗
     *
     * @param parent
     */
    public void show(View parent) {
        //一定要设置宽和高
        setWidth(parent.getWidth());
        setHeight(RecyclerView.LayoutParams.WRAP_CONTENT);
        showAsDropDown(parent);
        // 设置背景变暗
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = 0.5f;
    }

    /**
     * 定义接口
     */
    public interface OnMenuItemClickListener {
        void onItemClick(FolderBean folderBean, int postion);
    }
}
