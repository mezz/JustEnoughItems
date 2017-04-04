package mezz.jei.startup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.util.Log;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

public final class AnnotatedInstanceUtil {
	private AnnotatedInstanceUtil() {

	}

	public static List<IModPlugin> getModPlugins(ASMDataTable asmDataTable) {
		return getInstances(asmDataTable, JEIPlugin.class, IModPlugin.class);
	}

	private static <T> List<T> getInstances(ASMDataTable asmDataTable, Class annotationClass, Class<T> instanceClass) {
		String annotationClassName = annotationClass.getCanonicalName();
		Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
		List<T> instances = new ArrayList<T>();
		for (ASMDataTable.ASMData asmData : asmDatas) {
			try {
				Class<?> asmClass = Class.forName(asmData.getClassName());
				Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
				T instance = asmInstanceClass.newInstance();
				instances.add(instance);
			} catch (ClassNotFoundException e) {
				Log.error("Failed to load: {}", asmData.getClassName(), e);
			} catch (IllegalAccessException e) {
				Log.error("Failed to load: {}", asmData.getClassName(), e);
			} catch (InstantiationException e) {
				Log.error("Failed to load: {}", asmData.getClassName(), e);
			} catch (ExceptionInInitializerError e) {
				Log.error("Failed to load: {}", asmData.getClassName(), e);
			} catch (LinkageError e) {
				Log.error("Failed to load: {}", asmData.getClassName(), e);
			}
		}
		return instances;
	}
}
