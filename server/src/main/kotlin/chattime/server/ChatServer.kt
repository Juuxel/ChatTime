package chattime.server

import chattime.common.formatMessage
import chattime.server.event.*
import chattime.server.plugins.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap

class ChatServer : User
{
    private val threads: ArrayList<ConnectionThread> = ArrayList()
    private val users: ArrayList<User> = arrayListOf(this)
    internal val pluginLoader = PluginLoader(this)
    private val properties = Properties()
    private val propertiesPath = Paths.get("config.properties")

    val plugins: List<Plugin>
        get() = pluginLoader.plugins

    private val pluginPropertiesMap: HashMap<Plugin, PluginProperties> = HashMap()

    // User stuff //

    override val id = "Server"
    override var name = "Server"
    override var isEchoingEnabled
        get() = false
        set(value)
        { /* Fake variable :^D */ }

    init
    {
        pluginLoader.addPlugin(CommandPlugin())
        pluginLoader.addPlugin(AttributesPlugin())

        loadProperties()
    }

    private fun loadProperties()
    {
        // Initialize default values
        // No defaults right now

        // Prevents a NoSuchFileException
        if (Files.notExists(propertiesPath))
            Files.createFile(propertiesPath)

        properties.load(Files.newInputStream(propertiesPath))
    }

    fun addThread(thread: ConnectionThread)
    {
        threads.add(thread)
        users.add(thread)

        plugins.forEach { it.onUserJoin(UserEvent(this, thread)) }

        pushMessage("${thread.name} joined the chat room")
    }

    fun sendPluginMessage(id: String, sender: Plugin, msg: Any)
    {
        plugins.filter { it.id == id }.forEach {
            it.handlePluginMessage(
                PluginMessageEvent(
                    this,
                    sender,
                    msg
                )
            )
        }
    }

    fun getPluginProperties(plugin: Plugin): PluginProperties
        = pluginPropertiesMap.getOrPut(plugin, { PluginProperties(properties, plugin) })

    fun saveProperties()
    {
        properties.store(Files.newOutputStream(propertiesPath), "ChatTime server properties")
    }

    @Synchronized
    fun receiveAndPushMessage(msg: String, user: User)
    {
        val event = MessageEvent(this, msg, user)

        plugins.forEach { it.onMessageReceived(event) }

        if (event.isCanceled) return

        val formattedMessage = "/${user.name}/ ${event.msg}"

        pushMessage(formattedMessage,
                    blacklist = if (user.isEchoingEnabled) emptyList() else listOf(user))
    }

    @Synchronized
    fun pushMessage(msg: String,
                    blacklist: Collection<User> = emptyList(),
                    whitelist: Collection<User> = emptyList())
    {
        if (whitelist.isEmpty())
            users.filterNot { blacklist.contains(it) }.forEach { it.sendMessage(msg) }
        else
            users.filter { whitelist.contains(it) }.forEach { it.sendMessage(msg) }
    }

    override fun sendMessage(msg: String)
    {
        println(formatMessage(msg))
    }
}
