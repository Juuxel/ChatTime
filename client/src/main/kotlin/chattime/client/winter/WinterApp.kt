/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client.winter

import javafx.scene.control.Alert
import javafx.scene.control.TextInputDialog
import javafx.stage.Stage
import tornadofx.*
import java.net.Socket

var connectionHandler: () -> ConnectionHandler = {
    throw IllegalStateException()
}
    private set

class WinterApp : App(WinterView::class, WinterStyle::class)
{
    override fun start(stage: Stage)
    {
        try
        {
            val address = TextInputDialog().apply {
                contentText = "Address and port"
                title = "Connect to server"
                headerText = title
                graphic = imageview("icons/serverConnect.png")
            }.showAndWait().get()

            val split = address.split(':')

            val handler = ConnectionHandler(Socket(split[0], split[1].toInt()))

            connectionHandler = { handler }
        }
        catch (e: Exception)
        {
            Alert(Alert.AlertType.ERROR).apply {
                contentText = e.message
                title = "An error happened"
                headerText = "Error: ${e::class.simpleName}"
                graphic = imageview("icons/serverConnectFailed.png")
            }.showAndWait()

            return
        }

        super.start(stage)
    }
}
