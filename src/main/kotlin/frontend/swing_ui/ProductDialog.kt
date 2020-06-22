package frontend.swing_ui

import frontend.HttpClientLogic
import java.awt.Dimension
import java.awt.Frame
import java.awt.GridLayout
import javax.swing.*

class ProductDialog(owner: Frame, private val client: HttpClientLogic, id: Int) : JDialog(owner, "Product", true) {

    private val idLabel: JLabel = JLabel("Id:")
    private val nameLabel: JLabel = JLabel("Name:")
    private val numberLabel: JLabel = JLabel("Number:")
    private val priceLabel: JLabel = JLabel("Price:")
    private val groupLabel: JLabel = JLabel("Group:")

    private val product = client.loadProduct(id)

    private val idInput = JTextField(product.id.toString()).apply { isEditable = false }
    private val nameInput = JTextField(product.name)
    private val numberInput = JTextField(product.number.toString())
    private val priceInput = JSpinner(SpinnerNumberModel(product.price, 0.0, 10_000.0, 0.1))
    private val groupInput = JTextField(product.groupId.toString())

    init {

        add(createPanel())

        minimumSize = Dimension(200, 200)
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        pack()
        isVisible = true
    }

    private fun createPanel(): JPanel {
        return JPanel().apply {
            layout = GridLayout(5, 2)

            add(idLabel)
            add(idInput)
            add(nameLabel)
            add(nameInput)
            add(numberLabel)
            add(numberInput)
            add(priceLabel)
            add(priceInput)
            add(groupLabel)
            add(groupInput)
        }
    }
}