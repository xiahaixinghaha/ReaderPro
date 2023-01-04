package com.xhx.bookread.bean;

import com.orm.SugarRecord;
import com.orm.dsl.Column;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;
import com.xhx.bookread.configs.DatabaseConfig;

import java.io.Serializable;

@Table(name = DatabaseConfig.FILES_TAB_NAME_TEXT)
public class FilesBeanText extends SugarRecord implements Serializable {
    @Unique
    @Column(name = DatabaseConfig.COLUMN_FILE_LONGID)
    private long longId;//
    @Column(name = DatabaseConfig.COLUMN_FILE_PATH)
    String filePath;//
    @Column(name = DatabaseConfig.COLUMN_FILE_NAME)
    String fileName;
    @Column(name = DatabaseConfig.COLUMN_FILE_TYPE)
    String fileType;
    @Column(name = DatabaseConfig.COLUMN_FILE_DATE)
    String addDate;
    @Column(name = DatabaseConfig.COLUMN_FILE_CLASSIFY)
    int classify;//文件类型：1，文字 2，
    @Column(name = DatabaseConfig.COLUMN_FOLDER_INDEX)
    int folderIndex;//分类目录ID




    public FilesBeanText(){

    }
    public FilesBeanText(String filePath, String fileName, String fileType, String addDate, int classify, int folderIndex) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileType = fileType;
        this.addDate = addDate;
        this.classify = classify;
        this.folderIndex = folderIndex;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getAddDate() {
        return addDate;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public int getClassify() {
        return classify;
    }

    public void setClassify(int classify) {
        this.classify = classify;
    }

    public int getFolderIndex() {
        return folderIndex;
    }

    public void setFolderIndex(int folderIndex) {
        this.folderIndex = folderIndex;
    }

    @Override
    public long save() {
        longId = super.save();
        setLongId(longId);
        return longId;
    }

    public void setLongId(long longId) {
        this.longId = longId;
        update();
    }

    public long getLongId() {
        return longId;
    }
}
