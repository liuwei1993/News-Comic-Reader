package com.song.sunset.holders;

import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.song.sunset.R;

/**
 * Created by Song on 2017/3/31 0031.
 * E-mail: z53520@qq.com
 */
public class TitleImageViewHolder extends PhoenixBaseBottomViewHolder {

    public SimpleDraweeView image;

    public TitleImageViewHolder(View inflate) {
        super(inflate);
        image = (SimpleDraweeView) inflate.findViewById(R.id.phoenix_image);
    }
}
