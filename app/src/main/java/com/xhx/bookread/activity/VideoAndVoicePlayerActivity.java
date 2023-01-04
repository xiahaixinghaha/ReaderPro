package com.xhx.bookread.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.pdfsearch.R;
import com.xhx.bookread.bean.FilesBean;
import com.xhx.bookread.util.PublicUtil;

public class VideoAndVoicePlayerActivity extends MBaseActivity {
    String TAG = "VideoAndVoicePlayerActivity";
    private static final String KEY_FILES_BEAN = "files_bean";
    RelativeLayout rlHeader;
    PlayerView playerView;
    ExoPlayer player;

    public static void startVVPlayerActivity(Context context, FilesBean filesBean) {
        Intent intent = new Intent(context, VideoAndVoicePlayerActivity.class);
        intent.putExtra(KEY_FILES_BEAN, filesBean);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FilesBean filesBean = (FilesBean) getIntent().getSerializableExtra(KEY_FILES_BEAN);
        if (filesBean == null) {
            showShortToast("文件参数不存在！");
            return;
        }
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(filesBean.getFileName());
        rlHeader = findViewById(R.id.rlHeader);
        rlHeader.setVisibility(View.GONE);

        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setMedieoPlayer(filesBean.getFilePath());

    }

    private void setMedieoPlayer(String path) {
        playerView = findViewById(R.id.player_view);

        playerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rlHeader.getVisibility() == View.VISIBLE) {
                    rlHeader.setVisibility(View.GONE);
                    playerView.hideController();
                } else {
                    rlHeader.setVisibility(View.VISIBLE);
                    playerView.showController();
                }
            }
        });
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setShowSubtitleButton(true);
        Uri uri = PublicUtil.pathToUri(path);
        //填充媒体数据
        player.addMediaItem(MediaItem.fromUri(uri));
        //准备播放
        player.prepare();
        //准备完成就开始播放
        player.setPlayWhenReady(true);

    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_v_and_v_player;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        playerView.onPause();
        player.pause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        player.stop();
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        player.release();
        playerView.destroyDrawingCache();
        super.onDestroy();
    }
}
