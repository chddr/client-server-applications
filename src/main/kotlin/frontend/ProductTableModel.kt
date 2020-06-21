//package frontend
//
//import javax.swing.event.TableModelListener
//import javax.swing.table.TableModel
//
//class ProductTableModel: TableModel {
//
//    override fun addTableModelListener(l: TableModelListener?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getRowCount(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun getColumnName(columnIndex: Int): String {
//        return listOf(
//                "name",
//                "price",
//                "id",
//                "number",
//                "group"
//        )[columnIndex]
//    }
//
//    override fun isCellEditable(rowIndex: Int, columnIndex: Int) = false
//
//    override fun getColumnClass(columnIndex: Int): Class<*> {
//        TODO("Not yet implemented")
//    }
//
//    override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
//        TODO("Not yet implemented")
//    }
//
//    override fun getColumnCount(): Int {
//        TODO("Not yet implemented")
//    }
//
//    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun removeTableModelListener(l: TableModelListener?) {
//        TODO("Not yet implemented")
//    }
//}