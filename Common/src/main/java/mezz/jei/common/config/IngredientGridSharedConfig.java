package mezz.jei.common.config;

import mezz.jei.common.config.legacy.LegacyEnumSerializers;
import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;
import net.mezzdev.config.api.value.editor.ConfigValueEditMode;
import net.mezzdev.config.api.value.IConfigValue;

final class IngredientGridSharedConfig {
	private static final boolean defaultDrawBackground = false;
	private static final IngredientGridNavigationMode defaultNavigationMode = IngredientGridNavigationMode.PAGED;
	private static final NavigationVisibility defaultNavigationVisibility = NavigationVisibility.ENABLED;

	private final IConfigValue<Boolean> drawBackground;
	private final IConfigValue<IngredientGridNavigationMode> navigationMode;
	private final IConfigValue<NavigationVisibility> navigationVisibility;

	IngredientGridSharedConfig(IConfigCategoryBuilder category) {
		drawBackground = category.addBoolean("drawBackground", defaultDrawBackground)
			.addLegacyValue("ingredientList", "drawBackground")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		navigationMode = category.addValue(
				"navigationMode",
				defaultNavigationMode,
				LegacyEnumSerializers.enumWithLegacyName(
					IngredientGridNavigationMode.class,
					"SMOOTH_SCROLLING",
					IngredientGridNavigationMode.SCROLLING
				)
			)
			.addLegacyValue("ingredientList", "navigationMode")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
		navigationVisibility = category.addEnum("navigationVisibility", defaultNavigationVisibility)
			.addLegacyValue("ingredientList", "navigationVisibility")
			.addLegacyValue("ingredientList", "buttonNavigationVisibility")
			.setEditMode(ConfigValueEditMode.IMMEDIATE)
			.build();
	}

	IConfigValue<Boolean> drawBackground() {
		return drawBackground;
	}

	IConfigValue<IngredientGridNavigationMode> navigationMode() {
		return navigationMode;
	}

	IConfigValue<NavigationVisibility> navigationVisibility() {
		return navigationVisibility;
	}
}
