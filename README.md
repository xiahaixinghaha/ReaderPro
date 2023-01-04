# PDFSearch

#### 介绍
Android实现手机内PDF文件搜索
实现的效果图如下:
手机内PDF文件搜索:

![输入图片说明](https://images.gitee.com/uploads/images/2019/1009/175450_8a08dc8a_510566.png "20190923190017406.png")
PDF预览:
![输入图片说明](https://images.gitee.com/uploads/images/2019/1009/175343_60592bb6_510566.png "20190928181648378.png")

#### 软件架构
软件架构说明
Android Studio 3.5.0 


#### 安装教程

1. xxxx
2. xxxx
3. xxxx

#### 使用说明

1. 配置APP目录下的build.gradle
![输入图片说明](https://images.gitee.com/uploads/images/2019/1009/182032_1e093305_510566.png "屏幕截图.png")




2. so文件
    ![输入图片说明](https://images.gitee.com/uploads/images/2019/1009/175856_661ae94f_510566.png "屏幕截图.png")

3. 腾讯x5浏览器初始化
    public class App extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        initX5Web();
    }

    private void initX5Web() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。

        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(),  cb);
    }
}


4. Manifest权限配置
![输入图片说明](https://images.gitee.com/uploads/images/2019/1009/182352_0e6ab5e9_510566.png "屏幕截图.png")


5. 搜索手机内文件前动态请求权限：
![输入图片说明](https://images.gitee.com/uploads/images/2019/1009/182236_68dbc717_510566.png "屏幕截图.png")
    
#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request


#### 码云特技

1. 我的博客地址：https://blog.csdn.net/u012556114
2. 我的码云地址:https://gitee.com/jackning_admin