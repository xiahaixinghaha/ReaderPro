/**
 * Copyright 2016 JustWayward Team
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xhx.bookread.view.pdfview;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.view.MotionEvent;
import android.view.View;

public class PDFViewPager extends ViewPager {

    protected Context context;
    OnPageClickListener mOnPageClickListener;
    public PDFViewPager(Context context, String pdfPath,OnPageClickListener pageClickListener) {
        super(context);
        this.context = context;
        this.mOnPageClickListener=pageClickListener;
        init(pdfPath);
    }

    protected void init(String pdfPath) {
        setClickable(true);
        initAdapter(context, pdfPath);
    }

    public void initAdapter(Context context, String pdfPath) {
        setAdapter(new PDFPagerAdapter.Builder(context)
                .setPdfPath(pdfPath)
                .setOffScreenSize(getOffscreenPageLimit())
                .setOnPageClickListener(mOnPageClickListener)
                .create());
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}
