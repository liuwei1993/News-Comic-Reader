package com.song.sunset.activitys;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.song.core.statusbar.StatusBarUtil;
import com.song.sunset.IPush;
import com.song.sunset.R;
import com.song.sunset.activitys.base.BaseActivity;
import com.song.sunset.beans.CollectionOnlineListBean;
import com.song.sunset.beans.ComicCollectionBean;
import com.song.sunset.beans.ComicLocalCollection;
import com.song.sunset.beans.MusicInfo;
import com.song.sunset.fragments.CollectionFragment;
import com.song.sunset.fragments.ComicClassifyFragment;
import com.song.sunset.fragments.ComicRankFragment;
import com.song.sunset.fragments.PhoenixListFragment;
import com.song.sunset.fragments.MVPComicListFragment;
import com.song.sunset.mvp.models.ComicCollectionModel;
import com.song.sunset.mvp.presenters.ComicCollectionPresenter;
import com.song.sunset.mvp.views.ComicCollectionView;
import com.song.sunset.services.MusicGetterService;
import com.song.sunset.services.impl.BinderPoolImpl;
import com.song.sunset.services.impl.PushImpl;
import com.song.sunset.services.managers.BinderPool;
import com.song.sunset.services.managers.MessengerManager;
import com.song.sunset.services.managers.MusicGetterManager;
import com.song.sunset.services.managers.PushManager;
import com.song.sunset.utils.AppConfig;
import com.song.sunset.utils.GreenDaoUtil;
import com.song.sunset.utils.MusicLoader;
import com.song.sunset.utils.SPUtils;
import com.song.sunset.utils.process.AndroidProcesses;
import com.song.sunset.utils.process.models.AndroidAppProcess;
import com.squareup.haha.perflib.Main;
import com.sunset.greendao.gen.ComicLocalCollectionDao;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Song on 2016/12/2.
 * E-mail:z53520@qq.com
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, ComicCollectionView {

    public static final String TAG = MainActivity.class.getName();

    private Toolbar toolbar;
    private long lastBackPressedTime;
    private FloatingActionButton fab;
    private NavigationView navigationView;
    private static final int MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1001;
    private ComicCollectionPresenter mPresenter;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //夜间模式一定要包含日间模式的配置文件：如color，style......
        setDayNightMode(isNightMode());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions();
        initView();
        initDrawer();
        setUpListener();

        switchFragmentDelay(PhoenixListFragment.class.getName(), getResources().getString(R.string.phoenix_news), 0);
        mPresenter = new ComicCollectionPresenter();
        mPresenter.attachVM(this, new ComicCollectionModel());
        mPresenter.getNewestCollectedComic();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (!hasPermission()) {
//                startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
//                        MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS);
//            }
//        }
    }


    public void verifyStoragePermissions() {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //检测用户是否对本app开启了“Apps with usage access”权限
//    private boolean hasPermission() {
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) return false;
//        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
//                android.os.Process.myUid(), getPackageName());
//        return mode == AppOpsManager.MODE_ALLOWED;
//    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean swipeBackPriority() {
        return false;
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.logo);
        fab = (FloatingActionButton) findViewById(R.id.fab);
    }

    private void initDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        StatusBarUtil.setColorForDrawerLayout(this, drawer, getResources().getColor(R.color.transparent));
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_news);
        navigationView.setItemIconTintList(null);
        setDrawerLeftEdgeSize(this, drawer, 0.35f);
    }

    private void setUpListener() {
        navigationView.setNavigationItemSelectedListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                MainActivity.this.startActivity(new Intent(MainActivity.this, SubScaleViewActivity.class));
//                MainActivity.this.startActivity(new Intent(MainActivity.this, TouchEventTestActivity.class));
                MainActivity.this.startActivity(new Intent(MainActivity.this, TempTestActivity.class));
//                MainActivity.this.startActivity(new Intent(MainActivity.this, TransTestActivity.class));
//                ScrollingActivity.start(MainActivity.this);
//                new ImageViewer.Builder(MainActivity.this, new String[]{"http://img2.niutuku.com/1312/0831/0831-niutuku.com-28071.jpg",
//                        "http://img2.niutuku.com/desk/130220/52/52-niutuku.com-984.jpg",
//                        "http://img01.sogoucdn.com/app/a/100540002/490110.jpg",
//                        "http://att.x2.hiapk.com/forum/201409/10/173524pydcdt4ccz928j8d.jpg",
//                        "http://cdn.duitang.com/uploads/item/201409/07/20140907233240_VYNvH.jpeg"})
//                        .setStartPosition(0)
//                        .hideStatusBar(false)
//                        .show();

//                RecursiveTest();
//                PushManager.getInstance().connect();
//                PushManager.getInstance().sendMusicInfo(MusicLoader.instance().getMusicList().get(0));
//                MessengerManager.getInstance().sendMessage();
                MusicGetterManager.getInstance().setMusicCallBackListener(new MusicGetterManager.MusicCallBackListener() {
                    @Override
                    public void success(List<MusicInfo> list) {
                        Log.i(TAG, list.toString());
                    }

                    @Override
                    public void failure() {
                        Log.i(TAG, "false");
                    }
                });
                MusicGetterManager.getInstance().getMusicLists();
//                useBinderPool();
//                Log.i("music_list: ", MusicLoader.instance(MainActivity.this.getContentResolver()).getMusicList().toString());

//                switchDayNightMode();

//                PrintProcess();
//                getTopApp();
//                Log.i("recent_song", getTaskList());
            }
        });
    }

    public String getTaskList() {
        String apps = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return apps;
        }
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = getPackageManager();
        try {
            List<ActivityManager.RecentTaskInfo> list = am.getRecentTasks(64, 0);
            for (ActivityManager.RecentTaskInfo ti : list) {
                Intent intent = ti.baseIntent;
                ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
                if (resolveInfo != null) {
                    apps = apps.equals("") ? resolveInfo.loadLabel(pm) + "" : apps + "," + resolveInfo.loadLabel(pm);
                }
            }
            return apps;
        } catch (SecurityException se) {
            se.printStackTrace();
            return apps;
        }
    }

    private void PrintProcess() {
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();

        for (AndroidAppProcess process : processes) {
            Log.d("process_song", process.getPackageName());
        }
    }

    private void getTopApp() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager m = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            if (m != null) {
                long now = System.currentTimeMillis();
                //获取600秒之内的应用数据
                List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 600 * 1000, now);
                Log.i("song", "Running app number in last 600 seconds : " + stats.size());

                //取得最近运行的一个app，即当前运行的app
                if (!stats.isEmpty()) {
                    for (int i = 0; i < stats.size(); i++) {
                        Log.i("song", "top running app is : " + stats.get(i).getPackageName());
                    }
                }

            }
        }
    }

    private void useBinderPool() {
        IBinder iBinder = BinderPool.getInstance(MainActivity.this).queryBinder(BinderPoolImpl.BINDER_PUSH);
        IPush iPush = PushImpl.asInterface(iBinder);
        try {
            iPush.sendMusicInfo(MusicLoader.instance().getMusicList().get(0));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void RecursiveTest() {
        long start2 = System.currentTimeMillis();
        long result2 = getPlus(1000L);
        long end2 = System.currentTimeMillis();
        Log.i("结果对比", "result2=" + result2 + "; time2 = " + (end2 - start2) + "millis");

        final long start1 = System.currentTimeMillis();

        Observable.just(1000L)
                .map(new Func1<Long, Long>() {
                    @Override
                    public Long call(Long aLong) {
                        return getOrderPlus(aLong);
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        long result1 = aLong;
                        long end1 = System.currentTimeMillis();
                        Log.i("结果对比", "result1=" + result1 + "; time1 = " + (end1 - start1) + "millis");
                    }
                });
    }

    private long getFactorial(long endNum) {
        if (endNum <= 1) {
            return 1;
        } else {
            return getFactorial(endNum - 1) * endNum;
        }
    }

    /**
     * Java没有实现编译器尾递归的优化
     *
     * @param endNum
     * @return
     */
    private long getOrderPlus(long endNum) {
        return endNum == 1 ? 1 : getOrderPlus(endNum, 1);
    }

    private long getOrderPlus(long endNum, long sum) {
        return endNum == 1 ? sum : getOrderPlus(endNum - 1, sum + endNum);
    }

    private long getPlus(long endNum) {
        int sum = 0;
        for (long i = 0; i < endNum + 1; i++) {
            sum += i;
        }
        return sum;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            switchFragmentDelay(MVPComicListFragment.class.getName(), getResources().getString(R.string.newest_comic));
        } else if (id == R.id.nav_classify_comic) {
            switchFragmentDelay(ComicClassifyFragment.class.getName(), getResources().getString(R.string.classify_comic));
        } else if (id == R.id.nav_video) {
            VideoListActivity.start(this);
        } else if (id == R.id.nav_rank_comic) {
            switchFragmentDelay(ComicRankFragment.class.getName(), getResources().getString(R.string.rank_comic));
        } else if (id == R.id.nav_news) {
            switchFragmentDelay(PhoenixListFragment.class.getName(), getResources().getString(R.string.phoenix_news));
        } else if (id == R.id.nav_collection) {
            switchFragmentDelay(CollectionFragment.class.getName(), getResources().getString(R.string.collection_comic));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressedSupport() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (System.currentTimeMillis() - lastBackPressedTime < 2000) {
//                moveTaskToBack(true);
                SPUtils.setBooleanByName(this, SPUtils.APP_FIRST_INSTALL, false);
                finish();
            } else {
                lastBackPressedTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detachVM();
        }
        MessengerManager.getInstance().destroy(this);
        PushManager.getInstance().destory(this);
        super.onDestroy();
    }

    private void switchFragmentDelay(final String className, final String title) {
        switchFragmentDelay(className, title, 300L);
    }

    private void switchFragmentDelay(final String className, final String title, long delayTime) {
        fab.setVisibility(TextUtils.equals(className, PhoenixListFragment.class.getName()) ? View.VISIBLE : View.GONE);
        getmHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchFragment(className, R.id.activity_framelayout_main);
                toolbar.setTitle(title);
            }
        }, delayTime);
    }

    @Override
    public void onSuccess(CollectionOnlineListBean collectionOnlineListBean) {
        ComicLocalCollectionDao comicLocalCollectionDao = GreenDaoUtil.getDaoSession().getComicLocalCollectionDao();
        List<ComicLocalCollection> list = comicLocalCollectionDao.loadAll();
        if (list == null || list.isEmpty() || collectionOnlineListBean == null || collectionOnlineListBean.getFavList().isEmpty())
            return;
        ArrayList<String> newList = new ArrayList<>();
        for (ComicLocalCollection bean : list) {
            for (ComicCollectionBean onlineBean : collectionOnlineListBean.getFavList()) {
                if (TextUtils.equals(bean.getComicId() + "", onlineBean.getComic_id())
                        && !TextUtils.equals(bean.getChapterNum(), onlineBean.getPass_chapter_num() + "")) {
                    newList.add(onlineBean.getName());
                }
            }
        }
        if (newList.isEmpty()) {
            return;
        }
        StringBuilder content = new StringBuilder();
        for (String name : newList) {
            content.append(name).append("、");
        }
        content = new StringBuilder(content.substring(0, content.length() - 1));
        content.append("有更新");
        showNotification(content.toString());
    }

    private void showNotification(String content) {
        Intent intent = new Intent(this, ComicCollectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) (Math.random() * 100000),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.mipmap.logo)
                .setContentIntent(pendingIntent)
                .setContentTitle("漫画有更新")
                .setContentText(content)
                .setPriority(Notification.PRIORITY_MAX)
                .setAutoCancel(false);
        Notification notification = nBuilder.build();
        notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_SHOW_LIGHTS
                | Notification.FLAG_ONGOING_EVENT;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, notification);
        }
    }

    @Override
    public void onFailure(int errorCode, String errorMsg) {

    }
}
