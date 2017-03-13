package com.song.sunset.holders;

import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.song.sunset.R;

/**
 * Created by songmw3 on 2017/3/13.
 * E-mail:z53520@qq.com
 */
public class VideoListHelperHolder extends BaseViewHolder {

    public SimpleDraweeView cover;
    public TextView videoName;

    public VideoListHelperHolder(View view) {
        super(view);
        cover = (SimpleDraweeView) itemView.findViewById(R.id.video_drawee);
        videoName = (TextView) itemView.findViewById(R.id.video_text);
    }
}
