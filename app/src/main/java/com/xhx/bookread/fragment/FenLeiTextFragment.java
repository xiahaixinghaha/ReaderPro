package com.xhx.bookread.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.pdfsearch.R;
import com.orm.SugarRecord;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.xhx.bookread.activity.FenLeiActivity;
import com.xhx.bookread.activity.MBaseActivity;
import com.xhx.bookread.adapter.FileFenLeiAdapter;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.bean.FilesBeanText;
import com.xhx.bookread.bean.FilesBeanVideo;
import com.xhx.bookread.bean.FilesBeanVoice;
import com.xhx.bookread.bean.FolderBean;
import com.xhx.bookread.configs.DatabaseConfig;
import com.xhx.bookread.util.PublicUtil;
import com.xhx.bookread.view.dialog.FenLeiPopupWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FenLeiTextFragment extends MBaseFragment {
    FileFenLeiAdapter fileFenLeiAdapter;
    List<FilesBean> filesBeans = new ArrayList<>();
    private final int pageSize = 10;
    private int page = 1;
    RefreshLayout refreshLayout;
    TextView tvFenLei;
    FenLeiPopupWindow fenLeiPopupWindow;

    @Override
    public int getLayoutResId() {
        return R.layout.fragment_fenlei_text;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvBooks = view.findViewById(R.id.rvBooks);
        rvBooks.setLayoutManager(new GridLayoutManager(getContext(), 2));
        fileFenLeiAdapter = new FileFenLeiAdapter(filesBeans, true);
        rvBooks.setAdapter(fileFenLeiAdapter);
        fileFenLeiAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                FilesBean filesBean = (FilesBean) adapter.getItem(position);
                PublicUtil.openFile((MBaseActivity) getActivity(), filesBean);
            }
        });

        refreshLayout = (RefreshLayout) view.findViewById(R.id.refreshLayout);
        refreshLayout.setRefreshFooter(new ClassicsFooter(getContext()));
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                getData();
            }
        });
        view.findViewById(R.id.tvDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!fileFenLeiAdapter.checkedBean.isEmpty()) {
                    for (FilesBean filesBean : fileFenLeiAdapter.checkedBean) {
                        //移除适配器数据
                        fileFenLeiAdapter.remove(filesBean);
                        //删除数据库数据
                        deleteTableData(filesBean);
                        //删除文件
                        File file = new File(filesBean.getFilePath());
                        if (file.exists()) {
                            file.delete();
                        }
                        fileFenLeiAdapter.notifyDataSetChanged();
                    }
                    fileFenLeiAdapter.checkedBean.clear();
                }
            }
        });

        CheckBox checkBox = view.findViewById(R.id.checkboxAll);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                fileFenLeiAdapter.checkAll(b);
            }
        });
        tvFenLei = view.findViewById(R.id.tvFenLei);


        getData();
    }

    private void deleteTableData(FilesBean filesBean) {
        SugarRecord.deleteAll(FilesBean.class, DatabaseConfig.searchFileSql, filesBean.getFilePath());
    }

    @Override
    public void attachView() {

    }

    @Override
    public void initDatas() {

    }

    @Override
    public void configViews() {

    }


    int kind = -1;
    int index = -1;

    private void getData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            kind = bundle.getInt(FenLeiActivity.KEY_ARG);
            index = bundle.getInt(FenLeiActivity.KEY_INDEX);
            loadMore(kind, index);
            List<FolderBean> folderBeans = FolderBean.listAll(FolderBean.class);

            fenLeiPopupWindow = new FenLeiPopupWindow(getActivity(), folderBeans, new FenLeiPopupWindow.OnMenuItemClickListener() {
                @Override
                public void onItemClick(FolderBean folderBean, int position) {
                    if (position == -1) {
                        index = -1;
                        tvFenLei.setText("全部");
                    } else {
                        tvFenLei.setText(folderBean.getFolderName());
                        index = folderBean.getFolderIndex();
                    }
                    page = 1;
                    loadMore(kind, index);
                }
            });
            tvFenLei.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fenLeiPopupWindow.show(tvFenLei);
                }
            });
            if (index == -1)
                tvFenLei.setText("全部");
            else
                tvFenLei.setText(folderBeans.get(index).getFolderName());

        }
    }

    private void loadMore(int kind, int index) {
        if (page == 1) {
            filesBeans.clear();
        }
        int offset = (page - 1) * pageSize;
        int limit = pageSize;
        String sql = DatabaseConfig.searchFileByFolderIndex;

        switch (kind) {
            case 1:
                List<FilesBeanText> filesBean11s;
                if (index == -1) {
                    sql = DatabaseConfig.getQueryByLimitSql(DatabaseConfig.FILES_TAB_NAME_TEXT, limit, offset);
                    filesBean11s = SugarRecord.findWithQuery(FilesBeanText.class, sql);
                } else {
                    sql = DatabaseConfig.searchFileByFolderIndex;
                    filesBean11s = SugarRecord.find(FilesBeanText.class, sql, index + "", limit + "", offset + "");
                }
                for (FilesBeanText filesBean11 : filesBean11s) {
                    addToList(filesBean11.getFileName(), filesBean11.getFilePath(), filesBean11.getFileType(),
                            filesBean11.getFolderIndex(), filesBean11.getAddDate(), filesBean11.getClassify());
                }
                break;
            case 2:

                List<FilesBeanVoice> filesBean12s;
                if (index == -1) {
                    sql = DatabaseConfig.getQueryByLimitSql(DatabaseConfig.FILES_TAB_NAME_VOICE, limit, offset);
                    filesBean12s = SugarRecord.findWithQuery(FilesBeanVoice.class, sql);
                } else {
                    sql = DatabaseConfig.searchFileByFolderIndex;
                    filesBean12s = SugarRecord.find(FilesBeanVoice.class, sql, index + "", limit + "", offset + "");
                }
                for (FilesBeanVoice filesBean11 : filesBean12s) {
                    addToList(filesBean11.getFileName(), filesBean11.getFilePath(), filesBean11.getFileType(),
                            filesBean11.getFolderIndex(), filesBean11.getAddDate(), filesBean11.getClassify());
                }
                break;
            case 3:

                List<FilesBeanVideo> filesBean13s;
                if (index == -1) {
                    sql = DatabaseConfig.getQueryByLimitSql(DatabaseConfig.FILES_TAB_NAME_VIDEO, limit, offset);
                    filesBean13s = SugarRecord.findWithQuery(FilesBeanVideo.class, sql);
                } else {
                    sql = DatabaseConfig.searchFileByFolderIndex;
                    filesBean13s = SugarRecord.find(FilesBeanVideo.class, sql, index + "", limit + "", offset + "");
                }
                for (FilesBeanVideo filesBean11 : filesBean13s) {
                    addToList(filesBean11.getFileName(), filesBean11.getFilePath(), filesBean11.getFileType(),
                            filesBean11.getFolderIndex(), filesBean11.getAddDate(), filesBean11.getClassify());
                }
                break;

        }
        if (!filesBeans.isEmpty()) {
            fileFenLeiAdapter.notifyDataSetChanged();
            if (filesBeans.size() < pageSize * page) {
                refreshLayout.finishLoadMoreWithNoMoreData();
            } else {
                page++;
            }
        }
        refreshLayout.finishLoadMore();
    }

    private void addToList(String fileName, String filePath, String fileType, int folderIndex, String addDate, int classify) {
        filesBeans.add(new FilesBean(filePath, fileName, fileType, addDate, classify, folderIndex));
    }
}
