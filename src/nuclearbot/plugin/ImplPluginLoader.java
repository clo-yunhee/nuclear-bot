package nuclearbot.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import nuclearbot.builtin.DummyPlugin;
import nuclearbot.utils.Config;
import nuclearbot.utils.Logger;

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
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Implementation of the plugin loader.
 */
public class ImplPluginLoader implements PluginLoader {

	private static final String CONFIG_DELIMITER = ":>!";
	
	private static final String BUILTIN_PACKAGE_NAME = DummyPlugin.class.getPackage().getName();
	
	private final ClassLoader m_defaultClassLoader;
	
	private final String[] m_builtinPlugins;
	
	private Plugin m_plugin;
	
	public ImplPluginLoader()
	{
		m_defaultClassLoader = ImplPluginLoader.class.getClassLoader();
		
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

		// look for built-in plugins in the 'builtin' package
		final List<String> classes = new ArrayList<String>(10);
		try
		{
			final String path = BUILTIN_PACKAGE_NAME.replace('.', '/');
			final Enumeration<URL> resources = m_defaultClassLoader.getResources(path);
			while (resources.hasMoreElements())
			{
				final URL resource = resources.nextElement();
				classes.addAll(findPlugins(new File(resource.toURI()), BUILTIN_PACKAGE_NAME));
			}
		}
		catch (IOException | URISyntaxException e)
		{
			Logger.printStackTrace(e);
		}
		m_builtinPlugins = classes.toArray(new String[0]);
	}
	
	private List<String> findPlugins(final File dir, final String packageName)
	{
		final List<String> classes = new ArrayList<String>();
		if (dir.exists())
		{
			final File[] files = dir.listFiles();
			for (final File file : files)
			{
				final String name = file.getName();
				if (file.isDirectory() && !name.contains("."))
				{
					classes.addAll(findPlugins(file, packageName + '.' + name));
				}
				else if (name.endsWith(".class"))
				{
					try
					{
						final String className = packageName + '.' + name.substring(0, name.length() - 6);
						final Class<?> classFound = Class.forName(className, false, m_defaultClassLoader);
						if (Modifier.isPublic(classFound.getModifiers())) // must be public
						{
							final Class<?>[] interfaces = classFound.getInterfaces();
							boolean isPlugin = false; // must implement Plugin
							for (Class<?> impl : interfaces)
							{
								if (impl == Plugin.class)
								{
									isPlugin = true;
									break;
								}
							}
							if (isPlugin)
							{
								Logger.info("(ploader) Built-in plugin found: " + classFound.getName());
								classes.add(className);
							}
						}
					}
					catch (ClassNotFoundException e)
					{
						Logger.printStackTrace(e);
					}
				}
			}
		}
		return classes;
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
	
	private boolean loadPlugin(final String className, final ClassLoader classLoader)
	{
		final Class<?> pluginClass;
		try
		{
			pluginClass = Class.forName(className, true, classLoader);
		}
		catch (ClassNotFoundException e)
		{
			Logger.error("(ploader) Class " + className + " is not loaded.");
			Logger.printStackTrace(e);
			return false;
		}
		
		boolean success;
		
		// check it implements Plugin
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
		if (!(success = isPlugin))
		{
			Logger.error("(ploader) Class " + className + " must implement " + Plugin.class.getName() + " with a nullary constructor.");
		}
		else
		{
			// finally load it
			try
			{
				final Constructor<?> ctor = pluginClass.getDeclaredConstructor();
				ctor.setAccessible(true);
				m_plugin = (Plugin) ctor.newInstance();
			}
			catch (InstantiationException e)
			{
				Logger.error("(ploader) Class " + className + " must not be abstract or interface.");
				Logger.printStackTrace(e);
				success = false;
			}
			catch (NoSuchMethodException | IllegalArgumentException e)
			{
				Logger.error("(ploader) Class " + className + " must have a nullary constructor.");
				Logger.printStackTrace(e);
				success = false;
			}
			catch (IllegalAccessException | SecurityException | InvocationTargetException e)
			{
				success = false;
			}
		}
		return success;
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
	public Plugin getPlugin()
	{
		return m_plugin;
	}

	@Override
	public boolean loadPlugin(final String className)
	{
		final boolean success;
		if (success = loadPlugin(className, m_defaultClassLoader))
		{
			writeConfig(CONFIG_DELIMITER + className);
		}
		return success;
	}
	
	@Override
	public boolean loadPlugin(final File file, final String className)
	{
		final ClassLoader classLoader = getJarLoader(file);
		final boolean success;
		if (success = (classLoader != null && loadPlugin(className, classLoader)))
		{
			writeConfig(file.getAbsolutePath() + CONFIG_DELIMITER + className);
		}
		return success;
	}

	@Override
	public boolean loadPlugin(final File file)
	{
		boolean success;
		JarFile jar = null;
		Reader in = null;
		try
		{
			jar = new JarFile(file);
			final JarEntry entry = jar.getJarEntry("plugininfo");
			if (entry != null)
			{
				in = new InputStreamReader(jar.getInputStream(entry));
				final StringBuffer sb = new StringBuffer();
				
				final char[] buffer = new char[32];
				while (in.read(buffer) != -1)
				{
					sb.append(buffer);
				}
				
				final String className = sb.toString();
				final ClassLoader classLoader = getJarLoader(file);
				if (success = (classLoader != null && loadPlugin(className, classLoader)))
				{
					writeConfig(file.getAbsolutePath() + CONFIG_DELIMITER + className);
				}
			}
			else
			{
				Logger.error("(ploader) Jar file \"" + file.getAbsolutePath() + "\" didn't contain a plugininfo file.");
				success = false;
			}
		}
		catch (IOException e)
		{
			Logger.error("(ploader) I/O exception while reading plugininfo file in jar file \"" + file.getAbsolutePath() + "\":");
			Logger.printStackTrace(e);
			success = false;
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e) {}
			}
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
