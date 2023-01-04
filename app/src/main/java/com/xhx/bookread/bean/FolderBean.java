package com.xhx.bookread.bean;

import com.orm.SugarRecord;
import com.orm.dsl.Column;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;
import com.xhx.bookread.configs.DatabaseConfig;

import java.io.Serializable;
@Table(name = DatabaseConfig.FOLDER_TAB_NAME)
public class FolderBean extends SugarRecord implements Serializable {
    @Unique
    private long longId;
    @Column(name = DatabaseConfig.COLUMN_FOLDER_NAME)
    String folderName;
    String filePath;
    @Column(name = DatabaseConfig.COLUMN_FOLDER_INDEX)
    int folderIndex;//分类目录ID
    public FolderBean() {
    }
    public FolderBean(String folderName, String filePath, int folderIndex) {
        this.folderName = folderName;
        this.filePath = filePath;
        this.folderIndex = folderIndex;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
