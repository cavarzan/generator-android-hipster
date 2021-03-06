package <%= appPackage %>.util

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class RetryWhen<T>: ObservableTransformer<T, T> {

    private var count: Int by Delegates.notNull()
    private var period: Int by Delegates.notNull()
    private var backoff: Int by Delegates.notNull()

    @Suppress("MagicNumber")
    constructor() {
        count = 3
        period = 1000
        backoff = 0
    }

    @Suppress("MagicNumber")
    constructor(count: Int) {
        this.count = count
        period = 1000
        backoff = 0
    }

    @Suppress("MagicNumber")
    constructor(count: Int, period: Int) {
        this.count = count
        this.period = period
        backoff = 0
    }

    constructor(count: Int, period: Int, backoff: Int) {
        this.count = count
        this.period = period
        this.backoff = backoff
    }

    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream.retryWhen {
            it.cast(Throwable::class.java)
                    .zipWith(1..count) { throwable, counter -> Pair(throwable, counter) }
                    .flatMap {
                        if (it.second < count)
                            Observable.timer((period + period * backoff * count).toLong(), TimeUnit.MILLISECONDS)
                        else
                            Observable.error(it.first)
                    }
        }
    }
}