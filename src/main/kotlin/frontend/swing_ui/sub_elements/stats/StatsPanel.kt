import frontend.http.HttpClientLogic
import frontend.swing_ui.ClientApp
import frontend.swing_ui.sub_elements.stats.GroupStatsDialog
import frontend.swing_ui.utils.UiUtils.al
import javax.swing.*

class StatsPanel(private val client: HttpClientLogic, private val parent: ClientApp) : JPanel() {

    private val total = JLabel("-")

    init {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JLabel("Total worth:").al())
            add(total.al())
            add(Box.createVerticalStrut(5))
            add(createRefreshButton())
            add(Box.createVerticalStrut(5))
            add(createGroupStatsButton())
        })

    }

    private fun createGroupStatsButton(): JButton {
        val button = JButton("Group stats")
        button.al()

        button.addActionListener {
            GroupStatsDialog(this.parent, client)
        }
        return button
    }

    private fun createRefreshButton(): JButton {
        val button = JButton("Refresh")
        button.al()

        button.addActionListener {
            total.text = client.totalWorth().toString()
        }
        return button
    }

}