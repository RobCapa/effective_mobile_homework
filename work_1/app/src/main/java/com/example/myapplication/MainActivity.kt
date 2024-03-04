package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

private const val TAG = "TAG"

class MainActivity : AppCompatActivity() {

    private val appLaunchTime: Date by AppLaunchTimeDelegate()

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startDisplayingAppLaunchTime()
        findIntInList()
        mutableListOf(null, 12, 5, null, 6, null, 32, 10).let(::shakerSort)
    }

    /** Задача 1
     * Ответ: Да, потому что data класс автоматически переопределяет hashCode на основании
     * первичного конструктора. В результате мы рискуем не найти наш элемент в HashMap,
     * поскольку поиск в ней нужного бакета завязан на hashCode ключа
     * */

    /** Задача 2 */
    private fun startDisplayingAppLaunchTime() {
        scope.launch {
            val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(appLaunchTime)
            while (true) {
                Log.d(TAG, "app launch time : $formattedDate")
                delay(3000)
            }
        }
    }

    /** Задача 3 */
    private fun findIntInList() {
        val list = listOf(
            2L,
            "",
            3.2f,
            Any(),
            null,
            4,
        )

        Log.d(TAG, "found Int : ${list.findInt()}")
    }

    /** Задача 4 */
    private fun shakerSort(list: List<Int?>?): List<Int?>? {
        Log.d(TAG, "original list : $list")

        list ?: return null

        val mutableList = list.toMutableList()

        fun swap(currentIndex: Int, nextIndex: Int) {
            val temp = mutableList[currentIndex]
            mutableList[currentIndex] = mutableList[nextIndex]
            mutableList[nextIndex] = temp
        }

        var swapped: Boolean

        var start = 0
        var end = list.lastIndex

        do {
            swapped = false

            for (index in start until end) {
                val currentValue = mutableList[index] ?: Int.MAX_VALUE
                val nextValue = mutableList[index + 1] ?: Int.MAX_VALUE

                if (currentValue > nextValue) {
                    swap(index, index + 1)
                    swapped = true
                }
            }

            if (!swapped) break

            end--

            for (index in end downTo start) {
                val currentValue = mutableList[index] ?: Int.MAX_VALUE
                val nextValue = mutableList[index + 1] ?: Int.MAX_VALUE

                if (currentValue > nextValue) {
                    swap(index, index + 1)
                    swapped = true
                }
            }

            start++

        } while (swapped)

        Log.d(TAG, "sorted list : $mutableList")

        return mutableList
    }
}