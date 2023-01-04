package com.xhx.bookread.util;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2018/2/21 0021.
 */

public class CopyFileUtil {

    private static final String TAG = "CopyFileUtil";

    private static File sourceFile;
    private static File targetFile;
    private static String fileName;

    public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
        try (FileChannel inputChannel = new FileInputStream(source).getChannel();
             FileChannel outputChannel = new FileOutputStream(dest).getChannel()) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        }
    }

    public static void simpleCopy(final Context context, List<File> fileList, final String dest) {
        for (int i = 0; i < fileList.size(); i++) {
            sourceFile = fileList.get(i);
            //第一步先判断目标路径是否存在同名文件/文件夹
            fileName = sourceFile.getName();
            targetFile = new File(dest + "/" + fileName);
            if (targetFile.exists()) {
                //存在同名文件/文件夹
                Log.d(TAG, "simpleCopy: 存在同名文件/文件夹" + fileName);
                //弹窗，让用户选择覆盖/重命名/跳过
                //只能在点击事件中完成后续操作，不能通过改变标志位来延续后事......
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("存在同名文件/文件夹");
                builder.setMessage("名字：" + targetFile.getName());
                builder.setPositiveButton("覆盖", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sameCopy(context);
                    }
                });
                builder.setNegativeButton("重命名", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final EditText editText = new EditText(context);
                        AlertDialog.Builder renameBuilder = new AlertDialog.Builder(context);
                        renameBuilder.setCancelable(false);
                        renameBuilder.setTitle("重命名：");
                        renameBuilder.setView(editText);
                        renameBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                fileName = editText.getText().toString();
                                targetFile = new File(dest + "/" + fileName);

                                sameCopy(context);
                            }
                        });
                        renameBuilder.show();
                    }
                });
                builder.setNeutralButton("跳过", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "onClick: 用户选择了跳过");
                        Toast.makeText(context, "onClick: 用户选择了跳过", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            } else {
                //不存在同名文件/文件夹
                //第二步 判断是文件还是文件夹
                sameCopy(context);
            }
        }
        Log.d(TAG, "simpleCopy: 复制完毕");
    }

    private static void sameCopy(Context context) {
        //第二步 判断是文件还是文件夹
        if (sourceFile.isDirectory()) {
            //文件夹,将文件夹的所有内容加入subFileList
            if (targetFile.exists()) {
                //如果文件夹存在，用户选择的是覆盖，就没必要重新创建文件夹
                List<File> subFileList = new ArrayList<>();
                Collections.addAll(subFileList, sourceFile.listFiles());
                simpleCopy(context, subFileList, targetFile.getAbsolutePath());
            } else {
                if (targetFile.mkdir()) {
                    List<File> subFileList = new ArrayList<>();
                    Collections.addAll(subFileList, sourceFile.listFiles());
                    simpleCopy(context, subFileList, targetFile.getAbsolutePath());
                } else {
                    Log.d(TAG, "simpleCopy: 创建文件夹失败！");
                    Log.d(TAG, "simpleCopy: 要么没有权限，要么目标路径有问题");
                }
            }
        } else {
            //文件 - - 复制文件是耗时操作，需要在外环开子线程处理
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        copyFileUsingFileChannels(sourceFile, targetFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
