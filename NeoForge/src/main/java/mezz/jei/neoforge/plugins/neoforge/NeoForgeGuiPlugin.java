package mezz.jei.neoforge.plugins.neoforge;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.registration.ISlotDisplayInterpreterRegistration;
import mezz.jei.api.runtime.IJeiFeatures;
import mezz.jei.gui.startup.JeiEventHandlers;
import mezz.jei.gui.startup.JeiGuiStarter;
import mezz.jei.gui.startup.ResourceReloadHandler;
import mezz.jei.neoforge.events.RuntimeEventSubscriptions;
import mezz.jei.neoforge.startup.EventRegistration;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@JeiPlugin
public class NeoForgeGuiPlugin implements IModPlugin {
	private static final Logger LOGGER = LogManager.getLogger();
	private static @Nullable ResourceReloadHandler resourceReloadHandler;

	private @Nullable IJeiFeatures jeiFeatures;
	private final RuntimeEventSubscriptions runtimeSubscriptions = new RuntimeEventSubscriptions(NeoForge.EVENT_BUS);

	@Override
	public Identifier getPluginUid() {
		return Identifier.fromNamespaceAndPath(ModIds.JEI_ID, "neoforge_gui");
	}

	@Override
	public void configureJei(IJeiFeatures jeiFeatures) {
		this.jeiFeatures = jeiFeatures;
	}

	@Override
	public void registerSlotDisplayInterpreters(ISlotDisplayInterpreterRegistration registration) {
		registration.register(
			NeoForgeMod.FLUID_SLOT_DISPLAY.get(),
			NeoForgeTypes.FLUID_STACK,
			(ignoredSlotDisplay, ignoredContext, interpretationBuilder) -> {
				interpretationBuilder.setWildcardForSubtypes(true);
			}
		);
		registration.register(
			NeoForgeMod.FLUID_TAG_SLOT_DISPLAY.get(),
			NeoForgeTypes.FLUID_STACK,
			(slotDisplay, ignoredContext, interpretationBuilder) -> {
				interpretationBuilder
					.setTagKey(slotDisplay.tag())
					.setWildcardForSubtypes(true);
			}
		);
	}

	@Override
	public void registerRuntime(IRuntimeRegistration registration) {
		if (!isJeiGuiEnabled()) {
			return;
		}

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

	private boolean isJeiGuiEnabled() {
		IJeiFeatures jeiFeatures = this.jeiFeatures;
		return jeiFeatures == null || jeiFeatures.isJeiGuiEnabled();
	}
}
