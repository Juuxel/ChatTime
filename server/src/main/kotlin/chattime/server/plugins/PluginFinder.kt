package chattime.server.plugins

import chattime.server.ChatServer
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.Manifest

class PluginFinder(private val server: ChatServer)
{
    fun findPluginsFromDirectory(directory: String)
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
                    val pluginClass = Class.forName(it).kotlin

                    if (pluginClass.java.interfaces.contains(Plugin::class.java))
                    {
                        val plugin = pluginClass.constructors.first {
                            it.parameters.isEmpty()
                        }.call() as Plugin

                        server.addPlugin(plugin)
                    }
                }
            }
        }
    }
}
