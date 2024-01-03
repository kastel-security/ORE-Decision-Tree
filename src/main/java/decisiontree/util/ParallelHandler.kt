package decisiontree.util

import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue

interface ComputeHandler {
    fun <U, V> handle(n: Int, prepare: (Int) -> U, compute: (U) -> V, finalize: (V) -> Unit)
}

object SequentialHandler: ComputeHandler {
    override fun <U, V> handle(n: Int, prepare: (Int) -> U, compute: (U) -> V, finalize: (V) -> Unit) {
        for (i in 0 until n) {
            finalize(compute(prepare(i)))
        }
    }
}

object ParallelHandler: ComputeHandler {

    override fun <U, V> handle(n: Int, prepare: (Int) -> U, compute: (U) -> V, finalize: (V) -> Unit) {
        val executor = ForkJoinPool.commonPool()
        val futures = LinkedBlockingQueue<Future<V>>(n)
        val startingThread = Thread {
            for (i in 0 until n) {
                val startValue = prepare(i)
                futures += executor.submit(Callable { compute(startValue) })
            }
        }
        startingThread.start()
        for (i in 0 until n) {
            finalize(futures.take().get())
        }
    }

}