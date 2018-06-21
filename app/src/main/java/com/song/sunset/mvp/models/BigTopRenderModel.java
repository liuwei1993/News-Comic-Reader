package com.song.sunset.mvp.models;

import android.support.v7.widget.RecyclerView;

import com.song.sunset.beans.PhoenixChannelBean;
import com.song.sunset.holders.BigTopViewHolder;
import com.song.sunset.mvp.models.base.PhoenixBaseRenderModel;

/**
 * Created by Song on 2017/4/13 0013.
 * E-mail: z53520@qq.com
 */

public class BigTopRenderModel extends PhoenixBaseRenderModel {

    @Override
    public void render(RecyclerView.ViewHolder holder, PhoenixChannelBean phoenixChannelBean) {
        BigTopViewHolder viewHolder = (BigTopViewHolder) holder;
        viewHolder.title.setText(phoenixChannelBean.getTitle());
    }
}
