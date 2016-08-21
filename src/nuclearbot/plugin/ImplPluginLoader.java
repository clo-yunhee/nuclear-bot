package nuclearbot.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import nuclearbot.builtin.DummyPlugin;
import nuclearbot.util.Config;
import nuclearbot.util.Logger;

/*
 * Copyright (C) 2016 NuclearCoder
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
 * Implementation of the plugin loader.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ImplPluginLoader implements PluginLoader {

	private static final String CONFIG_DELIMITER = "!/";
	
	private static final String BUILTIN_PACKAGE_NAME = DummyPlugin.class.getPackage().getName();
	private static final String BUILTIN_PACKAGE_PATH = BUILTIN_PACKAGE_NAME.replace('.', '/');
	
	private final ClassLoader m_defaultClassLoader;
	
	private final String[] m_builtinPlugins;
	
	private JavaPlugin m_plugin;
	
	public ImplPluginLoader()
	{
		m_defaultClassLoader = ImplPluginLoader.class.getClassLoader();
		m_plugin = new ClasspathPlugin(new DummyPlugin(), DummyPlugin.class.getName());

		// look for built-in plugins in the 'builtin' package
		final List<String> classes = new ArrayList<String>(5);
		try
		{
			final URL resource = m_defaultClassLoader.getResource(BUILTIN_PACKAGE_PATH);
			if(resource.toString().startsWith("jar:"))
			{
				classes.addAll(findPluginsJar(resource, BUILTIN_PACKAGE_PATH));
			}
			else
			{
				classes.addAll(findPluginsDir(new File(resource.toURI()), BUILTIN_PACKAGE_PATH));
			}
		}
		catch (URISyntaxException e)
		{
			Logger.printStackTrace(e);
		}
		m_builtinPlugins = classes.toArray(new String[0]);
		
		// load the last loaded plugin
		final String lastLoadedPlugin = Config.get("last_plugin");
		if (lastLoadedPlugin == null || lastLoadedPlugin.isEmpty())
		{
			loadPlugin(DummyPlugin.class.getName());
		}
		else if (lastLoadedPlugin.startsWith(CONFIG_DELIMITER))
		{
			loadPlugin(lastLoadedPlugin.substring(CONFIG_DELIMITER.length()));
		}
		else
		{
			loadPlugin(new File(lastLoadedPlugin));
		}
	}
	
	private List<String> findPluginsJar(final URL resource, String packageName)
	{
		final List<String> classes = new ArrayList<String>();
		final String packagePath = packageName.replace('.', '/');
		try
		{
			final String filename = resource.getPath().replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
			final JarFile jarFile = new JarFile(filename);
			final Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements())
			{
				final JarEntry entry = entries.nextElement();
				final String entryName = entry.getName();
				if (entryName.startsWith(packagePath) && entryName.endsWith(".class"))
				{
					final String className = entryName.substring(0, entryName.length() - 6).replace('/', '.').replace('\\', '.');
					try
					{
						final Class<?> classFound = Class.forName(className, false, m_defaultClassLoader);
						final String classError = isPluginClass(classFound);
						if (classError == null)
						{
							Logger.info("(ploader) Built-in plugin found: " + classFound.getName());
							classes.add(className);
						}
					}
					catch (ClassNotFoundException e)
					{
						Logger.printStackTrace(e);
					}
				}
			}
			jarFile.close();
		}
		catch (IOException e)
		{
			Logger.error("(ploader) Exception while searching for built-in plugins:");
			Logger.printStackTrace(e);
		}
		
		return classes;
	}
	
	private List<String> findPluginsDir(final File directory, final String packageName)
	{
		final List<String> classes = new ArrayList<String>();
		final File[] files = directory.listFiles();
		for (final File file : files)
		{
			final String filename = file.getName();
			if (file.isDirectory())
			{
				classes.addAll(findPluginsDir(file, packageName + '.' + file.getName()));
			}
			else if (filename.endsWith(".class"))
			{
				final String filePath = packageName + '/' + filename;
				// remove the .class extension
				final String className = filePath.substring(0, filePath.length() - 6).replace('/', '.');
				try
				{
					final Class<?> classFound = Class.forName(className, false, m_defaultClassLoader);
					final String classError = isPluginClass(classFound);
					if (classError == null)
					{
						Logger.info("(ploader) Built-in plugin found: " + classFound.getName());
						classes.add(className);
					}
				}
				catch (ClassNotFoundException e)
				{
					Logger.printStackTrace(e);
				}
			}
		}
		return classes;
	}
	
	// returns a String with the error, or null if it is a plugin
	private String isPluginClass(final Class<?> pluginClass)
	{
		final String className = pluginClass.getName();
		
		// check if it implements Plugin
		final Class<?>[] interfaces = pluginClass.getInterfaces();
		boolean isPlugin = false;
		for (Class<?> impl : interfaces)
		{
			if (impl == Plugin.class)
			{
				isPlugin = true;
				break;
			}
		}
		if (!isPlugin)
		{
			return "Class " + className + " must implement " + Plugin.class.getName() + " with a nullary constructor.";
		}
		else
		{
			final int mod = pluginClass.getModifiers();
			if (Modifier.isAbstract(mod) || Modifier.isInterface(mod))
			{
				return "Class " + className + " must not be abstract or interface.";
			}
			try
			{
				if (Modifier.isPublic(mod))
				{
					pluginClass.getDeclaredConstructor();
					return null;
				}
				return "Class " + className + " must be public.";
			}
			catch (NoSuchMethodException e)
			{
				return "Class " + className + " must have a nullary constructor.";
			}
		}
	}
	
	private ClassLoader getJarLoader(final File file)
	{
		try
		{
			return new URLClassLoader(new URL[] { file.toURI().toURL() });
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}
	
	private Plugin loadPlugin(final String className, final ClassLoader classLoader)
	{
		Plugin plugin = null;
		try
		{
			final Class<?> pluginClass = Class.forName(className, true, classLoader);
			final String classError = isPluginClass(pluginClass);
			if (classError != null)
			{
				Logger.error("(ploader) " + classError);
			}
			else
			{
				// finally load it
				try
				{
					final Constructor<?> ctor = pluginClass.getDeclaredConstructor();
					ctor.setAccessible(true);
					plugin = (Plugin) ctor.newInstance();
				}
				catch (InstantiationException | NoSuchMethodException | IllegalArgumentException
						| IllegalAccessException | SecurityException | InvocationTargetException e)
				{
					// We should never reach this block.
				}
			}
		}
		catch (ClassNotFoundException e)
		{
			Logger.error("(ploader) Class " + className + " was not found:");
			Logger.printStackTrace(e);
		}
		return plugin;
	}
	
	private void writeConfig(String value)
	{
		Config.set("last_plugin", value);
	}
	
	@Override
	public String[] getBuiltinPlugins()
	{
		return m_builtinPlugins;
	}
	
	@Override
	public JavaPlugin getPlugin()
	{
		return m_plugin;
	}

	@Override
	public boolean loadPlugin(final String className)
	{
		final Plugin plugin = loadPlugin(className, m_defaultClassLoader);
		final boolean success = (plugin != null);
		if (success)
		{
			m_plugin = new ClasspathPlugin(plugin, className);
			writeConfig(CONFIG_DELIMITER + className);
		}
		return success;
	}
	
	@Override
	public boolean loadPlugin(final File file)
	{
		boolean success;
		final Plugin plugin;
		JarFile jar = null;
		try
		{
			jar = new JarFile(file);
			final JarEntry entry = jar.getJarEntry("plugin.properties");
			if (entry != null)
			{
				final Properties properties = new Properties();
				properties.load(jar.getInputStream(entry));
				
				final String className = properties.getProperty("main");
				if (className != null)
				{
					final ClassLoader classLoader = getJarLoader(file);
					plugin = loadPlugin(className, classLoader);
					success = (plugin != null);
					if (success)
					{
						m_plugin = new JarPlugin(plugin, properties);
						writeConfig(file.getAbsolutePath());
					}
				}
				else
				{
					Logger.error("(ploader) There is no \"main\" entry in the plugin.properties file.");
					success = false;
				}
			}
			else
			{
				Logger.error("(ploader) Jar file \"" + file.getAbsolutePath() + "\" didn't contain a plugin.properties file.");
				success = false;
			}
		}
		catch (IOException e)
		{
			Logger.error("(ploader) I/O exception while reading plugin.properties in jar file \"" + file.getAbsolutePath() + "\":");
			Logger.printStackTrace(e);
			success = false;
		}
		finally
		{
			if (jar != null)
			{
				try
				{
					jar.close();
				}
				catch (IOException e) {}
			}
		}
		return success;
	}
	
}
