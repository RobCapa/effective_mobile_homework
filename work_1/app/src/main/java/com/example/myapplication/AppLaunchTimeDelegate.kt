package com.example.myapplication

import java.util.Date
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class AppLaunchTimeDelegate: ReadOnlyProperty<Any, Date> {

    private val appLaunchTime = System.currentTimeMillis()

    override fun getValue(thisRef: Any, property: KProperty<*>): Date {
        return Date(appLaunchTime)
    }
}