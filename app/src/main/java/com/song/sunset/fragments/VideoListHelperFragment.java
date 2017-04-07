package com.song.sunset.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.song.sunset.R;
import com.song.sunset.adapters.VideoListHelperAdapter;
import com.song.sunset.beans.VideoBean;
import com.song.sunset.fragments.base.BaseFragment;
import com.song.sunset.utils.ViewUtil;
import com.song.sunset.utils.loadingmanager.ProgressLayout;
import com.song.sunset.utils.retrofit.RetrofitCallback;
import com.song.sunset.utils.retrofit.RetrofitService;
import com.song.sunset.utils.rxjava.RxUtil;
import com.song.sunset.utils.service.IfengNewsApi;
import com.song.sunset.utils.service.WholeApi;
import com.song.sunset.views.BaseLoadMoreView;

import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import rx.Observable;

/**
 * Created by songmw3 on 2017/3/13.
 * E-mail:z53520@qq.com
 */

public class VideoListHelperFragment extends BaseFragment implements BaseQuickAdapter.RequestLoadMoreListener, PtrHandler {

    private String typeid = "";
    private RecyclerView recyclerView;
    private VideoListHelperAdapter mAdapter;
    private int currentPage = 1;
    private boolean isLoading, isRefreshing = false;
    private PtrFrameLayout refreshLayout;
    private ProgressLayout progressLayout;

    private View.OnClickListener errorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDataFromRetrofit2(1);
        }
    };

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

        recyclerView = (RecyclerView) view.findViewById(R.id.rv_video_list);
        mAdapter = new VideoListHelperAdapter(getActivity());
        mAdapter.setOnLoadMoreListener(this, recyclerView);
        mAdapter.setLoadMoreView(new BaseLoadMoreView());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return ViewUtil.getScreenHeigth() / 3;
            }
        });
        getDataFromRetrofit2(currentPage);
    }

    public void getDataFromRetrofit2(int page) {
        Observable<List<VideoBean>> observable = RetrofitService.createApi(IfengNewsApi.class, WholeApi.IFENG_NEWS_BASE_URL).queryVideoObservable(page, "list", typeid);
        RxUtil.ifengNewsSubscribe(observable, new RetrofitCallback<VideoBean>() {
            @Override
            public void onSuccess(VideoBean videoBean) {
                progressLayout.showContent();
                List<VideoBean.ItemBean> videoBeanList = videoBean.getItem();
                if (isRefreshing) {
                    currentPage = 1;
                    isRefreshing = false;
                    mAdapter.setNewData(videoBeanList);
                    refreshLayout.refreshComplete();
                } else {
                    if (isLoading) {
                        isLoading = false;
                    }
                    mAdapter.addData(videoBeanList);
                    mAdapter.loadMoreComplete();
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
                        mAdapter.loadMoreEnd();
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
    public void onLoadMoreRequested() {
        if (isLoading) {
            return;
        }
        currentPage++;
        isLoading = true;
        getDataFromRetrofit2(currentPage);
    }

    @Override
    public void onStop() {
        super.onStop();
        currentPage = 1;
        isLoading = false;
        isRefreshing = false;
    }
}