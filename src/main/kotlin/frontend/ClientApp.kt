package frontend

import java.awt.Dimension
import javax.swing.*
import javax.swing.BoxLayout
import javax.swing.JOptionPane.ERROR_MESSAGE
import javax.swing.JOptionPane.INFORMATION_MESSAGE


class ClientApp(url: String) : JFrame("Client App") {

    private val loginPanel = createLoginPanel()
    private val productsTable = JPanel()
    private val groupTable = JPanel()

    private val mainPanel = JTabbedPane().apply {
        addTab("Login", loginPanel)
        addTab("Products", productsTable)
        addTab("Groups", groupTable)
    }

    val client = HttpClientLogic(url)

    init {
        minimumSize = Dimension(400, 400)
        defaultCloseOperation = EXIT_ON_CLOSE
        contentPane = mainPanel


        setLocationRelativeTo(null)
        pack()
        isVisible = true
    }


    private fun createLoginPanel(): JPanel {
        return JPanel().apply {
            add(JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                add(JLabel("Login"))
                val login = JTextField(20)
                add(login)

                add(JLabel("Password"))
                val password = JPasswordField(20)
                add(password)

                add(JButton("Log in!").apply {
                    addActionListener {
                        try {
                            val loginResponse = client.sendLogin(
                                    login.text,
                                    String(password.password)
                            )
                            JOptionPane.showMessageDialog(this, "Successfully logged in:\nYour role: ${loginResponse.role}", "Success", INFORMATION_MESSAGE)
                        } catch (e: Exception) {
                            JOptionPane.showMessageDialog(this, e, "Error", ERROR_MESSAGE)
                        }
                    }
                })
            })


        }
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ClientApp("http://localhost:8080/")
        }
    }
}