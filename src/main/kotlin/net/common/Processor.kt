package net.common

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import db.DaoProduct
import net.PROCESSOR_THREADS
import net.common.utils.ProcessorUtils.waitForStop
import protocol.Message
import java.util.concurrent.Executors

object Processor {

    private val service = Executors.newFixedThreadPool(PROCESSOR_THREADS)
    val db = DaoProduct("file.db")
    val OBJECT_MAPPER = jacksonObjectMapper()

    fun process(serverThread: ServerThread, message: Message) {
        service.submit(ProcessorThread(serverThread, message))
    }

    fun waitForProcessorStop() = service.waitForStop("Processors")

}

