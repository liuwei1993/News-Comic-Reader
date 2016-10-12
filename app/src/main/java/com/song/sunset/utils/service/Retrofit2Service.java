package com.song.sunset.utils.service;

import com.song.sunset.beans.ComicRankListBean;
import com.song.sunset.beans.basebeans.BaseBean;
import com.song.sunset.beans.ComicClassifyBean;
import com.song.sunset.beans.ComicDetailBean;
import com.song.sunset.beans.ComicListBean;
import com.song.sunset.beans.ComicReadBean;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Song on 2016/9/18 0018.
 * Email:z53520@qq.com
 */
public interface Retrofit2Service {
    String COMIC_BASE_URL = "http://app.u17.com/v3/appV3/android/phone/";

    //此处若不添加max-age信息，会统一在OfflineCacheControlInterceptor类中添加默认的max-age
    //    @Headers("Cache-Control: public, max-age=3600")
    @GET("comic/detail_static_new")
    Call<BaseBean<ComicDetailBean>> queryComicDetailRDByGetCall(
            @Query("comicid") int comicid);

    //    @Headers("Cache-Control: public, max-age=3600")
    @GET("list/commonComicList")
    Call<BaseBean<ComicListBean>> queryComicListRDByGetCall(
            @Query("page") int page,
            @Query("argName") String argName,
            @Query("argValue") int argValue);

    //    @Headers("Cache-Control: public, max-age=3600")
    @GET("comic/chapterlist")
    Call<BaseBean<List<ComicReadBean>>> queryComicReadRDByGetCall(
            @Query("comicid") String comicid);

    //    @Headers("Cache-Control: public, max-age=3600")
    @GET("sort/mobileCateList")
    Call<BaseBean<ComicClassifyBean>> queryComicClassifyBeanByGetCall(
            @Query("version") int version);

    //    @Headers("Cache-Control: public, max-age=3600")
    @GET("rank/list")
    Call<BaseBean<ComicRankListBean>> queryComicRankListBeanByGetCall();
}
