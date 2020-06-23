package frontend.swing_ui

import frontend.http.HttpClientLogic
import frontend.swing_ui.sub_elements.LoginPanel
import frontend.swing_ui.sub_elements.groups.GroupsPanel
import frontend.swing_ui.sub_elements.products.ProductsPanel
import frontend.swing_ui.sub_elements.users.UserPanel
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JTabbedPane


class ClientApp(url: String) : JFrame("Client App") {

    private val client = HttpClientLogic(url)

    private val loginPanel = LoginPanel(client, this)
    private val productsPanel = ProductsPanel(client, this)
    private val groupsPanel = GroupsPanel(client, this)
    private val userPanel = UserPanel(client, this)

    private val mainPanel = JTabbedPane().apply {
        addTab("Login", loginPanel)
    }


    fun addAdminTabs() {
        mainPanel.addTab("Products", productsPanel)
        mainPanel.addTab("Groups", groupsPanel)
        mainPanel.addTab("Users", userPanel)
    }

    fun addUserTabs() {
        mainPanel.addTab("Products", productsPanel)
        mainPanel.addTab("Groups", groupsPanel)
    }

    fun removeTabs() {
        mainPanel.remove(productsPanel)
        mainPanel.remove(groupsPanel)
        mainPanel.remove(userPanel)
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