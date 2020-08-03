package mezz.jei.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

public final class AnnotatedInstanceUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private AnnotatedInstanceUtil() {

	}

	public static List<IModPlugin> getModPlugins() {
		return getInstances(JeiPlugin.class, IModPlugin.class);
	}

	@SuppressWarnings("SameParameterValue")
	private static <T> List<T> getInstances(Class<?> annotationClass, Class<T> instanceClass) {
		Type annotationType = Type.getType(annotationClass);
		List<ModFileScanData> allScanData = ModList.get().getAllScanData();
		List<String> pluginClassNames = new ArrayList<>();
		for (ModFileScanData scanData : allScanData) {
			Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
			for (ModFileScanData.AnnotationData a : annotations) {
				if (Objects.equals(a.getAnnotationType(), annotationType)) {
					String memberName = a.getMemberName();
					pluginClassNames.add(memberName);
				}
			}
		}
		List<T> instances = new ArrayList<>();
		for (String className : pluginClassNames) {
			try {
				Class<?> asmClass = Class.forName(className);
				Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
				T instance = asmInstanceClass.newInstance();
				instances.add(instance);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | LinkageError e) {
				LOGGER.error("Failed to load: {}", className, e);
			}
		}
		return instances;
	}
}
