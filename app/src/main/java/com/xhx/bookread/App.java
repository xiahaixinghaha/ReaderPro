package com.xhx.bookread;

import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.orm.SchemaGenerator;
import com.orm.SugarApp;
import com.orm.SugarContext;
import com.orm.SugarDb;
import com.xhx.bookread.bean.UserBean;
import com.xhx.bookread.configs.DefConfig;
import com.xhx.bookread.readview.AppComponent;
import com.xhx.bookread.readview.SharedPreferencesUtil;
import com.xhx.bookread.util.AppUtils;
import com.xhx.bookread.util.UserTxtFileUtils;

import java.io.File;
import java.io.IOException;

public class App extends SugarApp {
    public static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        DefConfig.GEN_PATH = createFolder();
        //数据库初始化
        SugarContext.init(this);
        AppUtils.init(this);
        SharedPreferencesUtil.init(getApplicationContext(), getPackageName() + "_preference", Context.MODE_MULTI_PROCESS);

    }


    @Override
    public void onTerminate() {
        SugarContext.terminate();
        super.onTerminate();
    }

    /**
     * 创建文件夹
     */
    private String createFolder() {
        String filesPath;
        filesPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myFile = new File(filesPath + "/阅读文件");
        if (!myFile.exists()) {
            myFile.mkdirs();
        }

        File userPwdFile = new File(filesPath + "/阅读文件/user.txt");
        if (!userPwdFile.exists()){
            try {
                userPwdFile.createNewFile();
                UserBean userBean=new UserBean("12345","admin");
                Gson gson=new Gson();
                String json=gson.toJson(userBean);
                UserTxtFileUtils.writeToTxt(userPwdFile.getPath(),json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        DefConfig.USER_FILE_PATH=userPwdFile.getPath();
        return myFile.getAbsolutePath();
    }



}
