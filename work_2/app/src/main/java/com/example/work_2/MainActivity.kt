package com.example.work_2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.work_2.databinding.ActivityMainBinding
import com.jakewharton.rxbinding3.widget.textChangeEvents
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjects()
        networkRequest()
        timer()
        editTextDebounce()
        mergeObservables()
        recycler()
    }


    // Задание 1

    /** 1)
     * Вывод будет:
     *     doOnSubscribe = RxComputationThreadPool
     *     map = RxNewThreadScheduler
     *     flatMap = RxSingleScheduler
     *     subscribe = RxCachedThreadScheduler
     *
     * Разъяснения:
     *
     *     doOnSubscribe - будет вызван первым, потому что срабатывает в момент подписки
     *     будет иметь RxComputationThreadPool, потому что подписка идет снизу вверх
     *     и первым встречает subscribeOn(Schedulers.computation())
     *
     *     map - RxNewThreadScheduler, потому что timer, interval и некоторые другие методы
     *     устанавливают Scheduler через subscribeOn. Observable.timer установил RxNewThreadScheduler,
     *     нижележащий subscribeOn(io) эффекта не имеет
     *
     *     flatMap - RxSingleScheduler, потому что над ним поток меняется через observeOn на Single
     *
     *     subscribe - RxCachedThreadSchedule, потому что flatMap меняет поток после себя на тот,
     *     в котором работал Observable внутри flatMap
     * */


    /** 2)
     * Вывода не будет, потому что Subject является горячим источником,
     * и отправит данные еще до подписки на него
     * */

    private fun subjects() {

        // Первый вариант исправления: перенести подписку до отправки первого значения
        PublishSubject.create<String>().apply {
            subscribe { it.toLog() }
            onNext("PublishSubject 1")
            onNext("PublishSubject 2")
            onNext("PublishSubject 3")
        }

        // Второй вариант исправления: заменить на ReplaySubject, имеющим буффер
        ReplaySubject.create<String>().apply {
            onNext("ReplaySubject 1")
            onNext("ReplaySubject 2")
            onNext("ReplaySubject 3")
            subscribe { it.toLog() }
        }

    }


    // Задание 2

    /**
     * Сделайте сетевой запрос и отобразите результат на экране
     * */
    interface GoogleApi {
        @GET(".")
        fun getResult(): Single<String>
    }

    private fun networkRequest() {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.google.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val api = retrofit.create(GoogleApi::class.java)

        val disposable = api.getResult()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    binding.mainActivityTextViewNetworkRequestResult.text = result
                },
                { error ->
                    binding.mainActivityTextViewNetworkRequestResult.text = error.message
                }
            )
    }

    /**
     * Сделайте таймер. TextView которая раз в секунду меняется
     * */

    private fun timer() {
        val disposable = Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.mainActivityTextViewTimer.text = "$it"
            }
    }

    /**
     * Сделайте ресайклер. По нажатию на элемент передавайте его позицию во фрагмент.
     * Во фрагменте этот номер отображайте в тосте
     * */

    private fun recycler() {
        val subject = PublishSubject.create<Int>()

        with(binding.mainActivityRecyclerView) {
            adapter = ItemRecyclerAdapter(subject).apply {
                updateItems(
                    listOf(
                        Item(0),
                        Item(1),
                        Item(2),
                        Item(3),
                        Item(4),
                        Item(5),
                        Item(6),
                    )
                )
            }

            layoutManager = GridLayoutManager(this@MainActivity, 2)
        }

        val disposable = subject
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Toast.makeText(this, "$it", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Сделайте EditText. При наборе текста выводите в лог содержимое EditText всегда,
     * когда пользователь 3 секунды что-то не вводил
     * */

    private fun editTextDebounce() {
        val disposable = binding.mainActivityEditTextUserInput.textChangeEvents()
            .subscribeOn(Schedulers.io())
            .skip(1)
            .debounce(3, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                it.text.toLog()
            }
    }

    /**
     * Есть 2 сервера на которых лежат скидочные карты.
     * Нужно получить эти данные и вывести в единый список
     * */

    private fun mergeObservables() {
        val observableWithError = Observable.create { emitter ->
            emitter.onNext(1)
            emitter.onNext(2)
            emitter.onNext(3)
            emitter.onError(Exception())
        }

        val observableWithoutError = Observable.create { emitter ->
            emitter.onNext(4)
            emitter.onNext(5)
            emitter.onNext(6)
            emitter.onNext(7)
            emitter.onNext(8)
            emitter.onNext(9)
        }

        // Если 1 из запросов падает, то все равно выводить
        val disposable1 = Observable
            .mergeDelayError(observableWithError, observableWithoutError)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result ->
                "cart $result".toLog()
            }

        // Если 1 из запросов падает, то не выводить ничего
        val disposable2 = Observable
            .merge(observableWithoutError, observableWithError)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    "cart $result".toLog()
                },
                { error ->

                }
            )
    }

    private fun Any.toLog() {
        Log.d("tag", this.toString())
    }
}