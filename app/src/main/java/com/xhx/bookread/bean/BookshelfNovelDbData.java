package com.xhx.bookread.bean;

import com.orm.SugarRecord;
import com.orm.dsl.Column;
import com.orm.dsl.Table;
import com.orm.dsl.Unique;
import com.xhx.bookread.configs.DatabaseConfig;

/**
 * @author Feng Zhaohao
 * Created on 2019/11/26
 */
@Table(name = DatabaseConfig.EPUB_TAB_NAME)
public class BookshelfNovelDbData extends SugarRecord {
    @Unique
    @Column(name = DatabaseConfig.COLUMN_novelUrl)
    private String novelUrl;
    private String name;
    private String cover;
    private int chapterIndex;
    private int position;
    private int type;
    private int secondPosition;
    public BookshelfNovelDbData(){}
    public BookshelfNovelDbData(String novelUrl, String name, String cover,
                                int chapterIndex, int position, int type) {
        this(novelUrl, name, cover, chapterIndex, position, type, 0);
    }

    public BookshelfNovelDbData(String novelUrl, String name, String cover, int chapterIndex,
                                int position, int type, int secondPosition) {
        this.novelUrl = novelUrl;
        this.name = name;
        this.cover = cover;
        this.chapterIndex = chapterIndex;
        this.position = position;
        this.type = type;
        this.secondPosition = secondPosition;
    }

    public String getNovelUrl() {
        return novelUrl;
    }

    public void setNovelUrl(String novelUrl) {
        this.novelUrl = novelUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSecondPosition() {
        return secondPosition;
    }

    public void setSecondPosition(int secondPosition) {
        this.secondPosition = secondPosition;
    }

    @Override
    public String toString() {
        return "BookshelfNovelDbData{" +
                "novelUrl='" + novelUrl + '\'' +
                ", name='" + name + '\'' +
                ", cover='" + cover + '\'' +
                ", chapterIndex=" + chapterIndex +
                ", position=" + position +
                ", type=" + type +
                ", secondPosition=" + secondPosition +
                '}';
    }
}
