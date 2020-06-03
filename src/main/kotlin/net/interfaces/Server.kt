package net.interfaces

interface Server {
    fun waitForThread(): ServerThread
    fun stop()
}