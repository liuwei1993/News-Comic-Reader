package com.song.kotlin.utils

import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author songmingwen
 * @description
 * @since 2019/3/5
 */
class JdkDynamicProxy : InvocationHandler {

    var mTarget: Any? = null

    fun bind(target: Any): Any {
        this.mTarget = target
        return Proxy.newProxyInstance(target.javaClass.classLoader,
                target.javaClass.interfaces, this)
    }

//Java 代码正常运行，Kotlin 代码运行 crash。未解决
    @Throws(Throwable::class)
    override fun invoke(o: Any, method: Method, objects: Array<Any>): Any? {
        Log.e(this.javaClass.toString(), "before")
        val result = method.invoke(mTarget, *objects)
        Log.e(this.javaClass.toString(), "after")
        return result ?: Unit
    }
}