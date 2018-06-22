package com.song.sunset.activitys

import android.os.Bundle
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.song.sunset.R
import com.song.sunset.utils.BitmapUtil
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_rx_java.*
import java.io.File


class RxJavaActivity : AppCompatActivity() {

    val TAG = RxJavaActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rx_java)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            start()
        }
    }

    fun start() {
        val file = File(Environment.getExternalStorageDirectory().path + File.separator + "sunset")
        val folders = file.listFiles()
        object : Thread() {
            override fun run() {
                super.run()
                for (folder in folders!!) {
                    val files = folder.listFiles()
                    files!!.forEach { file ->
                        if (file.name.endsWith(".png") || file.name.endsWith(".jpg")) {
                            val bitmap = BitmapUtil.getSmallBitmap(file.path, 200, 200)
                            this@RxJavaActivity.runOnUiThread({ Log.i(TAG, "run: width = " + bitmap.width + ";height=" + bitmap.height) })
                        }
                    }
                }
            }
        }.start()

        Observable.fromArray(folders!!)
                .flatMap { filesDir -> Observable.just(filesDir) }
                .filter{file.name.endsWith(".png")||file.name.endsWith(".jpg")}
                .map { BitmapUtil.getSmallBitmap(file.path, 200, 200) }
                .subscribeOn(Schedulers.io())
                .subscribe{ bitmap -> Log.i(TAG, "run: width = " + bitmap.width + ";height=" + bitmap.height)}
    }

}
