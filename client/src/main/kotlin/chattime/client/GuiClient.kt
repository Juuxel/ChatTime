package chattime.client

import java.awt.BorderLayout
import java.awt.Dimension
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
            var answer: String = "no."

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
    val textArea = JTextArea()
    val inputField = JTextField()

    val serverScanner = Scanner(server.inputStream, Charsets.UTF_8.name())                    // This is why I
    val serverWriter = PrintWriter(server.outputStream.bufferedWriter(Charsets.UTF_8), true)  // don't like Windows

    Thread({ guiListen(serverScanner, textArea) }).start()

    textArea.isEditable = false

    inputField.addActionListener {
        serverWriter.println(inputField.text)
        inputField.text = ""
    }
    inputField.requestFocusInWindow()

    panel.add(JScrollPane(textArea), BorderLayout.CENTER)
    panel.add(inputField, BorderLayout.SOUTH)

    frame.size = Dimension(640, 440)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.contentPane = panel

    return frame
}

fun guiListen(scanner: Scanner, textArea: JTextArea)
{
    try
    {
        do
        {
            val input = scanner.nextLine()

            textArea.append("$input\n")

            // https://stackoverflow.com/questions/8789371/java-jtextpane-jscrollpane-de-activate-automatic-scrolling

        } while (input != null)
    }
    catch (e: Exception)
    {
        guiError("Disconnected from the server.")
        System.exit(0)
    }
}

fun guiError(msg: String) = JOptionPane.showMessageDialog(null, msg, "ChatTime", JOptionPane.ERROR_MESSAGE)
