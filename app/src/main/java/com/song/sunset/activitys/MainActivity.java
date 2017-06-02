package com.song.sunset.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.TimePickerView;
import com.song.sunset.R;
import com.song.sunset.activitys.base.BaseActivity;
import com.song.sunset.fragments.CollectionFragment;
import com.song.sunset.fragments.ComicClassifyFragment;
import com.song.sunset.fragments.ComicRankFragment;
import com.song.sunset.fragments.PhoenixListFragment;
import com.song.sunset.fragments.MVPComicListFragment;
import com.song.sunset.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by songmw3 on 2016/12/2.
 * E-mail:z53520@qq.com
 */
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private TimePickerView pvTime;
    private long lastBackPressedTime;
    private OptionsPickerView pvOptions;
    private ArrayList<String> options1Items = new ArrayList<>();
    private FloatingActionButton fab;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initDrawer();
        initTimePicker();
        initOptionsPicker();
        setUpListener();
        switchFragmentDelay(PhoenixListFragment.class.getName(), "凤凰新闻");
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
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_map);
    }

    private void setUpListener() {
        navigationView.setNavigationItemSelectedListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
//                pvTime.show();
//                pvOptions.show();
//                MainActivity.this.startActivity(new Intent(MainActivity.this, SubScaleViewActivity.class));
                MainActivity.this.startActivity(new Intent(MainActivity.this, TouchEventTestActivity.class));
//                MainActivity.this.startActivity(new Intent(MainActivity.this, TempTestActivity.class));
//                new ImageViewer.Builder(MainActivity.this, new String[]{"http://img2.niutuku.com/1312/0831/0831-niutuku.com-28071.jpg",
//                        "http://img2.niutuku.com/desk/130220/52/52-niutuku.com-984.jpg",
//                        "http://img01.sogoucdn.com/app/a/100540002/490110.jpg",
//                        "http://att.x2.hiapk.com/forum/201409/10/173524pydcdt4ccz928j8d.jpg",
//                        "http://cdn.duitang.com/uploads/item/201409/07/20140907233240_VYNvH.jpeg"})
//                        .setStartPosition(0)
//                        .hideStatusBar(false)
//                        .show();

                RecursiveTest();
            }
        });
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

//    /**
//     * 将文本中的表情符号转换为表情图片
//     *
//     * @param text
//     * @return
//     */
//    public CharSequence replace(Context context, CharSequence text, int upResId, int downResId) {
//        // SpannableString连续的字符串，长度不可变，同时可以附加一些object;可变的话使用SpannableStringBuilder，参考sdk文档
//        SpannableString ss = new SpannableString("[up_quote]" + text + "[down_quote]");
//        // 得到要显示图片的资源
//        Drawable upDrawable = context.getResources().getDrawable(upResId);
//        Drawable downDrawable = context.getResources().getDrawable(downResId);
//        // 设置高度
//        upDrawable.setBounds(0, 0, upDrawable.getIntrinsicWidth(), upDrawable.getIntrinsicHeight());
//        downDrawable.setBounds(0, 0, downDrawable.getIntrinsicWidth(), downDrawable.getIntrinsicHeight());
//        // 跨度底部应与周围文本的基线对齐
//        ImageSpan upSpan = new ImageSpan(upDrawable, ImageSpan.ALIGN_BASELINE);
//        ImageSpan downSpan = new ImageSpan(downDrawable, ImageSpan.ALIGN_BASELINE);
//        // 附加图片
//        ss.setSpan(upSpan, 0, "[up_quote]".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        ss.setSpan(downSpan, "[up_quote]".length() + text.length(), "[up_quote]".length() + text.length() + "[down_quote]".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        return ss;
//    }

    @Override
    protected void onResume() {
        super.onResume();
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            switchFragmentDelay(MVPComicListFragment.class.getName(), "更新漫画");
        } else if (id == R.id.classify_comic) {
            switchFragmentDelay(ComicClassifyFragment.class.getName(), "分类漫画");
        } else if (id == R.id.nav_video) {
//            switchFragmentDelay(TVListFragment.class.getName(), "电视频道");
            VideoListActivity.start(this);
        } else if (id == R.id.nav_manage) {
//            ComicRankFragment.start(MainActivity.this);
            switchFragmentDelay(ComicRankFragment.class.getName(), "排行漫画");

        } else if (id == R.id.nav_map) {
            switchFragmentDelay(PhoenixListFragment.class.getName(), "凤凰新闻");
//            getmHandler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    LocationMarkerActivity.start(MainActivity.this);
//                }
//            }, 300);
        } else if (id == R.id.nav_collection) {
            switchFragmentDelay(CollectionFragment.class.getName(), "收藏漫画");

//            getmHandler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    BasicMapActivity.start(MainActivity.this);
//                    MeadiaPlayerActivity.start(MainActivity.this);
//                    VideoViewActivity.start(MainActivity.this);
//                }
//            }, 300);
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
                moveTaskToBack(true);
            } else {
                lastBackPressedTime = System.currentTimeMillis();
            }
        }
    }

    private void initTimePicker() {
        pvTime = new TimePickerView(MainActivity.this, TimePickerView.Type.YEAR_MONTH_DAY);
        //控制时间范围
//        Calendar calendar = Calendar.getInstance();
//        pvTime.setRange(calendar.get(Calendar.YEAR) - 20, calendar.get(Calendar.YEAR));//要在setTime 之前才有效果哦
        pvTime.setRange(1900, 2016);
        pvTime.setCyclic(false);
        pvTime.setCancelable(true);
        pvTime.setTime(new Date());
        pvTime.setTitle("请选择日期");
        //时间选择后回调
        pvTime.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                Calendar c = Calendar.getInstance();
                c.setTime(date);
//                Toast.makeText(MainActivity.this, c.get(Calendar.YEAR) + "年" + (1 + c.get(Calendar.MONTH)) + "月" + c.get(Calendar.DAY_OF_MONTH) + "日", Toast.LENGTH_SHORT).show();
//                Toast.makeText(MainActivity.this, DateTimeUtils.getConstellation(1 + c.get(Calendar.MONTH), Calendar.DAY_OF_MONTH), Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, DateTimeUtils.getAge(date) + "岁", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initOptionsPicker() {
        pvOptions = new OptionsPickerView(this);
        options1Items.add("男");
        options1Items.add("女");
        pvOptions.setPicker(options1Items);
        pvOptions.setTitle("选择性别");
        pvOptions.setCyclic(true);
        pvOptions.setLabels("");
        pvOptions.setSelectOptions(1);
        pvOptions.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                Toast.makeText(MainActivity.this, options1 + "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchFragmentDelay(final String className, final String title) {
        getmHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchFragment(className, R.id.activity_framelayout_main);
                toolbar.setTitle(title);
            }
        }, 300);
    }
}
