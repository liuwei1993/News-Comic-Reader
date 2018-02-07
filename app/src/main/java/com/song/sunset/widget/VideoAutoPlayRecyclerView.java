package com.song.sunset.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.song.sunset.utils.ViewUtil;
import com.song.video.NormalVideoPlayer;

/**
 * Created by Song on 2017/7/3 0003.
 * E-mail: z53520@qq.com
 */

public class VideoAutoPlayRecyclerView extends LoadMoreRecyclerView implements Runnable {

    private boolean changed = false;

    private int mCenterLine;

    private NormalVideoPlayer mPlayer;

    private int currentPlayerPosition = RecyclerView.NO_POSITION;

    private TextView mTextView;

    public interface VideoListPlayListener {
        void playVideo(NormalVideoPlayer player, int position);

        void stopVideo();
    }

    private VideoListPlayListener videoListPlayListener;

    public void setVideoListener(VideoListPlayListener videoListener) {
        videoListPlayListener = videoListener;
    }

    public VideoAutoPlayRecyclerView(Context context) {
        super(context);
        initData();
    }

    public VideoAutoPlayRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public VideoAutoPlayRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData();
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (currentPlayerPosition == RecyclerView.NO_POSITION) {
            return;
        }
        if (videoListPlayListener == null) return;
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
            View targetView = linearManager.findViewByPosition(currentPlayerPosition);
            float top = getTopPosition(targetView);
            if (top == Float.MIN_VALUE) {
                videoListPlayListener.stopVideo();
                currentPlayerPosition = RecyclerView.NO_POSITION;
                return;
            }
        }
    }

    @Override
    public void onScrollStateChanged(int newState) {
        super.onScrollStateChanged(newState);
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (videoListPlayListener == null) return;
        if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
        if (layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
            //获取最后一个可见view的位置
            int lastPosition = linearManager.findLastVisibleItemPosition();
            //获取第一个可见view的位置
            int firstPosition = linearManager.findFirstVisibleItemPosition();

            if (firstPosition <= lastPosition &&
                    (firstPosition != RecyclerView.NO_POSITION || lastPosition != RecyclerView.NO_POSITION)) {
                for (int currentPosition = firstPosition; currentPosition < lastPosition + 1; currentPosition++) {
                    ViewGroup tempView = (ViewGroup) linearManager.findViewByPosition(currentPosition);
                    if (isCenterItemView(tempView)) {
                        if (currentPlayerPosition != currentPosition) {
                            changed = true;
                            ViewGroup viewGroup = (ViewGroup) linearManager.findViewByPosition(currentPlayerPosition);
                            if (viewGroup != null) {
                                viewGroup.removeView(mPlayer);
                            }
                        } else {
                            changed = false;
                        }
                        currentPlayerPosition = currentPosition;
                        break;
                    }
                }
            }
            if (changed) {
                ViewGroup targetView = (ViewGroup) linearManager.findViewByPosition(currentPlayerPosition);
                float top = getTopPosition(targetView);
                if (top == Float.MIN_VALUE) {
                    return;
                }

                targetView.addView(mPlayer);
                if (mPlayer != null) {
                    mPlayer.release();
                }
                videoListPlayListener.playVideo(mPlayer, currentPlayerPosition);
            }
        }
    }

    private int getDy(View targetView) {
        if (targetView == null) return 0;
        int top = (int) getTopPosition(targetView);
        int height = targetView.getHeight();
        int targetTop = mCenterLine - height / 2;
        return top - targetTop;
    }

    private boolean isCenterItemView(View tempView) {
        if (tempView == null) {
            return false;
        }
        float top = getTopPosition(tempView);
        float bottom = top + tempView.getHeight();
        return ((top < mCenterLine && bottom > mCenterLine) || top == mCenterLine || bottom == mCenterLine);
    }

    private float getTopPosition(View targetView) {
        if (targetView == null) {
            return Float.MIN_VALUE;
        }
        final int[] location = new int[2];
        targetView.getLocationOnScreen(location);
        return location[1];
    }

    private void initData() {
        mCenterLine = ViewUtil.getScreenHeigth() / 2;

        mPlayer = new NormalVideoPlayer(getContext());
        ViewGroup.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.width = ViewUtil.getScreenWidth(); // 宽度为屏幕宽度
        params.height = (int) (params.width * 9f / 16f);    // 高度为宽度的9/16
        mPlayer.setLayoutParams(params);

        mPlayer.setOnPlayStateChangedListener(new NormalVideoPlayer.OnPlayStateChanged() {
            @Override
            public void onPlayStateChanged(int state) {
                if (state == NormalVideoPlayer.STATE_COMPLETED) {
                    setClickViewToCenter(currentPlayerPosition + 1);
                }
            }
        });
    }


    @Override
    public void run() {
        setClickViewToCenter(currentPlayerPosition + 1);
    }

    /**
     * 设置点击的itemView，使其滑动到屏幕中间
     *
     * @param position
     */
    public void setClickViewToCenter(int position) {
        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            View targetView = layoutManager.findViewByPosition(position);
            int dy = getDy(targetView);
            this.smoothScrollBy(0, dy);
        }
    }
}
