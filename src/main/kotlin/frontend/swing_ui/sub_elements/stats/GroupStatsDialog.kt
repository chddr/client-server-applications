package frontend.swing_ui.sub_elements.stats

import frontend.http.HttpClientLogic
import javax.swing.*
import javax.swing.table.AbstractTableModel

class GroupStatsDialog(parent: JFrame, private var client: HttpClientLogic) : JDialog(parent, "Worth by group", true) {

    init {
        val stats = client
                .loadGroups()
                .map { it to client.groupWorth(it.id) }

        val table = JTable(object : AbstractTableModel() {
            override fun getRowCount() = stats.size
            override fun getColumnCount() = 2
            override fun getColumnName(i: Int) = listOf("Group", "Price")[i]

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
                return when (columnIndex) {
                    0 -> stats[rowIndex].first
                    1 -> stats[rowIndex].second
                    else -> null
                }
            }
        })

        add(JScrollPane(table))

        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        pack()
        isVisible = true
    }
}