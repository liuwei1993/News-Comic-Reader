package com.song.sunset.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.song.sunset.R;

/**
 * Created by Song on 2016/8/27 0027.
 * Email:z53520@qq.com
 */
public class ComicListViewHolder extends RecyclerView.ViewHolder {

    public SimpleDraweeView simpleDraweeView;
    public TextView comicName;
    public LinearLayout comicListLayout;

    public ComicListViewHolder(View itemview) {
        super(itemview);
        comicListLayout = (LinearLayout) itemview.findViewById(R.id.id_comic_list_item);
        simpleDraweeView = (SimpleDraweeView) itemview.findViewById(R.id.id_comic_list_simple_drawee_view);
        comicName = (TextView) itemview.findViewById(R.id.id_comic_list_simple_comic_name);
    }
}
