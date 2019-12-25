package mezz.jei.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

public final class AnnotatedClassFactory {
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<String> pluginClassNames;

	private AnnotatedClassFactory(List<String> pluginClassNames) {
		this.pluginClassNames = pluginClassNames;
	}

	public <T> List<T> createInstances(Class<T> instanceClass) {
		List<T> instances = new ArrayList<>();
		for (String className : this.pluginClassNames) {
			try {
				Class<?> asmClass = Class.forName(className);
				if (instanceClass.isAssignableFrom(asmClass)) {
					Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
					T instance = asmInstanceClass.newInstance();
					instances.add(instance);
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | LinkageError e) {
				LOGGER.error("Failed to create: {}", className, e);
			}
		}
		return instances;
	}

	public static AnnotatedClassFactory create(Class annotationClass) {
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
		return new AnnotatedClassFactory(pluginClassNames);
	}
}
