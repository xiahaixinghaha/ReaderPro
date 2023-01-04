package com.xhx.bookread.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.xhx.bookread.configs.Constant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 用于获取手机的文件夹及文件的工具类，如果权限允许，可以获取手机上任意路径的文件列表
 * GetFilesUtils使用的是懒汉式单例模式，线程安全
 *
 * @author wuwang
 * @since 2014.11
 */
public class FileUtils {
    private static final String TAG="FileUtils";
    public static final String FILE_TYPE_FOLDER = "wFl2d";

    public static final String FILE_INFO_NAME = "fName";
    public static final String FILE_INFO_ISFOLDER = "fIsDir";
    public static final String FILE_INFO_TYPE = "fFileType";
    public static final String FILE_INFO_NUM_SONDIRS = "fSonDirs";
    public static final String FILE_INFO_NUM_SONFILES = "fSonFiles";
    public static final String FILE_INFO_PATH = "fPath";

    private static FileUtils gfu;

    private FileUtils() {

    }

    public static boolean checkOPFInRootDirectory(String unzipDir) {
        String mPathOPF = "";
        boolean status = false;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(unzipDir
                    + "/META-INF/container.xml"), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("full-path")) {
                    int start = line.indexOf("full-path");
                    int start2 = line.indexOf('\"', start);
                    int stop2 = line.indexOf('\"', start2 + 1);
                    if (start2 > -1 && stop2 > start2) {
                        mPathOPF = line.substring(start2 + 1, stop2).trim();
                        break;
                    }
                }
            }
            br.close();

            if (!mPathOPF.contains("/")) {
                status = true;
            } else {
                status = false;
            }
        } catch (NullPointerException | IOException e) {
            Log.e(TAG,e.toString());
        }
        return status;
    }
    public static String getEpubFolderPath(String epubFileName) {
        return Constant.PATH_EPUB + "/" + epubFileName;
    }
    public static String getPathOPF(String unzipDir) {
        String mPathOPF = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(unzipDir
                    + "/META-INF/container.xml"), "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("full-path")) {
                    int start = line.indexOf("full-path");
                    int start2 = line.indexOf('\"', start);
                    int stop2 = line.indexOf('\"', start2 + 1);
                    if (start2 > -1 && stop2 > start2) {
                        mPathOPF = line.substring(start2 + 1, stop2).trim();
                        break;
                    }
                }
            }
            br.close();

            if (!mPathOPF.contains("/")) {
                return null;
            }

            int last = mPathOPF.lastIndexOf('/');
            if (last > -1) {
                mPathOPF = mPathOPF.substring(0, last);
            }

            return mPathOPF;
        } catch (NullPointerException | IOException e) {
            Log.e(TAG,e.toString());
        }
        return mPathOPF;
    }
    public static void unzipFile(String inputZip, String destinationDirectory) throws IOException {

        int buffer = 2048;
        List<String> zipFiles = new ArrayList<>();
        File sourceZipFile = new File(inputZip);
        File unzipDirectory = new File(destinationDirectory);

        createDir(unzipDirectory.getAbsolutePath());

        ZipFile zipFile;
        zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
        Enumeration zipFileEntries = zipFile.entries();

        while (zipFileEntries.hasMoreElements()) {

            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(unzipDirectory, currentEntry);

            if (currentEntry.endsWith(FileTypeUtil.TYPE_ZIP)) {
                zipFiles.add(destFile.getAbsolutePath());
            }

            File destinationParent = destFile.getParentFile();
            createDir(destinationParent.getAbsolutePath());

            if (!entry.isDirectory()) {

                if (destFile != null && destFile.exists()) {
                    Log.i(TAG,destFile + "已存在");
                    continue;
                }

                BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                int currentByte;
                // buffer for writing file
                byte[] data = new byte[buffer];

                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, buffer);

                while ((currentByte = is.read(data, 0, buffer)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
        zipFile.close();

        for (Iterator iter = zipFiles.iterator(); iter.hasNext(); ) {
            String zipName = (String) iter.next();
            unzipFile(zipName, destinationDirectory + File.separatorChar
                    + zipName.substring(0, zipName.lastIndexOf(FileTypeUtil.TYPE_ZIP)));
        }
    }

    /**
     * 创建根缓存目录
     *
     * @return
     */
    public static String createRootPath(Context context) {
        String cacheRootPath = "";
        if (isSdCardAvailable()) {
            // /sdcard/Android/data/<application package>/cache
            cacheRootPath = context.getExternalCacheDir().getPath();
        } else {
            // /data/data/<application package>/cache
            cacheRootPath = context.getCacheDir().getPath();
        }
        return cacheRootPath;
    }
    public static File getChapterFile(String bookId, int chapter) {
        File file = new File(getChapterPath(bookId, chapter));
        if (!file.exists())
            createFile(file);
        return file;
    }
    public static boolean isSdCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
    public static String getChapterPath(String bookId, int chapter) {
        return Constant.PATH_TXT + bookId + File.separator + chapter + ".txt";
    }
    /**
     * 递归创建文件夹
     *
     * @param file
     * @return 创建失败返回""
     */
    public static String createFile(File file) {
        try {
            if (file.getParentFile().exists()) {
                Log.i(TAG,"----- 创建文件" + file.getAbsolutePath());
                file.createNewFile();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());
                file.createNewFile();
                Log.i(TAG,"----- 创建文件" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 递归创建文件夹
     *
     * @param dirPath
     * @return 创建失败返回""
     */
    public static String createDir(String dirPath) {
        try {
            File file = new File(dirPath);
            if (file.getParentFile().exists()) {
                Log.i(TAG,"----- 创建文件夹" + file.getAbsolutePath());
                file.mkdir();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());
                Log.i(TAG,"----- 创建文件夹" + file.getAbsolutePath());
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dirPath;
    }
    /**
     * 获取GetFilesUtils实例
     *
     * @return GetFilesUtils
     **/
    public static synchronized FileUtils getInstance() {
        if (gfu == null) {
            gfu = new FileUtils();
        }
        return gfu;
    }

    /**
     * 获取文件path文件夹下的文件列表
     *
     * @param path 手机上的文件夹
     * @return path文件夹下的文件列表的信息，信息存储在Map中，Map的key的列表如下：<br />
     * FILE_INFO_NAME : String 文件名称<br />
     * FILE_INFO_ISFOLDER: boolean 是否为文件夹<br />
     * FILE_INFO_TYPE: string 文件的后缀<br />
     * FILE_INFO_NUM_SONDIRS : int 子文件夹个数<br />
     * FILE_INFO_NUM_SONFILES: int 子文件个数<br />
     * FILE_INFO_PATH : String 文件的绝对路径<br />
     * @see #getSonNode(String)
     **/
    public List<Map<String, Object>> getSonNode(File path) {
        if (path.isDirectory()) {
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            File[] files = path.listFiles();
            if (files != null) {

                for (int i = 0; i < files.length; i++) {
                    Map<String, Object> fileInfo = new HashMap<String, Object>();
                    fileInfo.put(FILE_INFO_NAME, files[i].getName());
                    if (files[i].isDirectory()) {
                        fileInfo.put(FILE_INFO_ISFOLDER, true);
                        File[] bFiles = files[i].listFiles();
                        if (bFiles == null) {
                            fileInfo.put(FILE_INFO_NUM_SONDIRS, 0);
                            fileInfo.put(FILE_INFO_NUM_SONFILES, 0);
                        } else {
                            int getNumOfDir = 0;
                            for (int j = 0; j < bFiles.length; j++) {
                                if (bFiles[j].isDirectory()) {
                                    getNumOfDir++;
                                }
                            }
                            fileInfo.put(FILE_INFO_NUM_SONDIRS, getNumOfDir);
                            fileInfo.put(FILE_INFO_NUM_SONFILES, bFiles.length - getNumOfDir);
                        }
                        fileInfo.put(FILE_INFO_TYPE, FILE_TYPE_FOLDER);
                    } else {
                        fileInfo.put(FILE_INFO_ISFOLDER, false);
                        fileInfo.put(FILE_INFO_NUM_SONDIRS, 0);
                        fileInfo.put(FILE_INFO_NUM_SONFILES, 0);
                        fileInfo.put(FILE_INFO_TYPE, getFileType(files[i].getName()));
                    }
                    fileInfo.put(FILE_INFO_PATH, files[i].getAbsolutePath());
                    list.add(fileInfo);
                }
                return list;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * 获取文件pathStr文件夹下的文件列表
     *
     * @param pathStr 手机上的文件夹的绝对路径
     * @return pathStr文件夹下的文件列表的信息，信息存储在Map中，Map的key的列表如下：<br />
     * FILE_INFO_NAME : String 文件名称<br />
     * FILE_INFO_ISFOLDER: boolean 是否为文件夹<br />
     * FILE_INFO_TYPE: string 文件的后缀<br />
     * FILE_INFO_NUM_SONDIRS : int 子文件夹个数<br />
     * FILE_INFO_NUM_SONFILES: int 子文件个数<br />
     * FILE_INFO_PATH : String 文件的绝对路径<br />
     * @see #getSonNode(File)
     **/
    public List<Map<String, Object>> getSonNode(String pathStr) {
        File path = new File(pathStr);
        return getSonNode(path);
    }

    /**
     * 获取文件path文件或文件夹的兄弟节点文件列表
     *
     * @param path 手机上的文件夹
     * @return path文件夹下的文件列表的信息，信息存储在Map中，Map的key的列表如下：<br />
     * FILE_INFO_NAME : String 文件名称<br />
     * FILE_INFO_ISFOLDER: boolean 是否为文件夹<br />
     * FILE_INFO_TYPE: string 文件的后缀<br />
     * FILE_INFO_NUM_SONDIRS : int 子文件夹个数<br />
     * FILE_INFO_NUM_SONFILES: int 子文件个数<br />
     * FILE_INFO_PATH : String 文件的绝对路径<br />
     * @see #getBrotherNode(String)
     **/
    public List<Map<String, Object>> getBrotherNode(File path) {
        if (path.getParentFile() != null) {
            return getSonNode(path.getParentFile());
        } else {
            return null;
        }
    }

    /**
     * 获取文件path文件或文件夹的兄弟节点文件列表
     *
     * @return path文件夹下的文件列表的信息，信息存储在Map中，Map的key的列表如下：<br />
     * FILE_INFO_NAME : String 文件名称<br />
     * FILE_INFO_ISFOLDER: boolean 是否为文件夹<br />
     * FILE_INFO_TYPE: string 文件的后缀<br />
     * FILE_INFO_NUM_SONDIRS : int 子文件夹个数<br />
     * FILE_INFO_NUM_SONFILES: int 子文件个数<br />
     * FILE_INFO_PATH : String 文件的绝对路径<br />
     * @see #getBrotherNode(File)
     **/
    public List<Map<String, Object>> getBrotherNode(String pathStr) {
        File path = new File(pathStr);
        return getBrotherNode(path);
    }

    /**
     * 获取文件或文件夹的父路径
     *
     * @return String path的父路径
     **/
    public String getParentPath(File path) {
        if (path.getParentFile() == null) {
            return null;
        } else {
            return path.getParent();
        }
    }

    /**
     * 获取文件或文件的父路径
     *
     * @return String pathStr的父路径
     **/
    public String getParentPath(String pathStr) {
        File path = new File(pathStr);
        if (path.getParentFile() == null) {
            return null;
        } else {
            return path.getParent();
        }
    }

    /**
     * 获取sd卡的绝对路径
     *
     * @return String 如果sd卡存在，返回sd卡的绝对路径，否则返回null
     **/
    public String getSDPath() {
        String sdcard = Environment.getExternalStorageState();
        if (sdcard.equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            return null;
        }
    }

    /**
     * 获取一个基本的路径，一般应用创建存放应用数据可以用到
     *
     * @return String 如果SD卡存在，返回SD卡的绝对路径，如果SD卡不存在，返回Android数据目录的绝对路径
     **/
    public String getBasePath() {
        String basePath = getSDPath();
        if (basePath == null) {
            return Environment.getDataDirectory().getAbsolutePath();
        } else {
            return basePath;
        }
    }

    /**
     * 获取文件path的大小
     *
     * @return String path的大小
     **/
    public String getFileSize(File path) throws IOException {
        if (path.exists()) {
            DecimalFormat df = new DecimalFormat("#.00");
            String sizeStr = "";
            FileInputStream fis = new FileInputStream(path);
            long size = fis.available();
            fis.close();
            if (size < 1024) {
                sizeStr = size + "B";
            } else if (size < 1048576) {
                sizeStr = df.format(size / (double) 1024) + "KB";
            } else if (size < 1073741824) {
                sizeStr = df.format(size / (double) 1048576) + "MB";
            } else {
                sizeStr = df.format(size / (double) 1073741824) + "GB";
            }
            return sizeStr;
        } else {
            return null;
        }
    }

    /**
     * 获取文件fpath的大小
     *
     * @return String path的大小
     **/
    public String getFileSize(String fpath) {
        File path = new File(fpath);
        if (path.exists()) {
            DecimalFormat df = new DecimalFormat("#.00");
            String sizeStr = "";
            long size = 0;
            try {
                FileInputStream fis = new FileInputStream(path);
                size = fis.available();
                fis.close();
            } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
                return "未知大小";
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
                return "未知大小";
            }
            if (size < 1024) {
                sizeStr = size + "B";
            } else if (size < 1048576) {
                sizeStr = df.format(size / (double) 1024) + "KB";
            } else if (size < 1073741824) {
                sizeStr = df.format(size / (double) 1048576) + "MB";
            } else {
                sizeStr = df.format(size / (double) 1073741824) + "GB";
            }
            return sizeStr;
        } else {
            return "未知大小";
        }
    }

    /**
     * 根据后缀获取文件fileName的类型
     *
     * @return String 文件的类型
     **/
    public String getFileType(String fileName) {
        if (fileName != "" && fileName.length() > 3) {
            int dot = fileName.lastIndexOf(".");
            if (dot > 0) {
                return fileName.substring(dot + 1);
            } else {
                return "";
            }
        }
        return "";
    }

    public Comparator<Map<String, Object>> defaultOrder() {

        final String orderBy0 = FILE_INFO_ISFOLDER;
        final String orderBy1 = FILE_INFO_TYPE;
        final String orderBy2 = FILE_INFO_NAME;

        Comparator<Map<String, Object>> order = new Comparator<Map<String, Object>>() {

            @Override
            public int compare(Map<String, Object> lhs, Map<String, Object> rhs) {
// TODO Auto-generated method stub
                int left0 = lhs.get(orderBy0).equals(true) ? 0 : 1;
                int right0 = rhs.get(orderBy0).equals(true) ? 0 : 1;
                if (left0 == right0) {
                    String left1 = lhs.get(orderBy1).toString();
                    String right1 = rhs.get(orderBy1).toString();
                    if (left1.compareTo(right1) == 0) {
                        String left2 = lhs.get(orderBy2).toString();
                        String right2 = rhs.get(orderBy2).toString();
                        return left2.compareTo(right2);
                    } else {
                        return left1.compareTo(right1);
                    }
                } else {
                    return left0 - right0;
                }
            }
        };

        return order;
    }

    /**
     * @param stringObjectMap
     * @return
     */
    public boolean isDir(Map<String, Object> stringObjectMap) {
        boolean isDir = (boolean) stringObjectMap.get(FileUtils.FILE_INFO_ISFOLDER);
        return isDir;
    }

    public String getFileName(Map<String, Object> stringObjectMap) {
        String fileName = (String) stringObjectMap.get(FileUtils.FILE_INFO_NAME);
        return fileName;
    }

    public String getFilePath(Map<String, Object> stringObjectMap) {
        String filePath = (String) stringObjectMap.get(FileUtils.FILE_INFO_PATH);
        return filePath;
    }

    public String getFileType(Map<String, Object> stringObjectMap) {
        String fileType = (String) stringObjectMap.get(FileUtils.FILE_INFO_TYPE);
        return fileType;
    }

    public static boolean copyFile(String oldPathName, String newPathName) {
        try {
            File oldFile = new File(oldPathName);
            if (!oldFile.exists()) {
                Log.e("copyFile", "copyFile:  oldFile not exist.");
                return false;
            } else if (!oldFile.isFile()) {
                Log.e("copyFile", "copyFile:  oldFile not file.");
                return false;
            } else if (!oldFile.canRead()) {
                Log.e("copyFile", "copyFile:  oldFile cannot read.");
                return false;
            }
            FileInputStream fileInputStream = new FileInputStream(oldPathName);
            FileOutputStream fileOutputStream = new FileOutputStream(newPathName);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getCharset(String fileName) {
        BufferedInputStream bis = null;
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            bis = new BufferedInputStream(new FileInputStream(fileName));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.mark(0);
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return charset;
    }
    /**
     * 文件拷贝
     *
     * @param src  源文件
     * @param desc 目的文件
     */
    public static void fileChannelCopy(File src, File desc) {
        //createFile(src);
        createFile(desc);
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(src);
            fo = new FileOutputStream(desc);
            FileChannel in = fi.getChannel();//得到对应的文件通道
            FileChannel out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fo != null) fo.close();
                if (fi != null) fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取Wifi传书保存文件
     *
     * @param fileName
     * @return
     */
    public static File createWifiTranfesFile(String fileName) {
        LogUtils.i("wifi trans save " + fileName);
        // 取文件名作为文件夹（bookid）
        String absPath = Constant.PATH_TXT + "/" + fileName + "/1.txt";

        File file = new File(absPath);
        if (!file.exists())
            createFile(file);
        return file;
    }
    /**
     * 删除指定文件，如果是文件夹，则递归删除
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static boolean deleteFileOrDirectory(File file) throws IOException {
        try {
            if (file != null && file.isFile()) {
                return file.delete();
            }
            if (file != null && file.isDirectory()) {
                File[] childFiles = file.listFiles();
                // 删除空文件夹
                if (childFiles == null || childFiles.length == 0) {
                    return file.delete();
                }
                // 递归删除文件夹下的子文件
                for (int i = 0; i < childFiles.length; i++) {
                    deleteFileOrDirectory(childFiles[i]);
                }
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static File getBookDir(String bookId) {
        return new File(Constant.PATH_TXT + bookId);
    }
    /**
     * 将内容写入文件
     *
     * @param filePath eg:/mnt/sdcard/demo.txt
     * @param content  内容
     * @param isAppend 是否追加
     */
    public static void writeFile(String filePath, String content, boolean isAppend) {
        LogUtils.i("save:" + filePath);
        try {
            FileOutputStream fout = new FileOutputStream(filePath, isAppend);
            byte[] bytes = content.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取文件夹大小
     *
     * @return
     * @throws Exception
     */
    public static long getFolderSize(String dir) throws Exception {
        File file = new File(dir);
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // 如果下面还有文件
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i].getAbsolutePath());
                } else {
                    size = size + fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
    /**
     * 转换文件大小
     *
     * @param fileLen 单位B
     * @return
     */
    public static String formatFileSizeToString(long fileLen) {
        DecimalFormat df = new DecimalFormat("0.00");
        String fileSizeString = "";
        if (fileLen < 1024) {
            fileSizeString = df.format((double) fileLen) + "B";
        } else if (fileLen < 1048576) {
            fileSizeString = df.format((double) fileLen / 1024) + "K";
        } else if (fileLen < 1073741824) {
            fileSizeString = df.format((double) fileLen / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileLen / 1073741824) + "G";
        }
        return fileSizeString;
    }
}
