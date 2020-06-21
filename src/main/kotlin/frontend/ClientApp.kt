package frontend

import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane

class ClientApp : JFrame("Client App") {


    private val productsTable = JPanel()
    private val groupTable = JPanel()
    private val mainPanel = JTabbedPane().apply {
        addTab("Products", productsTable)
        addTab("Groups", groupTable)
    }


    init {
        preferredSize = Dimension(800, 600)
        defaultCloseOperation = EXIT_ON_CLOSE
        contentPane = mainPanel

        pack()
        isVisible = true
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ClientApp()
        }
    }
}