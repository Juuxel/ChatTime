package chattime.client

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.PrintWriter
import java.net.ConnectException
import java.net.Socket
import java.util.*
import javax.swing.*

fun guiStart(args: Array<String>)
{
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    try
    {
        val addressWithPort = {
            var answer = "no."

            do
            {
                val input = JOptionPane.showInputDialog(null, "Input the server address in the format <address>:<port>",
                                                        "ChatTime - Enter address", JOptionPane.QUESTION_MESSAGE)

                if (input != null)
                    answer = input
            } while (input == null)

            answer
        }()

        val addressSplit = addressWithPort.split(':')
        val server = Socket(addressSplit[0], addressSplit[1].toInt())
        val frame = guiInit(server)

        frame.isVisible = true
    }
    catch (e: ConnectException)
    {
        guiError("Failed to connect to the server :-(")
    }
}

fun guiInit(server: Socket): JFrame
{
    val frame = JFrame("ChatTime GUI")
    val panel = JPanel(BorderLayout())
    val inputField = JTextField()

    val textListModel = DefaultListModel<String>()
    val textList = JList<String>(textListModel)
    val scrollPane = JScrollPane(textList)

    val serverScanner = Scanner(server.inputStream, Charsets.UTF_8.name())                    // This is why I
    val serverWriter = PrintWriter(server.outputStream.bufferedWriter(Charsets.UTF_8), true)  // don't like Windows

    Thread({ guiListen(serverScanner, textListModel, scrollPane) }).start()

    // TODO Write a popup menu with "Copy" and a timestamp on it
    textList.selectionMode = ListSelectionModel.SINGLE_SELECTION

    inputField.addActionListener {
        serverWriter.println(inputField.text)
        inputField.text = ""
    }
    inputField.addMouseListener(InputFieldMouse(inputField))
    inputField.requestFocusInWindow()

    panel.add(scrollPane, BorderLayout.CENTER)
    panel.add(inputField, BorderLayout.SOUTH)

    frame.size = Dimension(640, 440)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.contentPane = panel

    return frame
}

fun guiListen(scanner: Scanner, model: DefaultListModel<String>, scrollPane: JScrollPane)
{
    try
    {
        do
        {
            val input = scanner.nextLine()

            SwingUtilities.invokeLater { model.addElement(input) }

            // https://stackoverflow.com/questions/8789371/java-jtextpane-jscrollpane-de-activate-automatic-scrolling
            val sb = scrollPane.verticalScrollBar

            if (sb.value + sb.visibleAmount == sb.maximum)
                SwingUtilities.invokeLater {
                    sb.value = sb.maximum
                }
        } while (input != null)
    }
    catch (e: Exception)
    {
        guiError("Disconnected from the server.")
        System.exit(0)
    }
}

fun guiError(msg: String) = JOptionPane.showMessageDialog(null, msg, "ChatTime", JOptionPane.ERROR_MESSAGE)

private class InputFieldMouse(val inputField: JTextField) : MouseAdapter()
{
    val popup = JPopupMenu()

    init
    {
        val selectAll = JMenuItem("Select All")
        val paste = JMenuItem("Paste")

        selectAll.addActionListener { inputField.selectAll() }
        paste.addActionListener { inputField.paste() }

        popup.add(selectAll)
        popup.add(paste)
    }

    override fun mouseClicked(e: MouseEvent?)
    {
        if (e?.button == MouseEvent.BUTTON3)
            popup.show(inputField, e.x, e.y)
    }
}
