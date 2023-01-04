package com.xhx.bookread.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    public static final String DATE_TYPE="yyyy-MM-dd";
    public static String getCurrentDate(){
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_TYPE); //设置时间格式
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08")); //设置时区
        Date curDate = new Date(System.currentTimeMillis()); //获取当前时间
        String createDate = formatter.format(curDate);   //格式转换
        return createDate;
    }
}
