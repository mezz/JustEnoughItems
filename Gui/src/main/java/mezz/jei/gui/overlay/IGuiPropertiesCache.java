package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.util.ImmutableRect2i;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Stores the current GUI properties and exclusion areas, and reports when cached values change.
 */
public interface IGuiPropertiesCache {
	/**
	 * Creates an updater that invokes the callback when cached GUI properties or exclusion areas change.
	 */
	IScreenPropertiesUpdater createUpdater(Runnable onChange);

	/**
	 * Returns the cached GUI properties when they are valid for overlay layout, or null when there is no valid screen.
	 */
	@Nullable
	IGuiProperties getGuiProperties();

	/**
	 * Returns cached screen areas that the overlay should avoid.
	 */
	Set<ImmutableRect2i> getGuiExclusionAreas();
}
