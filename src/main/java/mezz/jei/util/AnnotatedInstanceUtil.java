package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.common.discovery.ASMDataTable;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;

public class AnnotatedInstanceUtil {
	private AnnotatedInstanceUtil() {

	}

	public static List<IModPlugin> getModPlugins(@Nonnull ASMDataTable asmDataTable) {
		List<IModPlugin> modPlugins = getInstances(asmDataTable, JEIPlugin.class, IModPlugin.class);
		Iterator<IModPlugin> iterator = modPlugins.iterator();
		while (iterator.hasNext()) {
			IModPlugin modPlugin = iterator.next();
			if (!modPlugin.isModLoaded()) {
				iterator.remove();
			}
		}
		return modPlugins;
	}

	private static <T> List<T> getInstances(@Nonnull ASMDataTable asmDataTable, Class annotationClass, Class<T> instanceClass) {
		String annotationClassName = annotationClass.getCanonicalName();
		Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
		List<T> instances = new ArrayList<>();
		for (ASMDataTable.ASMData asmData : asmDatas) {
			try {
				Class<?> asmClass = Class.forName(asmData.getClassName());
				Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
				T instance = asmInstanceClass.newInstance();
				instances.add(instance);
			} catch (Throwable e) {
				Log.error("Failed to load: {}", asmData.getClassName(), e);
			}
		}
		return instances;
	}
}
