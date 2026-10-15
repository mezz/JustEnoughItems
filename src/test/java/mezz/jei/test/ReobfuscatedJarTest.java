package mezz.jei.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/** Checks JEI interface implementations in the shipped jar using Java reflection. */
public class ReobfuscatedJarTest {
	@Test
	public void concreteClassesImplementPackagedInterfaces() throws IOException, ClassNotFoundException {
		String jarPath = System.getProperty("jei.test.packagedJar");
		String classpath = System.getProperty("jei.test.packagedClasspath");
		assertNotNull("Run through :test to check the reobfuscated jar", jarPath);
		assertNotNull(classpath);
		List<URL> urls = new ArrayList<>();
		// Prefer shipped JEI classes over the development classes on the test classpath.
		urls.add(new File(jarPath).toURI().toURL());
		for (String entry : classpath.split(File.pathSeparator)) {
			urls.add(new File(entry).toURI().toURL());
		}
		List<String> failures = new ArrayList<>();
		try (URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader().getParent());
			JarFile jar = new JarFile(jarPath)
		) {
			List<String> names = jar.stream()
				.map(entry -> entry.getName())
				.filter(name -> name.startsWith("mezz/jei/") && name.endsWith(".class"))
				.map(name -> name.substring(0, name.length() - ".class".length()).replace('/', '.'))
				.collect(Collectors.toList());
			assertFalse("The packaged jar must contain JEI classes", names.isEmpty());
			for (String name : names) {
				Class<?> type = Class.forName(name, false, loader);
				if (!Modifier.isAbstract(type.getModifiers())) {
					for (Method method : type.getMethods()) {
						// Minecraft dependencies use development names here, so check JEI's own contracts.
						if (method.getDeclaringClass().getName().startsWith("mezz.jei.") && Modifier.isAbstract(method.getModifiers())) {
							failures.add(name + " does not implement " + method);
						}
					}
				}
			}
		}
		failures.sort(String::compareTo);
		assertTrue("Broken method linkage in " + jarPath + ":\n" + String.join("\n", failures), failures.isEmpty());
	}
}
