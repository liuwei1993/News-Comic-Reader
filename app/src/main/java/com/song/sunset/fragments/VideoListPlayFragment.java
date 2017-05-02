package com.song.sunset.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.song.sunset.R;
import com.song.sunset.adapters.VideoListAdapter;
import com.song.sunset.beans.VideoBean;
import com.song.sunset.fragments.base.BaseFragment;
import com.song.sunset.impls.LoadingMoreListener;
import com.song.sunset.utils.ViewUtil;
import com.song.sunset.utils.loadingmanager.ProgressLayout;
import com.song.sunset.utils.retrofit.RetrofitCallback;
import com.song.sunset.utils.retrofit.RetrofitService;
import com.song.sunset.utils.rxjava.RxUtil;
import com.song.sunset.utils.service.PhoenixNewsApi;
import com.song.sunset.utils.service.WholeApi;
import com.song.sunset.views.LoadMoreRecyclerView;

import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import rx.Observable;

/**
 * Created by Song on 2017/4/27 0027.
 * E-mail: z53520@qq.com
 */

public class VideoListPlayFragment extends BaseFragment implements PtrHandler, LoadingMoreListener {

    private String typeid = "";
    private LoadMoreRecyclerView recyclerView;
    private VideoListAdapter mAdapter;
    private int currentPage = 1;
    private boolean isLoading, isRefreshing = false;
    private PtrFrameLayout refreshLayout;
    private ProgressLayout progressLayout;
    private int currentPlayerPosition = -1;
    private RelativeLayout progressBar;

    private View.OnClickListener errorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDataFromRetrofit2(1);
        }
    };
    private int mCenterLine;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            typeid = bundle.getString("typeid");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_videolist_helper_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressLayout = (ProgressLayout) view.findViewById(R.id.progress);
        progressLayout.showLoading();

        progressBar = (RelativeLayout) view.findViewById(R.id.id_loading_more_progress);
        showProgress(false);

        refreshLayout = (PtrFrameLayout) view.findViewById(R.id.id_comic_list_swipe_refresh);
        StoreHouseHeader header = new StoreHouseHeader(getContext());
        header.setPadding(0, ViewUtil.dip2px(2), 0, ViewUtil.dip2px(2));
        header.initWithString("Song");
        header.setTextColor(getResources().getColor(R.color.colorPrimary));
        header.setBackgroundColor(getResources().getColor(R.color.white));

        refreshLayout.setDurationToCloseHeader(1500);
        refreshLayout.setHeaderView(header);
        refreshLayout.addPtrUIHandler(header);
        refreshLayout.setPtrHandler(this);

        recyclerView = (LoadMoreRecyclerView) view.findViewById(R.id.rv_video_list);
        setRVCenterLine();

        mAdapter = new VideoListAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLoadingMoreListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return ViewUtil.getScreenHeigth() / 3;
            }
        });
        getDataFromRetrofit2(currentPage);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                    //判断是当前layoutManager是否为LinearLayoutManager
                    // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
                    if (layoutManager instanceof LinearLayoutManager) {
                        LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                        //获取最后一个可见view的位置
                        int lastItemPosition = linearManager.findLastVisibleItemPosition();
                        //获取第一个可见view的位置
                        int firstItemPosition = linearManager.findFirstVisibleItemPosition();

//                        View targetView = linearManager.findViewByPosition(firstItemPosition);
//                        if (Math.abs(targetView.getTop()) <= 1) {
//                            return;
//                        }
//                        recyclerView.smoothScrollBy(0, targetView.getTop());

//                        View targetView = null;
//                        for (int currentPosition = firstItemPosition; currentPosition < lastItemPosition + 1; currentPosition++) {
//                            View tempView = linearManager.findViewByPosition(currentPosition);
//                            if (isCenterItemView(tempView)) {
//                                currentPlayerPosition = currentPosition;
//                                targetView = tempView;
//                                break;
//                            }
//                        }
//                        if (targetView == null) return;
//                        int dY = (int) getCenterDy(targetView);
//                        if (Math.abs(dY) <= 1) {
//                            return;
//                        }
//                        recyclerView.smoothScrollBy(0, dY);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });

    }

    private float getTopPosition(View targetView) {
        final int[] location = new int[2];
        targetView.getLocationOnScreen(location);
        return location[1];
    }

    private float getCenterDy(View targetView) {
        float top = getTopPosition(targetView);
        float bottom = top + targetView.getHeight();
        float targetViewCenterLine = (top + bottom) / 2;
        return targetViewCenterLine - mCenterLine;
    }

    private void setRVCenterLine() {
        mCenterLine = ViewUtil.getScreenHeigth() / 2;
    }

    private boolean isCenterItemView(View tempView) {
        if (tempView == null) {
            return false;
        }
        float top = getTopPosition(tempView);
        float bottom = top + tempView.getHeight();
        return ((top < mCenterLine && bottom > mCenterLine) || top == mCenterLine || bottom == mCenterLine);
    }

    public void getDataFromRetrofit2(int page) {
        Observable<List<VideoBean>> observable = RetrofitService.createApi(PhoenixNewsApi.class, WholeApi.PHOENIX_NEWS_BASE_URL).queryVideoObservable(page, "list", typeid);
        RxUtil.phoenixNewsSubscribe(observable, new RetrofitCallback<VideoBean>() {
            @Override
            public void onSuccess(VideoBean videoBean) {
                progressLayout.showContent();
                List<VideoBean.ItemBean> videoBeanList = videoBean.getItem();
                if (isRefreshing) {
                    currentPage = 1;
                    isRefreshing = false;
                    if (mAdapter != null) {
                        mAdapter.setData(videoBeanList);
                    }
                    refreshLayout.refreshComplete();
                } else {
                    if (isLoading) {
                        isLoading = false;
                    }
                    if (mAdapter != null) {
                        mAdapter.addDatas(videoBeanList);
                        showProgress(false);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMsg) {
                if (isRefreshing) {
                    isRefreshing = false;
                    refreshLayout.refreshComplete();
                } else {
                    currentPage--;
                    if (isLoading) {
                        isLoading = false;
                        showProgress(false);
                    } else {
                        progressLayout.showError(getResources().getDrawable(R.drawable.icon_new_style_failure), "连接失败",
                                "无法建立连接",
                                "点击重试", errorClickListener, null);
                    }
                }
            }
        });
    }

    @Override
    public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
        return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
    }

    @Override
    public void onRefreshBegin(PtrFrameLayout frame) {
        if (isRefreshing) {
            return;
        }
        isRefreshing = true;
        getDataFromRetrofit2(1);
    }

    @Override
    public void onStop() {
        super.onStop();
        currentPage = 1;
        isLoading = false;
        isRefreshing = false;
    }

    @Override
    public void onLoadingMore() {
        if (isLoading) {
            return;
        }
        showProgress(true);
        currentPage++;
        isLoading = true;
        getDataFromRetrofit2(currentPage);
    }

    public void showProgress(boolean flag) {
        progressBar.setVisibility(flag ? View.VISIBLE : View.GONE);
    }
}