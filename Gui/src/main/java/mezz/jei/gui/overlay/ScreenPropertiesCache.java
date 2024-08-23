package mezz.jei.gui.overlay;

import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.util.ImmutablePoint2i;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.GuiProperties;
import net.minecraft.client.gui.screens.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ScreenPropertiesCache {
	private final IScreenHelper screenHelper;
	private @Nullable IGuiProperties previousGuiProperties;
	private boolean guiPropertiesAreValid = false;
	private Set<ImmutableRect2i> previousGuiExclusionAreas = Set.of();
	private @Nullable ImmutablePoint2i mouseExclusionArea;

	public ScreenPropertiesCache(IScreenHelper screenHelper) {
		this.screenHelper = screenHelper;
	}

	public static class Updater {
		private static final Logger LOGGER = LogManager.getLogger();
		private static final int MIN_GUI_DIMENSION = -1_000_000_000;
		private static final int MAX_GUI_DIMENSION = 1_000_000_000;

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
				boolean previouslyValid = cache.guiPropertiesAreValid;
				cache.guiPropertiesAreValid = validateGuiProperties(currentGuiProperties);
				cache.previousGuiProperties = currentGuiProperties;
				if (previouslyValid || cache.guiPropertiesAreValid) {
					changed = true;
				}
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
			validate(errors,"guiLeft", MIN_GUI_DIMENSION, MAX_GUI_DIMENSION, guiProperties.guiLeft());
			validate(errors,"guiTop", MIN_GUI_DIMENSION, MAX_GUI_DIMENSION, guiProperties.guiTop());
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

	public Updater getUpdater(Runnable onChange) {
		return new Updater(this, onChange);
	}

	public boolean hasValidScreen() {
		return guiPropertiesAreValid;
	}

	public Optional<IGuiProperties> getGuiProperties() {
		if (!guiPropertiesAreValid) {
			return Optional.empty();
		}
		return Optional.ofNullable(previousGuiProperties);
	}

	public Set<ImmutableRect2i> getGuiExclusionAreas() {
		return previousGuiExclusionAreas;
	}

	public @Nullable ImmutablePoint2i getMouseExclusionArea() {
		return mouseExclusionArea;
	}
}
