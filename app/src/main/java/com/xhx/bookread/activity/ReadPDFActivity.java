/**
 * Copyright 2016 JustWayward Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xhx.bookread.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.example.pdfsearch.R;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.view.pdfview.OnPageClickListener;
import com.xhx.bookread.view.pdfview.PDFViewPager;

import java.io.File;


public class ReadPDFActivity extends MBaseActivity {

    public static void start(Context context, FilesBean filesBean) {
        Intent intent = new Intent(context, ReadPDFActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra("filesBean", filesBean);
        intent.setData(Uri.fromFile(new File(filesBean.getFilePath())));
        context.startActivity(intent);
    }
    private OnPageClickListener clickListener = new OnPageClickListener() {

        @Override
        public void onPageTap(View view, float x, float y) {

            int item = pdfViewPager.getCurrentItem();
            int total = pdfViewPager.getChildCount();

            if (x < 0.33f && item > 0) {
                item -= 1;
                pdfViewPager.setCurrentItem(item);
            } else if (x >= 0.67f && item < total - 1) {
                item += 1;
                pdfViewPager.setCurrentItem(item);
            }
        }
    };
    PDFViewPager pdfViewPager;

    public void initDatas() {
        FilesBean filesBean = (FilesBean) getIntent().getSerializableExtra("filesBean");
        setTitle(filesBean.getFileName());
        LinearLayout llPdfRoot = findViewById(R.id.llPdfRoot);
        String filePath = Uri.decode(getIntent().getDataString().replace("file://", ""));
        pdfViewPager = new PDFViewPager(this, filePath,clickListener);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            llPdfRoot.addView(pdfViewPager);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_read_pdf;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        int cItem = 0;
        if (pdfViewPager != null) {
            cItem = pdfViewPager.getCurrentItem();
        }
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_read_pdf);
        initToolBar();
        initDatas();
        pdfViewPager.setOffscreenPageLimit(cItem+1);
        pdfViewPager.setCurrentItem(cItem);
    }
}
