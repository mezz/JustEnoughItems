package mezz.jei.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.language.ModFileScanData;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.util.Log;
import org.objectweb.asm.Type;

public final class AnnotatedInstanceUtil {
	private AnnotatedInstanceUtil() {

	}

	public static List<IModPlugin> getModPlugins() {
		return getInstances(JEIPlugin.class, IModPlugin.class);
	}

	@SuppressWarnings("SameParameterValue")
	private static <T> List<T> getInstances(Class annotationClass, Class<T> instanceClass) {
		List<ModFileScanData> scanData = ModList.get().getAllScanData();
		final List<String> pluginClassNames = scanData.stream()
			.map(ModFileScanData::getAnnotations).flatMap(Collection::stream)
			.filter(a -> Objects.equals(a.getClassType(), Type.getType(annotationClass)))
			.map(ModFileScanData.AnnotationData::getMemberName)
			.collect(Collectors.toList());
		List<T> instances = new ArrayList<>();
		for (String className : pluginClassNames) {
			try {
				Class<?> asmClass = Class.forName(className);
				Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
				T instance = asmInstanceClass.newInstance();
				instances.add(instance);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | LinkageError e) {
				Log.get().error("Failed to load: {}", className, e);
			}
		}
		return instances;
	}
}
