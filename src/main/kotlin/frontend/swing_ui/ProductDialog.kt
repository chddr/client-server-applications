package frontend.swing_ui

import frontend.HttpClientLogic
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*

class ProductDialog(owner: Frame, private val client: HttpClientLogic, id: Int) : JDialog(owner, "Product", true) {

    private val idInput: JTextField
    private val nameInput: JTextField
    private val numberInput: JTextField
    private val priceInput: JSpinner
    private val groupInput: JTextField


    init {
        val product = client.loadProduct(id)

        idInput = JTextField(product.id.toString()).apply {
            isEditable = false
        }
        nameInput = JTextField(product.name)
        numberInput = JTextField(product.number.toString())
        priceInput = JSpinner(SpinnerNumberModel(product.price, 0.0, 10_000.0, 0.1))
        groupInput = JTextField(product.groupId.toString())



        add(createPanel())

        minimumSize = Dimension(300,300)
        setLocationRelativeTo(null)
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        pack()
        isVisible = true
    }

    private fun createPanel(): JPanel {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            add(JLabel("Id:"))
            add(idInput)
            add(JLabel("Name:"))
            add(nameInput)
            add(JLabel("Number:"))
            add(numberInput)
            add(JLabel("Price:"))
            add(priceInput)
            add(JLabel("Group:"))
            add(groupInput)
        }
    }
}