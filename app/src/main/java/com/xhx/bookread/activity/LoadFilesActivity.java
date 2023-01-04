package com.xhx.bookread.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.example.pdfsearch.R;
import com.orm.SchemaGenerator;
import com.orm.SugarContext;
import com.orm.SugarDb;
import com.orm.SugarRecord;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.bean.FilesBeanText;
import com.xhx.bookread.bean.FilesBeanVideo;
import com.xhx.bookread.bean.FilesBeanVoice;
import com.xhx.bookread.bean.FolderBean;
import com.xhx.bookread.configs.DatabaseConfig;
import com.xhx.bookread.configs.DefConfig;
import com.xhx.bookread.util.DateUtil;
import com.xhx.bookread.util.FileTypeUtil;
import com.xhx.bookread.util.FileUtils;
import com.xhx.bookread.util.PermissionUtils;
import com.xhx.bookread.util.SpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadFilesActivity extends MBaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.an_loading);
        LinearInterpolator interpolator = new LinearInterpolator();
        rotateAnimation.setInterpolator(interpolator);
        ImageView ivLoading = findViewById(R.id.ivLoading);
        ivLoading.startAnimation(rotateAnimation);
        requestPermission();


    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_load_files;
    }

    private void requestPermission() {
        PermissionUtils.requestStoragePermission(this, new PermissionUtils.RequestCallBack() {
            @Override
            public void callBack(boolean isHave) {
                if (isHave) {
                    getFileData();
                } else {

                }
            }
        });
    }

    List<FilesBean> addFilesList = new ArrayList<>();

    private void getFileData() {
        clearDatabase();

        new Thread() {
            @Override
            public void run() {
                super.run();
                getFolders();
                if (!TextUtils.isEmpty(DefConfig.GEN_PATH)) {
                    List<FolderBean> folderBeans = FolderBean.listAll(FolderBean.class);
                    if (folderBeans != null && !folderBeans.isEmpty()) {
                        addFilesList.clear();
                        for (int i = 0; i < folderBeans.size(); i++) {
                            FolderBean folderBean = folderBeans.get(i);
                            getFilesFromDir(folderBean.getFilePath(), folderBean.getFolderIndex());
                        }
                        if (!addFilesList.isEmpty()) {
                            new SpUtils(LoadFilesActivity.this).putTJBooks(addFilesList);
                        }
                    }
                }
                loadCompt();
            }
        }.start();
    }

    private void clearDatabase() {

        SugarContext.terminate();
        SchemaGenerator schemaGenerator = new SchemaGenerator(this);
        schemaGenerator.deleteTables(new SugarDb(this).getDB());
        SugarContext.init(this);
        schemaGenerator.createDatabase(new SugarDb(this).getDB());
    }

    FileUtils fileUtils;

    private void getFilesFromDir(String filePath, int folderIndex) {
        if (fileUtils == null) fileUtils = FileUtils.getInstance();
        List<Map<String, Object>> list = fileUtils.getSonNode(filePath);
        if (list == null || list.isEmpty()) return;
        for (Map<String, Object> stringObjectMap : list) {
            String path = fileUtils.getFilePath(stringObjectMap);
            if (fileUtils.isDir(stringObjectMap)) {
                getFilesFromDir(path, folderIndex);
            } else {
                String name = fileUtils.getFileName(stringObjectMap);
                String type = fileUtils.getFileType(stringObjectMap);
                List<FilesBean> filesBeans = FilesBean.find(FilesBean.class, DatabaseConfig.searchFileSql, path);
                if (filesBeans == null || filesBeans.isEmpty()) {
                    FilesBean filesBean = new FilesBean(path, name, type, DateUtil.getCurrentDate(), FileTypeUtil.fileTypeFenLei(name), folderIndex);
                    filesBean.save();
                    SugarRecord sugarRecord = null;
                    int t = FileTypeUtil.fileTypeFenLei(filesBean.getFileName());
                    switch (t) {
                        case FileTypeUtil.CLASSIFY_BOOK:
                            sugarRecord = new FilesBeanText(path, name, type, DateUtil.getCurrentDate(), FileTypeUtil.fileTypeFenLei(name), folderIndex);
                            break;
                        case FileTypeUtil.CLASSIFY_VOICE:
                            sugarRecord = new FilesBeanVoice(path, name, type, DateUtil.getCurrentDate(), FileTypeUtil.fileTypeFenLei(name), folderIndex);
                            break;
                        case FileTypeUtil.CLASSIFY_VIDEO:
                            sugarRecord = new FilesBeanVideo(path, name, type, DateUtil.getCurrentDate(), FileTypeUtil.fileTypeFenLei(name), folderIndex);
                            break;
                    }
                    if (sugarRecord != null) {
                        sugarRecord.save();
                    }

                }
            }
        }
    }

    private void loadCompt() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (new SpUtils(LoadFilesActivity.this).isLogin()){
                    startActivity(new Intent(LoadFilesActivity.this, MainActivity.class));
                    finish();
                }else {
                    startActivity(new Intent(LoadFilesActivity.this, LoginActivity.class));
                }
            }
        });
    }

    //获取文件夹信息
    private void getFolders() {
        FileUtils fileUtils = FileUtils.getInstance();
        List<Map<String, Object>> fileList = fileUtils.getSonNode(DefConfig.GEN_PATH);
        if (fileList == null || fileList.isEmpty()) return;
        for (int i = 0; i < fileList.size(); i++) {
            Map<String, Object> stringObjectMap = fileList.get(i);
            boolean isDir = (boolean) stringObjectMap.get(FileUtils.FILE_INFO_ISFOLDER);
            if (isDir) {
                String fileName = (String) stringObjectMap.get(FileUtils.FILE_INFO_NAME);
                String filePath = (String) stringObjectMap.get(FileUtils.FILE_INFO_PATH);
                List<FolderBean> folderBeans = FolderBean.find(FolderBean.class, DatabaseConfig.searchFolderSql, fileName);
                if (folderBeans == null || folderBeans.isEmpty()) {
                    FolderBean folderBean = new FolderBean(fileName, filePath, i);
                    folderBean.save();
                } else {
                    FolderBean.deleteAll(FolderBean.class);
                    getFolders();
                }
            }
        }
    }
}
