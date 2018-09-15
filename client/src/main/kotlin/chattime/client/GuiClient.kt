/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client

import chattime.api.net.Packet
import picocli.CommandLine
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.system.exitProcess

@CommandLine.Command(name = "gui", description = ["launch the gui client"])
internal class GuiClient : Runnable
{
    companion object
    {
        private fun edt(block: () -> Unit)
            = SwingUtilities.invokeLater(block)
    }

    override fun run()
    {
        val connection = createConnection()

        val frame = JFrame().apply {
            val mainView = MessageView()
            val inputField = JTextField().apply {
                addActionListener {
                    connection.toServer(text)
                    text = ""
                }
            }

            connection.handleMessage {
                mainView.add(MessageComponent(it))
            }

            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) = handleExit(connection, this@apply)
            })

            contentPane = JPanel().apply {
                layout = BorderLayout()

                add(JScrollPane(mainView), BorderLayout.CENTER)
                add(inputField, BorderLayout.SOUTH)
            }

            size = Dimension(640, 480)
            defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE

            edt {
                isVisible = true
            }
        }

        connection.handleExit {
            handleExit(connection, frame)
        }
    }

    private fun handleExit(connection: Connection, frame: JFrame): Nothing
    {
        frame.isVisible = false
        frame.dispose()
        connection.close()
        exitProcess(0)
    }

    private fun createConnection(): Connection
    {
        val host = userInput("Host") ?: TODO()
        val port = userInput("Port")?.toInt() ?: TODO()

        return Connection(host, port)
    }

    private fun userInput(message: String): String? = JOptionPane.showInputDialog(message)

    private class MessageView : JPanel()
    {
        init
        {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        override fun add(comp: Component?): Component
        {
            return super.add(comp).also {
                if (comp is MessageComponent)
                    comp.alignmentX = Component.LEFT_ALIGNMENT
            }
        }
    }

    private class MessageComponent(message: Packet.Message) : JLabel()
    {
        init
        {
            val senderHtml =
                if (message.sender != "") "<b>${message.sender}</b> "
                else ""

            val msg = message.message.split('\n').joinToString(separator = "<br>")

            font = Font.decode("${Font.SANS_SERIF} 12")
            text = "<html>$senderHtml$msg"
        }
    }
}
