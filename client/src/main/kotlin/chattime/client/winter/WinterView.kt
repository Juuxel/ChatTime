/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client.winter

import chattime.common.Info
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.event.EventTarget
import javafx.geometry.Insets
import javafx.scene.control.Alert
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class WinterView : View("ChatTime Winter")
{
    private val box = BorderPane()
    override val root = box

    private val messageBox = VBox(3.0).apply {
        padding = Insets(3.0)
    }

    init
    {
        with(box) {
            style {
                fontFamily = "Roboto, Segoe UI, Arial"
            }

            center = borderpane {
                bottom = hbox {
                    val field = textfield {
                        hgrow = Priority.ALWAYS

                        action {
                            connectionHandler().toServer(text)
                            clear()
                        }
                    }

                    button(graphic = imageview("icons/sendMessage.png")) {
                        addClass(WinterStyle.sendButton)

                        action {
                            connectionHandler().toServer(field.text)
                            field.clear()
                        }
                    }
                }

                center = scrollpane {
                    this += messageBox
                }
            }

            top = menubar() {
                menu("App") {
                    item("About ChatTime Winter") {
                        action {
                            alert(Alert.AlertType.INFORMATION, "ChatTime Winter v${Info.version}",
                                  "Made by Juuxel\n"
                                      + "Licensed under MPL\n\n"
                                      + "Open source libraries:\n"
                                      + "- Picocli (GitHub: remkop/picocli)\n"
                                      + "- RxJava and RxKotlin (GitHub: ReactiveX/RxJava, RxKotlin)\n"
                                      + "- TornadoFX (GitHub: edvin/tornadofx)\n\n"
                                      + "Icons from icons8.com")
                        }
                    }

                    item("Close") {
                        action {
                            primaryStage.close()
                            connectionHandler().close()
                        }
                    }
                }
            }

            /*
            left = vbox(3.0) {
                addClass(WinterStyle.sidebar)
                padding = Insets(2.5)

                label("Person 1")
                label("Person 2")
                button("Add test message") {
                    action {
                        messageBox += message("Gibberish", "askdakpodpwakspdwkpdas")
                    }
                }
            }
            */
        }

        connectionHandler().handleMessage { msg ->
            Platform.runLater {
                val parts = {
                    when
                    {
                        msg.startsWith("[") ->
                        {
                            val endIndex = msg.indexOf(']')
                            msg.substring(1, endIndex) to msg.substring(endIndex + 1).trim()
                        }
                        msg.contains(':') ->
                        {
                            val endIndex = msg.indexOf(':')
                            msg.substring(0, endIndex) to msg.substring(endIndex + 1).trim()
                        }
                        else -> "Server" to msg
                    }
                }()

                messageBox += message(parts.first, parts.second)
            }
        }

        connectionHandler().handleExit(Platform::exit)

        primaryStage.onCloseRequest = EventHandler {
            connectionHandler().close()
        }
    }

    private fun EventTarget.message(sender: String, text: String,
                                    isUsersMessage: Boolean = false,
                                    block: (VBox.() -> Unit)? = null): VBox
    {
        return vbox(3.0) {
            label(sender) {
                addClass(WinterStyle.messageSender)
            }
            label(text)

            if (isUsersMessage)
                addClass(WinterStyle.usersMessage)

            addClass(WinterStyle.message)
            block?.invoke(this)
        }
    }
}
