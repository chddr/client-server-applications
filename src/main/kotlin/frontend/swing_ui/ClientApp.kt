package frontend.swing_ui

import StatsPanel
import frontend.http.HttpClientLogic
import frontend.swing_ui.sub_elements.LoginPanel
import frontend.swing_ui.sub_elements.groups.GroupsPanel
import frontend.swing_ui.sub_elements.products.ProductsPanel
import frontend.swing_ui.sub_elements.users.UserPanel
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JTabbedPane
import javax.swing.WindowConstants


class ClientApp(url: String) : JFrame("Client App") {

    private val client = HttpClientLogic(url)

    private val loginPanel = LoginPanel(client, this)
    private val productsPanel = ProductsPanel(client, this)
    private val groupsPanel = GroupsPanel(client, this)
    private val userPanel = UserPanel(client, this)
    private val statsPanel = StatsPanel(client, this)

    private val mainPanel = JTabbedPane().apply {
        addTab("Login", loginPanel)
    }


    fun addAdminTabs() {
        addUserTabs()
        mainPanel.addTab("Users", userPanel)
    }

    fun addUserTabs() {
        mainPanel.addTab("Products", productsPanel)
        mainPanel.addTab("Groups", groupsPanel)
        mainPanel.addTab("Stats", statsPanel)
    }

    fun removeTabs() {
        mainPanel.remove(productsPanel)
        mainPanel.remove(groupsPanel)
        mainPanel.remove(userPanel)
        mainPanel.remove(statsPanel)
    }


    init {
        minimumSize = Dimension(400, 350)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        contentPane = mainPanel


        setLocationRelativeTo(null)
        pack()
        isVisible = true
    }

    override fun dispose() {
        super.dispose()
        client.close()
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ClientApp("http://localhost:8080/")
        }
    }
}