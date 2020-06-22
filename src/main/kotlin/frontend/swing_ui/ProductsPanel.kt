package frontend.swing_ui

import db.entities.Criterion
import db.entities.Product
import frontend.HttpClientLogic
import frontend.swing_ui.ProductsPanel.UpDown.Down
import frontend.swing_ui.ProductsPanel.UpDown.Up
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.table.AbstractTableModel


class ProductsPanel(private val client: HttpClientLogic, private val parent: JFrame) : JPanel() {
    companion object {
        private const val SIZE: Int = 10
        private val colNames = arrayOf("Id", "Price", "Name", "Number", "Group")
    }

    enum class UpDown { Up, Down }

    /*Swing elements*/
    private val queryInput = JTextField(10)
    private val lowerBound = JTextField(10)
    private val upperBound = JTextField(10)
    private var pageNum: JLabel = JLabel()
    private var table = createTable()

    /*Info*/
    private var prods = ArrayList<Product>(25)
    private val criterion = Criterion()
    private var page = 0

    init {
        layout = BorderLayout(5, 5)
        add(createQueryPanel(), BorderLayout.WEST)
        add(createPagePanel(), BorderLayout.SOUTH)
        add(JScrollPane(table), BorderLayout.CENTER)
    }

    private fun updatePageCount(upDown: UpDown) {
        when (upDown) {
            Up -> page++
            Down -> page = (--page).coerceAtLeast(0)
        }
        refreshTable()
    }

    private fun refreshTable() {
        pageNum.text = page.toString()
        try {
            prods = client.loadProducts(page, SIZE, criterion)
            table.updateUI()
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(this, e.message, "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    /*
     * Bulky,
     * Ugly,
     * and Massive create methods.
     * Extracting them to a separate classes would be overkill
     * So I decided to keep'em here where I can reach
     * for all the interconnected variables more easily
     */

    private fun createPagePanel(): JPanel {
        return JPanel().apply {
            layout = FlowLayout(FlowLayout.CENTER)

            add(JButton("<").apply {
                addActionListener { updatePageCount(Down) }
            })

            add(JLabel(page.toString()).apply {
                font = font.deriveFont(18f)
            })

            add(JButton(">").apply {
                addActionListener { updatePageCount(Up) }
            })
        }
    }

    private fun createQueryPanel(): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JLabel("Query:"))
            add(queryInput)
            add(JLabel("Lower bound:"))
            add(lowerBound)
            add(JLabel("Upper bound:"))
            add(upperBound)
            add(Box.createVerticalStrut(5))

            add(JButton("Load products").apply {
                addActionListener {
                    val (lower, upper) = try {
                        val lower = if (lowerBound.text.isBlank()) {
                            null
                        } else lowerBound.text.toDouble()
                        val upper = if (upperBound.text.isBlank()) {
                            null
                        } else upperBound.text.toDouble()
                        lower to upper
                    } catch (e: Exception) {
                        JOptionPane.showMessageDialog(this, "Please input correct numeric bounds", "Error", JOptionPane.ERROR_MESSAGE)
                        return@addActionListener
                    }

                    criterion.query(queryInput.text)
                            .lower(lower)
                            .upper(upper)
                    refreshTable()
                }
            })
        }
    }

    private fun createTable(): JTable {
        return JTable(object : AbstractTableModel() {
            override fun getRowCount() = prods.size
            override fun getColumnCount() = 5
            override fun getColumnName(i: Int) = colNames[i]

            override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
                return when (columnIndex) {
                    0 -> prods[rowIndex].id
                    1 -> prods[rowIndex].price
                    2 -> prods[rowIndex].name
                    3 -> prods[rowIndex].number
                    4 -> prods[rowIndex].groupId
                    else -> null
                }
            }
        }).apply {
            addMouseListener(object : MouseAdapter() {
                override fun mousePressed(mouseEvent: MouseEvent) {
                    val table = mouseEvent.source as JTable
                    if (table.selectedRow != -1) {
                        val row = table.selectedRow
                        val id = table.getValueAt(row, 0) as Int
                        ProductDialog(this@ProductsPanel.parent, client, id)
                    }
                }
            })
        }
    }

}
