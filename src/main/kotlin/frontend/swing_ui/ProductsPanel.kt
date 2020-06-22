package frontend.swing_ui

import db.entities.Criterion
import db.entities.Product
import frontend.HttpClientLogic
import frontend.swing_ui.ProductsPanel.UpDown.Down
import frontend.swing_ui.ProductsPanel.UpDown.Up
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*
import javax.swing.table.AbstractTableModel


class ProductsPanel(private val client: HttpClientLogic) : JPanel() {

    private val queryInput = JTextField(10)
    private val lowerBound = JTextField(10)
    private val upperBound = JTextField(10)

    var pageNum: JLabel = JLabel()

    companion object {
        private const val SIZE: Int = 10
        private val colNames = arrayOf("Name", "Price", "Id", "Number", "Group")
    }

    private var prods = ArrayList<Product>(25)
    private val criterion = Criterion()
    private var page = 0


    private var table = JTable(object : AbstractTableModel() {
        override fun getRowCount() = prods.size
        override fun getColumnCount() = 5
        override fun getColumnName(i: Int) = colNames[i]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
            return when (columnIndex) {
                0 -> prods[rowIndex].name
                1 -> prods[rowIndex].price
                2 -> prods[rowIndex].id
                3 -> prods[rowIndex].number
                4 -> prods[rowIndex].groupId
                else -> null
            }
        }
    })

    init {
        layout = BorderLayout()
        add(createQueryPanel(), BorderLayout.WEST)
        add(createPagePanel(), BorderLayout.SOUTH)
        add(table, BorderLayout.CENTER)
    }

    private fun createPagePanel(): JPanel {
        return JPanel().apply {
            layout = FlowLayout(FlowLayout.CENTER)

            add(JButton("<").apply {
                addActionListener { updatePage(Down) }
            })
            pageNum = JLabel(page.toString()).apply {
                font = font.deriveFont(18f)
            }
            add(pageNum)
            add(JButton(">").apply {
                addActionListener { updatePage(Up) }
            })
        }
    }

    enum class UpDown { Up, Down }

    private fun updatePage(upDown: UpDown) {
        when (upDown) {
            Up -> page++
            Down -> page = (--page).coerceAtLeast(0)
        }
        updatePage()
    }

    private fun updatePage() {
        prods = client.loadProducts(page, SIZE, criterion)
        pageNum.text = page.toString()
        table.updateUI()
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


            add(JButton("Load products").apply {
                addActionListener {
                    val (lower, upper) = try {
                        lowerBound.text.toDouble() to upperBound.text.toDouble()
                    } catch (e: Exception) {
                        JOptionPane.showMessageDialog(this, "Please input correct numeric bounds", "Error", JOptionPane.ERROR_MESSAGE)
                        return@addActionListener
                    }

                    criterion.query(queryInput.text)
                            .lower(lower)
                            .upper(upper)
                    updatePage()
                }
            })
        }
    }

}
