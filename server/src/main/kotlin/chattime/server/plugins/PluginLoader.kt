/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package chattime.server.plugins

import chattime.server.saveProperties
import chattime.server.ChatServer
import chattime.api.event.EventType
import chattime.api.event.PluginLoadEvent
import chattime.api.plugin.LoadOrder
import chattime.api.plugin.Plugin
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.Manifest

class PluginLoader(private val server: ChatServer)
{
    private val pluginLoadList: ArrayList<Plugin> = ArrayList()
    private val mPlugins: ArrayList<Plugin> = ArrayList()

    val plugins: List<Plugin>
        get() = mPlugins

    internal fun findPluginsFromDirectory(directory: String)
    {
        val path = Paths.get(directory)

        if (Files.notExists(path))
            Files.createDirectory(path)

        val stream = Files.newDirectoryStream(path)

        stream.forEach {
            if (it.toString().endsWith(".jar"))
            {
                val classLoader = URLClassLoader(arrayOf(it.toUri().toURL()), this::class.java.classLoader)

                val url = classLoader.findResource("META-INF/MANIFEST.MF")
                val manifest = Manifest(url.openStream())
                val attributes = manifest.mainAttributes
                val pluginClassesString = attributes.getValue("Plugin-Classes")

                val pluginClasses =
                    if (pluginClassesString.contains(';'))
                        pluginClassesString.split(';')
                    else
                        listOf(pluginClassesString)

                pluginClasses.forEach {
                    val pluginClass = Class.forName(it, true, classLoader)

                    addPlugin(constructPlugin(pluginClass))
                }
            }
        }
    }

    fun addPlugin(plugin: Plugin) = pluginLoadList.add(plugin)
    fun addPlugin(className: String) = addPlugin(constructPlugin(Class.forName(className)))

    internal fun loadPlugins()
    {
        fun doesLoadOrderConflict(p1: Plugin, p2: Plugin): Boolean
        {
            // Before
            if (p1.loadOrder.any { it is LoadOrder.Before && it.id == p2.id} &&
                p2.loadOrder.any { it is LoadOrder.Before && it.id == p1.id})
                return true

            // After
            if (p1.loadOrder.any { it is LoadOrder.After && it.id == p2.id} &&
                p2.loadOrder.any { it is LoadOrder.After && it.id == p1.id})
                return true

            return false
        }

        val sortedLoadList = pluginLoadList.sortedWith(Comparator { p1, p2 ->
            if (doesLoadOrderConflict(p1, p2))
            {
                println("Plugin load orders conflict: ${p1.id}, ${p2.id}")
                pluginLoadList.removeAll(listOf(p1, p2))
            }

            if (p1.loadOrder.any { it is LoadOrder.Before && it.id == p2.id } ||
                p2.loadOrder.any { it is LoadOrder.After && it.id == p1.id })
                return@Comparator -1
            else if (p1.loadOrder.any { it is LoadOrder.After && it.id == p2.id } ||
                p2.loadOrder.any { it is LoadOrder.Before && it.id == p1.id })
                return@Comparator 1

            return@Comparator 0
        })

        val disabledMarkings = ArrayList<Plugin>()

        mainPlugins@ for (plugin in sortedLoadList)
        {
            val pluginProps = server.getPluginProperties(plugin)

            if (!pluginProps["enabled", "true"].toBoolean())
            {
                println("Plugin ${plugin.id} is disabled, skipping loading...")
                disabledMarkings += plugin
                continue
            }

            for (requiredPlugin in plugin.loadOrder.filter { it.isRequired })
            {
                if (!sortedLoadList.any { it.id == requiredPlugin.id }
                    || disabledMarkings.any { it.id == requiredPlugin.id })
                {
                    println("Required plugin ${requiredPlugin.id} for ${plugin.id} not found, skipping loading...")
                    continue@mainPlugins
                }
            }

            mPlugins.add(plugin)
            server.eventBus.subscribe(EventType.pluginLoad) {
                if (it.plugin == plugin) plugin.load(it)
            }

            plugin.load(PluginLoadEvent(server, plugin))

            // Save the initial properties after the plugins have been loaded
            saveProperties()
        }
    }

    private fun constructPlugin(pluginClass: Class<*>): Plugin
    {
        if (pluginClass.interfaces.contains(Plugin::class.java))
        {

            return pluginClass.constructors.first {
                it.parameters.isEmpty()
            }.newInstance() as Plugin
        }
        else throw IllegalArgumentException("pluginClass does not extend Plugin!")
    }
}
