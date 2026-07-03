package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Batched updater for cached screen geometry and exclusion areas.
 */
public interface IScreenPropertiesUpdater {
	/**
	 * Updates cached GUI properties from the given screen.
	 */
	IScreenPropertiesUpdater updateScreen(@Nullable Screen guiScreen);

	/**
	 * Updates cached GUI properties directly.
	 */
	IScreenPropertiesUpdater updateGuiProperties(@Nullable IGuiProperties currentGuiProperties);

	/**
	 * Updates cached screen areas that the overlay should avoid.
	 */
	IScreenPropertiesUpdater updateExclusionAreas(Set<ImmutableRect2i> updatedGuiExclusionAreas);

	/**
	 * Updates the point that the bookmark overlay should avoid while dragging.
	 */
	IScreenPropertiesUpdater updateMouseExclusionArea(@Nullable ImmutablePoint2i mouseExclusionArea);

	/**
	 * Applies the batched updates and runs the change callback when needed.
	 */
	void update();

	/**
	 * Applies the batched updates and runs the change callback even when the cached screen geometry has not changed.
	 */
	void forceUpdate();
}
