package com.xhx.bookread.util;

import android.Manifest;
import android.os.Build;

import androidx.annotation.NonNull;


import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ForwardScope;
import com.xhx.bookread.activity.MBaseActivity;

import java.util.List;

public class PermissionUtils {

    public interface RequestCallBack {
        void callBack(boolean isHave);
    }

    public static void requestStoragePermission(MBaseActivity activity, RequestCallBack callBack) {
        String[] p = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ?
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                : new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        PermissionX.init(activity)
                .permissions(p)
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(@NonNull ForwardScope scope, @NonNull List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "请在设置中手动开启以下权限", "允许", "取消");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                        if (!allGranted) {
                            StringBuilder s = new StringBuilder();
                            for (String s1 : deniedList) {
                                s.append(s1);
                            }
                            activity.showShortToast("以下权限被拒绝" + s);
                        }
                        callBack.callBack(allGranted);
                    }
                });

    }

}
