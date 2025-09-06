package ru.vladislavsumin.qa.core.proguardParser

import com.android.tools.r8.internal.Gk0
import com.android.tools.r8.naming.k
import com.android.tools.r8.retrace.RetraceClassResult

private val FirstFiled by lazy {
    val first = Gk0::class.java.getDeclaredField("b")
    first.isAccessible = true
    first
}

private val SecondField by lazy {
    val second = k::class.java.getDeclaredField("a")
    second.isAccessible = true
    second
}

val RetraceClassResult.className: String?
    get() {
        val find = FirstFiled.get(this)
        return find?.let { SecondField.get(it) as String }
    }
