package frontend.swing_ui

import frontend.HttpClientLogic
import frontend.swing_ui.sub_elements.LoginPanel
import frontend.swing_ui.sub_elements.ProductsPanel
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane


class ClientApp(url: String) : JFrame("Client App") {

    private val client = HttpClientLogic(url)

    private val loginPanel = LoginPanel(client)
    private val productsPanel = ProductsPanel(client, this)
    private val groupTable = JPanel()

    private val mainPanel = JTabbedPane().apply {
        addTab("Login", loginPanel)
        addTab("Products", productsPanel)
        addTab("Groups", groupTable)
    }


    init {
        minimumSize = Dimension(400, 400)
        defaultCloseOperation = EXIT_ON_CLOSE
        contentPane = mainPanel


        setLocationRelativeTo(null)
        pack()
        isVisible = true
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ClientApp("http://localhost:8080/")
        }
    }
}