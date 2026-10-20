package mezz.jei.test;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks packaged dependencies and guards JEI interfaces against Minecraft remapping collisions. */
public class ReobfuscatedJarTest {
	@Test
	public void packagedLuceneIndexesAndSearches() throws Exception {
		try (URLClassLoader loader = new URLClassLoader(packagedClasspath(), ClassLoader.getPlatformClassLoader())) {
			Class<?> storageType = loader.loadClass("mezz.jei.common.search.LuceneSearchStorage");
			assertEquals(new File(System.getProperty("jei.test.packagedJar")).toURI().toURL(),
				storageType.getProtectionDomain().getCodeSource().getLocation());
			Object storage = storageType.getConstructor().newInstance();
			storageType.getMethod("put", String.class, Object.class).invoke(storage, "cobblestone", "stone");
			storageType.getMethod("put", String.class, Object.class).invoke(storage, "oak planks", "wood");
			List<Object> results = new ArrayList<>();
			Consumer<Collection<Object>> collect = results::addAll;

			// Opening the real index exercises the relocated Lucene codec service providers.
			storageType.getMethod("getSearchResults", String.class, Consumer.class).invoke(storage, "cobble", collect);

			assertEquals(List.of("stone"), results);
		}
	}

	@Test
	public void packagedJeiInterfacesAvoidMinecraftRemappingCollisions() throws Exception {
		String jarPath = System.getProperty("jei.test.packagedJar");
		assertNotNull(jarPath, "Run through :Forge:test to check the reobfuscated jar");
		List<String> failures = new ArrayList<>();
		try (URLClassLoader loader = new URLClassLoader(packagedClasspath(), ClassLoader.getPlatformClassLoader());
			JarFile jar = new JarFile(jarPath)
		) {
			List<String> names = jar.stream()
				.map(entry -> entry.getName())
				.filter(name -> name.startsWith("mezz/jei/") && !name.startsWith("mezz/jei/modshade/") && name.endsWith(".class"))
				.map(name -> name.substring(0, name.length() - ".class".length()).replace('/', '.'))
				.toList();
			assertFalse(names.isEmpty(), "The packaged jar must contain JEI classes");
			for (String name : names) {
				Class<?> type = Class.forName(name, false, loader);
				if (type.isInterface()) {
					Set<String> packagedMethods = new HashSet<>();
					for (Method method : type.getDeclaredMethods()) {
						packagedMethods.add(methodSignature(method));
					}
					Class<?> developmentType = Class.forName(name, false, getClass().getClassLoader());
					// Minecraft override mappings may not propagate consistently to unrelated JEI interfaces.
					// Avoid JEI interface method names that collide with Minecraft methods subject to remapping.
					for (Method method : developmentType.getDeclaredMethods()) {
						if (Modifier.isPublic(method.getModifiers()) && !packagedMethods.contains(methodSignature(method))) {
							failures.add(name + "#" + methodSignature(method) + " is remapped; use a JEI interface method name that does not collide with Minecraft methods");
						}
					}
				} else if (!Modifier.isAbstract(type.getModifiers())) {
					for (Method method : findInterfaceImplementationRisks(type)) {
						failures.add(name + " has no concrete JEI implementation of " + method + "; use a distinct JEI method name instead of relying on Minecraft's mapped methods");
					}
				}
			}
		}
		failures.sort(String::compareTo);
		assertTrue(failures.isEmpty(), () -> "JEI interface methods at risk from Minecraft remapping in " + jarPath + ":\n" + String.join("\n", failures));
	}

	private static String methodSignature(Method method) {
		return method.getName() + MethodType.methodType(method.getReturnType(), method.getParameterTypes()).toMethodDescriptorString();
	}

	private static Set<Method> findInterfaceImplementationRisks(Class<?> type) throws NoSuchMethodException {
		Set<Method> methods = new HashSet<>(List.of(type.getMethods()));
		// This classpath uses Minecraft's development names, which can mask missing
		// packaged overrides in getMethods(). Inspect the interface declarations directly.
		for (Class<?> current = type; current != null; current = current.getSuperclass()) {
			for (Class<?> interfaceType : current.getInterfaces()) {
				methods.addAll(List.of(interfaceType.getMethods()));
			}
		}
		Set<Method> atRisk = new HashSet<>();
		for (Method method : methods) {
			if (method.getDeclaringClass().getName().startsWith("mezz.jei.") && Modifier.isAbstract(method.getModifiers())) {
				Method implementation = type.getMethod(method.getName(), method.getParameterTypes());
				if (Modifier.isAbstract(implementation.getModifiers()) || implementation.getDeclaringClass().getName().startsWith("net.minecraft.")) {
					atRisk.add(method);
				}
			}
		}
		return atRisk;
	}

	private static URL[] packagedClasspath() throws IOException {
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
		return urls.toArray(URL[]::new);
	}
}
