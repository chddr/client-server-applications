package net.tcp

import net.HOST
import net.SERVER_PORT
import net.tcp.UtilsTCP.receive
import net.tcp.UtilsTCP.send
import protocol.Message
import protocol.Packet
import java.net.InetAddress
import java.net.Socket
import kotlin.concurrent.thread

class UserTestTCP(private val clientId: Byte = 0, private val userId: Int = 0) {

    private val socket = Socket(InetAddress.getByName(HOST), SERVER_PORT).apply { soTimeout = 0 }
    private val input = System.`in`.bufferedReader()
    private var outIndex: Long = 0

    init {
        println(" --- TESTER ---\n")
        println("First input the command number , -1 for reference or -2 to quit the tester\n")
        reference()
        println("STARTING: ")

        socket.use {
            launchReceiver()
            run()
        }
    }

    private fun run() {
        loop@ while (true) {

            when (val code = readCode()) {
                -1 -> reference()
                -2 -> break@loop
                -3 -> continue@loop
                in Message.ClientCommands -> {
                    val command = Message.ClientCommands[code]

                    println("Command: ${command.name}")
                    println("Now input the message: ")
                    val msg = input.readLine()
                    sendMessage(command, msg)
                }
                else -> println("wrong command, continuing...")
            }
        }
    }

    private fun readCode(): Int {
        println("Input the code:")
        return try {
            input.readLine().toInt()
        } catch (e: NumberFormatException) {
            println("not a code, continuing...")
            -3
        }
    }

    private fun sendMessage(command: Message.ClientCommands, msg: String) {
        socket.send(Packet(
                clientId,
                outIndex++,
                Message(
                        command,
                        userId,
                        msg
                )
        ))
    }

    private fun launchReceiver() {
        thread(isDaemon = true) {
            while (true) socket.receive()
        }
    }

    private fun reference() {
        println("Commands:")
        for ((i, value) in Message.ClientCommands.values().iterator().withIndex()) {
            println("$i\t${value.name}")
        }
        println()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            UserTestTCP()
        }
    }
}