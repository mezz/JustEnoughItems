package mezz.jei.common.config;

import mezz.jei.common.config.legacy.LegacyEnumSerializers;
import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;
import net.mezzdev.config.api.value.editor.ConfigValueEditMode;
import net.mezzdev.config.api.value.IConfigValue;

final class IngredientGridSharedConfig {
	private static final IngredientGridBackgroundStyle defaultBackgroundStyle = IngredientGridBackgroundStyle.NONE;
	private static final IngredientGridNavigationMode defaultNavigationMode = IngredientGridNavigationMode.PAGED;
	private static final NavigationVisibility defaultNavigationVisibility = NavigationVisibility.ENABLED;

	private final IConfigValue<IngredientGridBackgroundStyle> backgroundStyle;
	private final IConfigValue<IngredientGridNavigationMode> navigationMode;
	private final IConfigValue<NavigationVisibility> navigationVisibility;

	IngredientGridSharedConfig(IConfigCategoryBuilder category) {
		backgroundStyle = category.addValue(
				"drawBackground",
				defaultBackgroundStyle,
				LegacyEnumSerializers.enumOrBoolean(IngredientGridBackgroundStyle.class, IngredientGridBackgroundStyle::fromBoolean)
			)
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

	IConfigValue<IngredientGridBackgroundStyle> backgroundStyle() {
		return backgroundStyle;
	}

	IConfigValue<IngredientGridNavigationMode> navigationMode() {
		return navigationMode;
	}

	IConfigValue<NavigationVisibility> navigationVisibility() {
		return navigationVisibility;
	}
}
