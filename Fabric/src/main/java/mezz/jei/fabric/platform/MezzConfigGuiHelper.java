package mezz.jei.fabric.platform;

import net.mezzdev.config.gui.ConfigGui;
import net.mezzdev.config.gui.api.IConfigScreenFactory;
import net.mezzdev.config.gui.fabric.ConfigGuiFabricPluginFinder;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

final class MezzConfigGuiHelper {
	private MezzConfigGuiHelper() {

	}

	public static Optional<Screen> getConfigScreen(String modId, @Nullable Screen parent) {
		Map<String, IConfigScreenFactory> factories = ConfigGui.createScreenFactories(ConfigGuiFabricPluginFinder.getPlugins());
		return Optional.ofNullable(factories.get(modId))
			.map(factory -> factory.create(parent));
	}
}
