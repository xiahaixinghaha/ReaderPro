package com.xhx.bookread.activity;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.pdfsearch.R;
import com.google.gson.Gson;
import com.xhx.bookread.bean.UserBean;
import com.xhx.bookread.configs.DefConfig;
import com.xhx.bookread.util.SpUtils;
import com.xhx.bookread.util.UserTxtFileUtils;

public class LoginActivity extends MBaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void configViews() {
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPwd = findViewById(R.id.etPwd);
        TextView tvBtnSubmit = findViewById(R.id.tvBtnSubmit);
        if (TextUtils.isEmpty(DefConfig.USER_FILE_PATH)) {
            showShortToast("配置错误");
            return;
        }
        String s = UserTxtFileUtils.readFromXML(DefConfig.USER_FILE_PATH);
        if (TextUtils.isEmpty(s)){
            showShortToast("配置错误");
            return;
        }
        Gson gson=new Gson();

        UserBean userBean= gson.fromJson(s,UserBean.class);
        if (userBean==null||TextUtils.isEmpty(userBean.getUserName())||TextUtils.isEmpty(userBean.getPwd())){
            showShortToast("配置错误");
            return;
        }
        tvBtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = etEmail.getText().toString();
                String pwd = etPwd.getText().toString();
                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pwd)) {
                    showShortToast("请输入用户名或密码！");
                    return;
                }
                if (TextUtils.equals(user, userBean.getUserName()) && TextUtils.equals(pwd, userBean.getPwd())) {
                    showShortToast("登录成功！");
                    new SpUtils(LoginActivity.this).putLogin(true);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    showShortToast("用户名或密码错误！");
                }
            }
        });


    }
}
