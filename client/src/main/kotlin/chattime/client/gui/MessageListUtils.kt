/* This file is a part of the ChatTime project.
 * ChatTime is distributed under the GNU GPLv3 license.
 * Source code is available at https://github.com/Juuxel/ChatTime.
 */
package chattime.client.gui

import chattime.common.formatTime
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.LocalTime
import java.util.ArrayList
import javax.swing.*

internal typealias MessageWithTime = Pair<String, LocalTime>

internal class MessageListModel : DefaultListModel<String>()
{
    val timeList: List<MessageWithTime>
        get() = mTimeList

    private val mTimeList = ArrayList<MessageWithTime>()

    fun addElement(msg: String, time: LocalTime)
    {
        super.addElement(msg)
        mTimeList.add(msg to time)
    }
}

internal class MessageListMouse(private val list: JList<String>,
                                private val model: MessageListModel) : MouseAdapter()
{
    private val popup = JPopupMenu()
    private val time = JLabel()

    init
    {
        val copy = JMenuItem("Copy")

        copy.addActionListener {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(list.selectedValue), null)
        }

        popup.add(time)
        popup.add(copy)
    }

    // https://stackoverflow.com/a/6007967

    override fun mouseReleased(e: MouseEvent?)
    {
        if (e != null)
            showPopup(e)
    }

    override fun mousePressed(e: MouseEvent?)
    {
        if (e != null)
            showPopup(e)
    }

    private fun showPopup(e: MouseEvent)
    {
        if (e.isPopupTrigger)
        {
            list.selectedIndex = list.locationToIndex(e.point)
            time.text = formatTime(model.timeList[list.selectedIndex].second)
            popup.show(list, e.x, e.y)
        }
    }
}
