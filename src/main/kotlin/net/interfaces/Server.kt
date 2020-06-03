package net.interfaces

interface Server {
    fun serverCycle()
    fun stop()
    fun waitForStop()
}