package mezz.jei.forge.startup;

import mezz.jei.api.JeiPlugin;
import mezz.jei.library.startup.IPluginFinder;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

public final class ForgePluginFinder implements IPluginFinder {
	private static final Logger LOGGER = LogManager.getLogger();

	private final LinkedHashSet<String> pluginClassNames;

	public ForgePluginFinder() {
		Type annotationType = Type.getType(JeiPlugin.class);
		List<ModFileScanData> allScanData = ModList.get().getAllScanData();
		this.pluginClassNames = new LinkedHashSet<>();
		for (ModFileScanData scanData : allScanData) {
			Iterable<ModFileScanData.AnnotationData> annotations = scanData.getAnnotations();
			for (ModFileScanData.AnnotationData a : annotations) {
				if (Objects.equals(a.annotationType(), annotationType)) {
					String memberName = a.memberName();
					pluginClassNames.add(memberName);
				}
			}
		}
	}

	@Override
	public <T> List<T> getPlugins(Class<T> pluginClass) {
		List<T> instances = new ArrayList<>();
		for (String className : pluginClassNames) {
			try {
				Class<?> asmClass = Class.forName(className);
				if (pluginClass.isAssignableFrom(asmClass)) {
					Class<? extends T> asmInstanceClass = asmClass.asSubclass(pluginClass);
					Constructor<? extends T> constructor = asmInstanceClass.getDeclaredConstructor();
					T instance = constructor.newInstance();
					instances.add(instance);
				}
			} catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
				LOGGER.error("Failed to load: {}", className, e);
			}
		}
		return instances;
	}
}
