package decisiontree.util

val prefix = ThreadLocal.withInitial { "--" }

inline fun <T> timed(name: String, f: () -> T): T {
    val startTime = System.currentTimeMillis()
    val p = prefix.get()
    prefix.set(p + "--")
    println("$p $name started.")
    val result: T
    try {
        result = f()
    } finally {
        prefix.set(p)
        val endTime = System.currentTimeMillis()
        println("$p $name finished. (%.2fs)".format((endTime - startTime) / 1000.0))
    }
    return result
}

inline fun benchmark(name: String, seconds: Int, f: (Long) -> Unit) {
    var startTime = 0L
    val p = prefix.get()
    prefix.set(p + "--")
    println("$p $name started.")
    var operations = 0L
    try {
        val testStartTime = System.currentTimeMillis()
        val targetTime = testStartTime + seconds * 1000
        //First estimate the amount of operations, that can be done in 10 seconds.
        do {
            f(operations)
            operations++
        } while (System.currentTimeMillis() < targetTime)

        //Now do the actual benchmark. This avoids the timing influence of time measurement in the benchmark.
        startTime = System.nanoTime()
        for (i in 0L until operations) {
            f(i)
        }
    } finally {
        prefix.set(p)
        val endTime = System.nanoTime()
        val durationInSeconds = (endTime - startTime) / 1_000_000_000.0
        println("$p $name finished. $operations operations in %.3fs (%.1Eop/s)".format(durationInSeconds, operations / durationInSeconds))
    }
}