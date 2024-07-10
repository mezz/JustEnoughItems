package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.GuiProperties;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ScreenPropertiesCache {
	private final IScreenHelper screenHelper;
	private @Nullable IGuiProperties previousGuiProperties;
	private Set<ImmutableRect2i> previousGuiExclusionAreas = Set.of();
	private @Nullable ImmutablePoint2i mouseExclusionArea;

	public ScreenPropertiesCache(IScreenHelper screenHelper) {
		this.screenHelper = screenHelper;
	}

	public static class Updater {
		private final ScreenPropertiesCache cache;
		private final Runnable onChange;
		private boolean changed = false;

		public Updater(ScreenPropertiesCache cache, Runnable onChange) {
			this.cache = cache;
			this.onChange = onChange;
		}

		public Updater updateScreen(@Nullable Screen guiScreen) {
			IGuiProperties currentGuiProperties = Optional.ofNullable(guiScreen)
				.flatMap(cache.screenHelper::getGuiProperties)
				.orElse(null);

			if (!GuiProperties.areEqual(cache.previousGuiProperties, currentGuiProperties)) {
				cache.previousGuiProperties = currentGuiProperties;
				changed = true;
			}

			return this;
		}

		public Updater updateExclusionAreas(Set<ImmutableRect2i> updatedGuiExclusionAreas) {
			if (!cache.previousGuiExclusionAreas.equals(updatedGuiExclusionAreas)) {
				cache.previousGuiExclusionAreas = updatedGuiExclusionAreas;
				changed = true;
			}
			return this;
		}

		public Updater updateMouseExclusionArea(@Nullable ImmutablePoint2i mouseExclusionArea) {
			if (!Objects.equals(cache.mouseExclusionArea, mouseExclusionArea)) {
				cache.mouseExclusionArea = mouseExclusionArea;
				changed = true;
			}
			return this;
		}

		public void update() {
			if (changed) {
				onChange.run();
			}
		}
	}

	public Updater getUpdater(Runnable onChange) {
		return new Updater(this, onChange);
	}

	public boolean hasValidScreen() {
		return previousGuiProperties != null;
	}

	public Optional<IGuiProperties> getGuiProperties() {
		return Optional.ofNullable(previousGuiProperties);
	}

	public Set<ImmutableRect2i> getGuiExclusionAreas() {
		return previousGuiExclusionAreas;
	}

	public @Nullable ImmutablePoint2i getMouseExclusionArea() {
		return mouseExclusionArea;
	}
}
