package nuclearbot.plugin

import nuclearbot.builtin.DummyPlugin
import nuclearbot.util.Config
import nuclearbot.util.Logger
import java.io.File
import java.io.IOException
import java.lang.reflect.Modifier
import java.net.URISyntaxException
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile

/*
 * Copyright (C) 2017 NuclearCoder
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Implementation of the plugin loader.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ImplPluginLoader : PluginLoader {

    private val defaultClassLoader = ImplPluginLoader::class.java.classLoader

    override val builtinPlugins: Array<String>

    override lateinit var plugin: JavaPlugin

    init {
        // look for built-in plugins in the 'builtin' package
        builtinPlugins = mutableListOf<String>().let {
            try {
                val resource = defaultClassLoader.getResource(BUILTIN_PACKAGE_PATH)
                        ?: throw IllegalStateException("Builtin package is not present.")

                it.addAll(if (resource.toString().startsWith("jar:")) {
                    findPluginsJar(resource, BUILTIN_PACKAGE_PATH)
                } else {
                    findPluginsDir(File(resource.toURI()), BUILTIN_PACKAGE_PATH)
                })
            } catch (e: URISyntaxException) {
                Logger.printStackTrace(e)
            } catch (e: NullPointerException) {
                Logger.printStackTrace(e)
            }
            it.toTypedArray()
        }

        // load the last loaded plugin
        Config["last_plugin", DummyPlugin::class.java.name].run {
            if (isEmpty()) {
                loadPlugin(DummyPlugin::class.java.name)
            } else if (startsWith(CONFIG_DELIMITER)) {
                loadPlugin(substring(CONFIG_DELIMITER.length))
            } else {
                loadPlugin(File(this))
            }
        }
    }

    private fun findPluginsJar(resource: URL, packageName: String): List<String> {
        val classes = mutableListOf<String>()
        val packagePath = packageName.replace('.', '/')
        val filename = resource.path
                .replaceFirst("[.]jar[!].*".toRegex(), ".jar")
                .replaceFirst("file:".toRegex(), "")

        try {
            JarFile(filename).use { jarFile ->
                val entries = jarFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val entryName = entry.name
                    if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                        val className = entryName
                                .substring(0, entryName.length - 6)
                                .replace('/', '.')
                                .replace('\\', '.')
                        try {
                            val classFound = Class.forName(className, false, defaultClassLoader)
                            val classError = isPluginClass(classFound)
                            if (classError == null) {
                                Logger.info("(pLoader) Built-in plugin found: " + classFound.name)
                                classes.add(className)
                            }
                        } catch (e: ClassNotFoundException) {
                            Logger.printStackTrace(e)
                        }

                    }
                }
            }
        } catch (e: IOException) {
            Logger.error("(pLoader) Exception while searching for built-in plugins:")
            Logger.printStackTrace(e)
        }

        return classes
    }

    private fun findPluginsDir(directory: File, packageName: String): List<String> {
        val classes = ArrayList<String>()
        val files = directory.listFiles()
        // not a directory or I/O error
        if (files == null) {
            Logger.error("(pLoader) Error while listing package.")
            return classes
        }

        for (file in files) {
            val filename = file.name
            if (file.isDirectory) {
                classes.addAll(findPluginsDir(file, "$packageName.$filename"))
            } else if (filename.endsWith(".class")) {
                val filePath = "$packageName/$filename"
                // remove the .class extension
                val className = filePath.substring(0, filePath.length - 6)
                        .replace('/', '.')
                try {
                    val classFound = Class.forName(className, false, defaultClassLoader)
                    val classError = isPluginClass(classFound)
                    if (classError == null) {
                        Logger.info("(pLoader) Built-in plugin found: ${classFound.name}")
                        classes.add(className)
                    }
                } catch (e: ClassNotFoundException) {
                    Logger.printStackTrace(e)
                }

            }
        }
        return classes
    }

    // returns a String with the error, or null if it is a plugin
    private fun isPluginClass(pluginClass: Class<*>): String? {
        val className = pluginClass.name

        // check if it implements Plugin
        if (pluginClass.interfaces.none { it == Plugin::class.java }) {
            return "Class $className must implement ${Plugin::class.java.name} with a nullary constructor."
        }

        // check if it is an actual class (not abstract, not interface)
        val mod = pluginClass.modifiers
        if (Modifier.isAbstract(mod) || Modifier.isInterface(mod)) {
            return "Class $className must not be abstract or interface."
        }

        try {
            if (Modifier.isPublic(mod)) {
                pluginClass.getDeclaredConstructor()
                return null
            }
            return "Class $className must be public."
        } catch (e: NoSuchMethodException) {
            return "Class $className must have a nullary constructor."
        }
    }

    private fun getJarLoader(file: File) = URLClassLoader(arrayOf(file.toURI().toURL())) // FIXME: potential leak

    private fun loadPlugin(className: String, classLoader: ClassLoader): Plugin? {
        try {
            val pluginClass = Class.forName(className, true, classLoader)
            val classError = isPluginClass(pluginClass)
            if (classError != null) {
                Logger.error("(pLoader) $classError")
            } else {
                // finally load it
                return pluginClass.getDeclaredConstructor().let {
                    it.isAccessible = true
                    it.newInstance() as Plugin
                }
            }
        } catch (e: ClassNotFoundException) {
            Logger.error("(pLoader) Class $className was not found:")
            Logger.printStackTrace(e)
        }
        return null
    }

    private fun writeConfig(value: String) {
        Config["last_plugin"] = value
    }

    override fun loadPlugin(className: String): Boolean {
        val plugin = loadPlugin(className, defaultClassLoader)
        if (plugin != null) {
            this.plugin = ImplJavaPlugin(plugin, isBuiltin = true)
            writeConfig(CONFIG_DELIMITER + className)
            return true
        }
        return false
    }

    override fun loadPlugin(file: File): Boolean {
        try {
            JarFile(file).use { jar ->
                val entry = jar.getJarEntry("plugin.properties")
                if (entry != null) {
                    val properties = Properties().apply { load(jar.getInputStream(entry)) }
                    val className = properties.getProperty("main")
                    if (className != null) {
                        val classLoader = getJarLoader(file)
                        loadPlugin(className, classLoader)?.let {
                            this.plugin = ImplJavaPlugin(it, isBuiltin = false)
                            writeConfig(file.absolutePath)
                            return true
                        }
                    } else {
                        Logger.error("(pLoader) There is no \"main\" entry in the plugin.properties file.")
                    }
                } else {
                    Logger.error("(pLoader) Jar file \"̂${file.absolutePath}\" didn't contain a plugin.properties file.")
                }
            }
        } catch (e: IOException) {
            Logger.error("(pLoader) I/O exception while reading plugin.properties in jar file \"${file.absolutePath}\":")
            Logger.printStackTrace(e)
        }
        return false
    }

    companion object {
        private const val CONFIG_DELIMITER = "!/"

        private val BUILTIN_PACKAGE_NAME = DummyPlugin::class.java.`package`.name
        private val BUILTIN_PACKAGE_PATH = BUILTIN_PACKAGE_NAME.replace('.', '/')
    }

}
