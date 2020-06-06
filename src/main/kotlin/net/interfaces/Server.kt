package net.interfaces

import java.io.Closeable
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException

interface Server: Closeable {
    /**Creates a new ServerThread to be added to ThreadPool by ServerRunner*/
    @Throws(SocketTimeoutException::class, SocketException::class, IOException::class)
    fun waitForThread(): ServerThread
    override fun close()
}