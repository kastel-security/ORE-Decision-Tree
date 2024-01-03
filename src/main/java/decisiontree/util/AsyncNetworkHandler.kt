package decisiontree.util

import org.apache.commons.io.input.CountingInputStream
import org.apache.commons.io.output.CountingOutputStream
import java.io.Closeable
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class AsyncNetworkHandler(socket: Socket): Closeable {
    private val couts = CountingOutputStream(socket.getOutputStream().buffered())
    private val outs = ObjectOutputStream(couts)
    init { outs.flush() }
    private val cins = CountingInputStream(socket.getInputStream().buffered())
    private val ins = ObjectInputStream(cins)
    private val outQueue: BlockingQueue<Any> = LinkedBlockingQueue()
    private val outThread: Thread
    @Volatile
    private var isRunning = true

    init {
        outThread = Thread {
            while (isRunning || !outQueue.isEmpty()) {
                try {
                    if (outQueue.isEmpty()) {
                        outs.flush()
                    }
                    outs.writeObject(outQueue.take())
                } catch (e: InterruptedException) {
                }
            }
            try {
                outs.flush()
            } catch (_ : IOException) {}
        }
        outThread.name = "AsyncNetwork Output"
        outThread.isDaemon = true
        outThread.start()
    }

    fun <T> writeObject(obj: T) {
        if (!isRunning) {
            throw IOException("Closed output stream.")
        }
        outQueue.put(obj)
    }

    fun <T> readObject(): T {
        return ins.readObject() as T
    }

    override fun close() {
        isRunning = false
        outThread.interrupt()
        outThread.join()
        try {
            outs.close()
        } finally {
        }
    }

    fun bytesSent(): Long {
        return couts.byteCount
    }

    fun bytesReceived(): Long {
        return cins.byteCount
    }
}
