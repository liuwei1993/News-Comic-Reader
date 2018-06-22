package com.song.sunset.services.impl;

import android.content.Context;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.song.sunset.IMusicCallBackListener;
import com.song.sunset.IMusicGetter;
import com.song.sunset.utils.MusicLoader;
import com.song.sunset.utils.rxjava.RxUtil;


import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Song on 2018/1/5 0005.
 * E-mail: z53520@qq.com
 */

public class MusicGetterImpl extends IMusicGetter.Stub {

    public static final String TAG = MusicGetterImpl.class.getName();

    private final RemoteCallbackList<IMusicCallBackListener> mCallBacks = new RemoteCallbackList<>();

    private Context mContext;

    public MusicGetterImpl(Context context) {
        mContext = context;
    }

    @Override
    public void getMusicList(final IMusicCallBackListener listener) throws RemoteException {
        Log.i(TAG, "in");
        mCallBacks.register(listener);

        Disposable disposable = Observable.just(0)
                .map(integer -> {
                    Log.i(TAG, "get");
                    return MusicLoader.instance().getMusicList(mContext);
                })
                .filter(musicInfos -> {
                    Log.i(TAG, "filter");
                    return musicInfos != null;
                })
                .compose(RxUtil.getDefaultScheduler())
                .subscribe(musicInfo -> {
                    Log.i(TAG, "next：" + musicInfo.toString());
                    final int N = mCallBacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            Log.i(TAG, "success" + musicInfo.toString());
                            mCallBacks.getBroadcastItem(i).success(musicInfo);
                        } catch (RemoteException ignored) {
                            Log.i(TAG, "exception:" + ignored);
                            ignored.printStackTrace();
                        }
                    }
                    mCallBacks.finishBroadcast();
                }, throwable -> {
                    Log.i(TAG, "error: " + throwable.getMessage());
                    final int N = mCallBacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            mCallBacks.getBroadcastItem(i).failure();
                        } catch (RemoteException ignored) {
                        }
                    }
                    mCallBacks.finishBroadcast();
                });
    }
}
