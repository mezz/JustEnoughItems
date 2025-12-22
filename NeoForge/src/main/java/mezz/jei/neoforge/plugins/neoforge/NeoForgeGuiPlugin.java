package mezz.jei.neoforge.plugins.neoforge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.gui.startup.JeiGuiStarter;
import mezz.jei.gui.startup.ResourceReloadHandler;
import mezz.jei.neoforge.events.RuntimeEventSubscriptions;
import mezz.jei.neoforge.startup.EventRegistration;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@JeiPlugin
public class NeoForgeGuiPlugin implements IModPlugin {
	private static final Logger LOGGER = LogManager.getLogger();
	private static @Nullable ResourceReloadHandler resourceReloadHandler;

	private final RuntimeEventSubscriptions runtimeSubscriptions = new RuntimeEventSubscriptions(NeoForge.EVENT_BUS);

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "neoforge_gui");
	}

	@Override
	public void registerRuntime(IRuntimeRegistration registration) {
		if (!runtimeSubscriptions.isEmpty()) {
			LOGGER.error("JEI GUI is already running.");
			runtimeSubscriptions.clear();
		}

		JeiEventHandlers eventHandlers = JeiGuiStarter.start(registration);
		resourceReloadHandler = eventHandlers.resourceReloadHandler();

		EventRegistration.registerEvents(runtimeSubscriptions, eventHandlers);
	}

	@Override
	public void onRuntimeUnavailable() {
		LOGGER.info("Stopping JEI GUI");
		runtimeSubscriptions.clear();
		resourceReloadHandler = null;
	}

	public static Optional<ResourceReloadHandler> getResourceReloadHandler() {
		return Optional.ofNullable(resourceReloadHandler);
	}
}
