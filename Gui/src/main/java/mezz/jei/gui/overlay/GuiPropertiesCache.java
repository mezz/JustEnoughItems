package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.GuiProperties;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GuiPropertiesCache<T> implements IGuiPropertiesCache {
	private final GuiPropertiesGetter<T> guiPropertiesGetter;
	private @Nullable IGuiProperties previousGuiProperties;
	private boolean guiPropertiesAreValid = false;
	private Set<ImmutableRect2i> previousGuiExclusionAreas = Set.of();
	private @Nullable ImmutablePoint2i mouseExclusionArea;

	public GuiPropertiesCache(GuiPropertiesGetter<T> guiPropertiesGetter) {
		this.guiPropertiesGetter = guiPropertiesGetter;
	}

	@FunctionalInterface
	public interface GuiPropertiesGetter<T> {
		@Nullable
		IGuiProperties getGuiProperties(T screen);
	}

	private static class Updater<T> implements IScreenPropertiesUpdater {
		private static final Logger LOGGER = LogManager.getLogger();
		private static final int MIN_GUI_DIMENSION = -1_000_000_000;
		private static final int MAX_GUI_DIMENSION = 1_000_000_000;

		private final GuiPropertiesCache<T> cache;
		private final Runnable onChange;
		private boolean changed = false;

		public Updater(GuiPropertiesCache<T> cache, Runnable onChange) {
			this.cache = cache;
			this.onChange = onChange;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Updater<T> updateScreen(@Nullable Screen guiScreen) {
			if (guiScreen == null) {
				return updateGuiProperties(null);
			}
			T typedScreen = (T) guiScreen;
			return updateGuiProperties(cache.guiPropertiesGetter.getGuiProperties(typedScreen));
		}

		@Override
		public Updater<T> updateGuiProperties(@Nullable IGuiProperties currentGuiProperties) {
			if (!GuiProperties.areEqual(cache.previousGuiProperties, currentGuiProperties)) {
				boolean previouslyValid = cache.guiPropertiesAreValid;
				cache.guiPropertiesAreValid = validateGuiProperties(currentGuiProperties);
				cache.previousGuiProperties = currentGuiProperties;
				if (previouslyValid || cache.guiPropertiesAreValid) {
					changed = true;
				}
			}

			return this;
		}

		@Override
		public Updater<T> updateExclusionAreas(Set<ImmutableRect2i> updatedGuiExclusionAreas) {
			if (!cache.previousGuiExclusionAreas.equals(updatedGuiExclusionAreas)) {
				cache.previousGuiExclusionAreas = updatedGuiExclusionAreas;
				changed = true;
			}
			return this;
		}

		@Override
		public Updater<T> updateMouseExclusionArea(@Nullable ImmutablePoint2i mouseExclusionArea) {
			if (!Objects.equals(cache.mouseExclusionArea, mouseExclusionArea)) {
				cache.mouseExclusionArea = mouseExclusionArea;
				changed = true;
			}
			return this;
		}

		@Override
		public void update() {
			if (changed) {
				notifyChange();
			}
		}

		@Override
		public void forceUpdate() {
			notifyChange();
		}

		private void notifyChange() {
			onChange.run();
		}

		private static void validate(List<String> errors, String property, int min, int max, int value) {
			if (value < min || value > max) {
				errors.add(String.format("%s must be greater than %s and less than %s: %s", property, min, max, value));
			}
		}

		private static boolean validateGuiProperties(@Nullable IGuiProperties guiProperties) {
			if (guiProperties == null) {
				return false;
			}
			List<String> errors = new ArrayList<>();
			validate(errors, "guiXSize", 1, MAX_GUI_DIMENSION, guiProperties.guiXSize());
			validate(errors, "guiYSize", 1, MAX_GUI_DIMENSION, guiProperties.guiYSize());
			validate(errors, "screenWidth", 1, MAX_GUI_DIMENSION, guiProperties.screenWidth());
			validate(errors, "screenHeight", 1, MAX_GUI_DIMENSION, guiProperties.screenHeight());
			validate(errors, "guiLeft", MIN_GUI_DIMENSION, MAX_GUI_DIMENSION, guiProperties.guiLeft());
			validate(errors, "guiTop", MIN_GUI_DIMENSION, MAX_GUI_DIMENSION, guiProperties.guiTop());
			if (!errors.isEmpty()) {
				LOGGER.error(
					"Received invalid gui properties for screen: {}\n{}",
					guiProperties.screenClass(),
					String.join("\n", errors)
				);
				return false;
			}
			return true;
		}
	}

	@Override
	public IScreenPropertiesUpdater createUpdater(Runnable onChange) {
		return new Updater<>(this, onChange);
	}

	public boolean hasValidScreen() {
		return guiPropertiesAreValid;
	}

	@Override
	public @Nullable IGuiProperties getGuiProperties() {
		if (!guiPropertiesAreValid) {
			return null;
		}
		return previousGuiProperties;
	}

	@Override
	public Set<ImmutableRect2i> getGuiExclusionAreas() {
		return previousGuiExclusionAreas;
	}

	public @Nullable ImmutablePoint2i getMouseExclusionArea() {
		return mouseExclusionArea;
	}
}
