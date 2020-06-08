package net.common

import db.DaoProduct
import net.PROCESSOR_THREADS
import protocol.Message
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object Processor {

    private val service = Executors.newFixedThreadPool(PROCESSOR_THREADS)
    val db = DaoProduct("file.db")

    fun process(serverThread: ServerThread, message: Message) {
        service.submit(ProcessorThread(serverThread, message))
    }

    fun ExecutorService.waitForStop(what: String) {
        shutdown()
        while (!isTerminated) {
            println("$what is waiting for shutdown") //TODO move it from here to Utils along with everythin in pRocessorhread
            awaitTermination(10, TimeUnit.SECONDS)
        }
    }

    fun waitForProcessorStop() = service.waitForStop("Processors")

}

