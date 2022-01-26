package dev.ithurts.plugin.ide.service.binding

import com.intellij.util.ReflectionUtil

object ReflectionUtils {
    fun <T> invokeMethod(target: Any, methodName: String, vararg args: Class<*>): T {
        val method =ReflectionUtil.getDeclaredMethod(target::class.java, methodName, *args)
        return method?.invoke(target, *args) as T
    }
}