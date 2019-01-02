package mezz.jei.startup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.common.discovery.ASMDataTable;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AnnotatedInstanceUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private AnnotatedInstanceUtil() {

	}

	public static List<IModPlugin> getModPlugins(ASMDataTable asmDataTable) {
		return getInstances(asmDataTable, JEIPlugin.class, IModPlugin.class);
	}

	private static <T> List<T> getInstances(ASMDataTable asmDataTable, Class annotationClass, Class<T> instanceClass) {
		String annotationClassName = annotationClass.getCanonicalName();
		Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
		Collection<String> classNames = new ArrayList<>();
		for (ASMDataTable.ASMData asmData : asmDatas) {
			classNames.add(asmData.getClassName());
		}
		return getInstances(classNames, instanceClass);
	}

	public static <T> List<T> getInstances(Iterable<String> classNames, Class<T> instanceClass) {
		List<T> instances = new ArrayList<>();
		for (String className : classNames) {
			try {
				Class<?> asmClass = Class.forName(className);
				Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
				T instance = asmInstanceClass.newInstance();
				instances.add(instance);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | RuntimeException | LinkageError e) {
				LOGGER.error("Failed to load: {}", className, e);
			}
		}
		return instances;
	}
}
