package frontend.swing_ui

import frontend.HttpClientLogic
import javax.swing.*

class LoginPanel(private val client: HttpClientLogic) : JPanel() {

    init {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JLabel("Login"))
            val login = JTextField("login", 20)
            add(login)

            add(JLabel("Password"))
            val password = JPasswordField("password", 20)
            add(password)
            add(Box.createVerticalStrut(5))

            add(JButton("Log in!").apply {
                addActionListener {
                    try {
                        val loginResponse = client.sendLogin(
                                login.text,
                                String(password.password)
                        )
                        JOptionPane.showMessageDialog(this, "Successfully logged in:\nYour role: ${loginResponse.role}", "Success", JOptionPane.INFORMATION_MESSAGE)
                    } catch (e: Exception) {
                        JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE)
                    }
                }
            })
        })

    }
}