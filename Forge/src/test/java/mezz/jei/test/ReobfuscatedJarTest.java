package mezz.jei.test;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks JEI interface implementations in the shipped jar using Java reflection. */
public class ReobfuscatedJarTest {
	@Test
	public void concreteClassesImplementPackagedInterfaces() throws IOException, ClassNotFoundException {
		String jarPath = System.getProperty("jei.test.packagedJar");
		String classpath = System.getProperty("jei.test.packagedClasspath");
		assertNotNull(jarPath, "Run through :Forge:test to check the reobfuscated jar");
		assertNotNull(classpath);
		List<URL> urls = new ArrayList<>();
		// Prefer shipped JEI classes over the development classes on the test classpath.
		urls.add(new File(jarPath).toURI().toURL());
		for (String entry : classpath.split(File.pathSeparator)) {
			urls.add(new File(entry).toURI().toURL());
		}
		List<String> failures = new ArrayList<>();
		try (URLClassLoader loader = new URLClassLoader(urls.toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
			JarFile jar = new JarFile(jarPath)
		) {
			List<String> names = jar.stream()
				.map(entry -> entry.getName())
				.filter(name -> name.startsWith("mezz/jei/") && name.endsWith(".class"))
				.map(name -> name.substring(0, name.length() - ".class".length()).replace('/', '.'))
				.toList();
			assertFalse(names.isEmpty(), "The packaged jar must contain JEI classes");
			for (String name : names) {
				Class<?> type = Class.forName(name, false, loader);
				if (!Modifier.isAbstract(type.getModifiers())) {
					for (var method : type.getMethods()) {
						// Minecraft dependencies use development names here, so check JEI's own contracts.
						if (method.getDeclaringClass().getName().startsWith("mezz.jei.") && Modifier.isAbstract(method.getModifiers())) {
							failures.add(name + " does not implement " + method);
						}
					}
				}
			}
		}
		failures.sort(String::compareTo);
		assertTrue(failures.isEmpty(), () -> "Broken method linkage in " + jarPath + ":\n" + String.join("\n", failures));
	}
}
