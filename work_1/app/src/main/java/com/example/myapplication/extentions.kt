package com.example.myapplication

fun List<*>.findInt(): Int? {
    return firstOrNull { it is Int } as Int?
}