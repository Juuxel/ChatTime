/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.client.winter

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class WinterStyle : Stylesheet()
{
    companion object Classes
    {
        val message by cssclass()
        val messageSender by cssclass()
        val usersMessage by cssclass()
        val inputPane by cssclass()
        val sendButton by cssclass()

        val sidebar by cssclass()
    }

    init
    {
        sidebar {
            borderWidth += box(right = 3.px, top = 0.px, left = 0.px, bottom = 0.px)
            borderColor += box(Color.LIGHTGRAY)
        }

        select(inputPane, textField) {
            maxWidth = Double.MAX_VALUE.px
        }

        message {
            backgroundColor += Colors.darkColor
            padding = box(.5.em, 1.em)
            backgroundRadius += box(10.px)
            minWidth = 20.em
            maxWidth = 50.em
        }

        usersMessage {
            backgroundColor += Colors.primaryColor
        }

        messageSender {
            fontWeight = FontWeight.BOLD
        }

        sendButton {
            backgroundColor += Color.rgb(0, 0, 0, 0.0)
        }
    }
}
