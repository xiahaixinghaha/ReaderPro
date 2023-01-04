package com.xhx.bookread.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.xhx.bookread.bean.FilesBean;

import java.util.ArrayList;
import java.util.List;

public class SpUtils {
    private final String SP_BOOK_DATA = "book_data";
    private final String KEY_TJ_BOOK = "key_tj_book";
    private final String KEY_IS_LOGIN = "key_is_login";
    SharedPreferences sharedPreferences;

    public SpUtils(Context context) {
        sharedPreferences = context.getSharedPreferences(SP_BOOK_DATA, Context.MODE_PRIVATE);
    }

    public void putTJBooks(List<FilesBean> books) {
        Gson gson = new Gson();
        String s = gson.toJson(books);
        sharedPreferences.edit().putString(KEY_TJ_BOOK, s).apply();
    }

    public List<FilesBean> getTJBooks() {
        String s = sharedPreferences.getString(KEY_TJ_BOOK, "");
        List<FilesBean> list = new ArrayList<>();
        if (!TextUtils.isEmpty(s)) {
            Gson gson = new Gson();
            List<FilesBean> filesBeans = gson.fromJson(s, new TypeToken<List<FilesBean>>() {
            }.getType());
            if (filesBeans != null)
                list.addAll(filesBeans);
        }
        return list;
    }
    public boolean isLogin(){
       return sharedPreferences.getBoolean(KEY_IS_LOGIN,false);
    }
    public void putLogin(boolean isLogin){
        sharedPreferences.edit().putBoolean(KEY_IS_LOGIN,isLogin).apply();
    }
}
