package mezz.jei.gui.config.sorting;

import com.mojang.blaze3d.platform.NativeImage;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.gui.elements.DrawableText;
import mezz.jei.common.platform.Services;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.recipes.RecipeCategoryIconUtil;
import net.mezzdev.config.gui.api.IConfigValueIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Provides JEI's runtime-discovered sort-order values and display metadata for ConfigGui.
 */
public final class SortingOrderConfigValues {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Map<String, Optional<IConfigValueIcon>> MOD_ICON_CACHE = new HashMap<>();

	private final IJeiRuntime runtime;
	private final Map<String, IRecipeCategory<?>> recipeCategoriesByValue;
	private final Map<String, String> ingredientModIdsByValue;

	public SortingOrderConfigValues(IJeiRuntime runtime) {
		this.runtime = Objects.requireNonNull(runtime, "runtime");
		this.recipeCategoriesByValue = createRecipeCategoriesByValue(runtime);
		this.ingredientModIdsByValue = createIngredientModIdsByValue(runtime);
	}

	public List<String> getRecipeCategorySortOrderValues() {
		return List.copyOf(recipeCategoriesByValue.keySet());
	}

	public Component getRecipeCategorySortOrderValueName(String value) {
		IRecipeCategory<?> category = recipeCategoriesByValue.get(value);
		if (category == null) {
			return Component.literal(value);
		}
		Identifier uid = category.getRecipeType().getUid();
		IModIdHelper modIdHelper = runtime.getJeiHelpers().getModIdHelper();
		String modName = modIdHelper.getModNameForModId(uid.getNamespace());
		return Component.translatable(
			"jei.config.client.sorting.recipeCategorySortOrder.value",
			modName,
			category.getTitle()
		);
	}

	public Optional<IConfigValueIcon> getRecipeCategorySortOrderValueIcon(String value) {
		return Optional.ofNullable(recipeCategoriesByValue.get(value))
			.map(category -> new DrawableConfigValueIcon(RecipeCategoryIconUtil.create(
				category,
				runtime.getRecipeManager(),
				runtime.getJeiHelpers().getGuiHelper()
			)));
	}

	public List<String> getIngredientModNameSortOrderValues() {
		return List.copyOf(ingredientModIdsByValue.keySet());
	}

	public Optional<IConfigValueIcon> getIngredientModNameSortOrderValueIcon(String value) {
		String modId = ingredientModIdsByValue.get(value);
		if (modId == null) {
			return Optional.of(createIngredientModNameFallbackIcon(value));
		}
		return MOD_ICON_CACHE.computeIfAbsent(modId, SortingOrderConfigValues::createIngredientModNameIcon)
			.or(() -> Optional.of(createIngredientModNameFallbackIcon(modId, value)));
	}

	public List<String> getIngredientTypeSortOrderValues() {
		return runtime.getIngredientManager()
			.getRegisteredIngredientTypes()
			.stream()
			.map(IngredientTypeSortingConfig::getIngredientTypeString)
			.distinct()
			.toList();
	}

	public Optional<IConfigValueIcon> getIngredientTypeSortOrderValueIcon(String value) {
		return runtime.getIngredientManager()
			.getRegisteredIngredientTypes()
			.stream()
			.filter(type -> IngredientTypeSortingConfig.getIngredientTypeString(type).equals(value))
			.findFirst()
			.flatMap(this::createIngredientTypeSortOrderValueIcon);
	}

	public Component getIngredientTypeSortOrderValueName(String value) {
		String typeName = getSimpleClassName(value);
		IIngredientManager ingredientManager = runtime.getIngredientManager();
		Optional<Identifier> pluginUid = ingredientManager.getRegisteredIngredientTypes().stream()
			.filter(type -> IngredientTypeSortingConfig.getIngredientTypeString(type).equals(value))
			.findFirst()
			.flatMap(ingredientManager::getRegisteringPluginUid);
		if (pluginUid.isPresent()) {
			String modName = runtime.getJeiHelpers().getModIdHelper()
				.getModNameForModId(pluginUid.get().getNamespace());
			return Component.translatable("jei.config.client.sorting.ingredientTypeSortOrder.value", typeName, modName);
		}
		return Component.literal(typeName);
	}

	public static Optional<Component> getRecipeCategorySortOrderValueDescription(String value) {
		return Optional.of(Component.translatable(
			"jei.config.client.sorting.recipeCategorySortOrder.value.description",
			value
		));
	}

	public static Optional<Component> getIngredientModNameSortOrderValueDescription(String value) {
		return Optional.of(Component.translatable(
			"jei.config.client.sorting.ingredientModNameSortOrder.value.description",
			value
		));
	}

	public static Optional<Component> getIngredientTypeSortOrderValueDescription(String value) {
		return Optional.of(Component.translatable(
			"jei.config.client.sorting.ingredientTypeSortOrder.value.description",
			value
		));
	}

	private static Map<String, IRecipeCategory<?>> createRecipeCategoriesByValue(IJeiRuntime runtime) {
		Map<String, IRecipeCategory<?>> recipeCategoriesByValue = new LinkedHashMap<>();
		runtime.getRecipeManager()
			.createRecipeCategoryLookup()
			.includeHidden()
			.get()
			.forEach(category -> {
				Identifier uid = category.getRecipeType().getUid();
				recipeCategoriesByValue.putIfAbsent(uid.toString(), category);
			});
		return Collections.unmodifiableMap(recipeCategoriesByValue);
	}

	private static Map<String, String> createIngredientModIdsByValue(IJeiRuntime runtime) {
		IIngredientManager ingredientManager = runtime.getIngredientManager();
		Set<String> modIds = new LinkedHashSet<>();
		for (IIngredientType<?> ingredientType : ingredientManager.getRegisteredIngredientTypes()) {
			addIngredientModIds(modIds, ingredientManager, ingredientType);
		}
		IModIdHelper modIdHelper = runtime.getJeiHelpers().getModIdHelper();
		Map<String, String> ingredientModIdsByValue = new LinkedHashMap<>();
		for (String modId : modIds) {
			String modName = modIdHelper.getModNameForModId(modId);
			ingredientModIdsByValue.putIfAbsent(modName, modId);
		}
		return Collections.unmodifiableMap(ingredientModIdsByValue);
	}

	private static <T> void addIngredientModIds(Set<String> modIds, IIngredientManager ingredientManager, IIngredientType<T> ingredientType) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredientType);
		for (ITypedIngredient<T> ingredient : ingredientManager.getAllTypedIngredients(ingredientType)) {
			try {
				// Sorting uses the resource namespace; display names and search aliases are unnecessary here.
				String modId = ingredientHelper.getIdentifier(ingredient.getIngredient()).getNamespace();
				modIds.add(modId);
			} catch (RuntimeException e) {
				LOGGER.warn("Failed to get the sorting mod id for ingredient type: {}", ingredientType.getIngredientClass().getName(), e);
			}
		}
	}

	private static Optional<IConfigValueIcon> createIngredientModNameIcon(String modId) {
		return Services.PLATFORM.getModHelper()
			.getModIconByteCandidates(modId)
			.stream()
			.map(iconBytes -> createIngredientModNameIcon(modId, iconBytes))
			.flatMap(Optional::stream)
			.findFirst();
	}

	private static Optional<IConfigValueIcon> createIngredientModNameIcon(String modId, byte[] iconBytes) {
		NativeImage image;
		try {
			image = NativeImage.read(new ByteArrayInputStream(iconBytes));
		} catch (IOException | RuntimeException e) {
			LOGGER.warn("Failed to load mod icon for {}", modId, e);
			return Optional.empty();
		}

		int width = image.getWidth();
		int height = image.getHeight();
		try {
			DynamicTexture texture = new DynamicTexture(() -> "JEI mod icon: " + modId, image);
			Identifier textureLocation = Identifier.fromNamespaceAndPath("jei", "mod_icon/" + modId);
			Minecraft.getInstance()
				.getTextureManager()
				.register(textureLocation, texture);
			return Optional.of(new TextureConfigValueIcon(textureLocation, width, height));
		} catch (RuntimeException e) {
			image.close();
			LOGGER.warn("Failed to register mod icon for {}", modId, e);
			return Optional.empty();
		}
	}

	private static IConfigValueIcon createIngredientModNameFallbackIcon(String value) {
		String text = getFallbackText(value);
		return new DrawableConfigValueIcon(new DrawableText(text, 16, 16, 0xE0E0E0));
	}

	private IConfigValueIcon createIngredientModNameFallbackIcon(String modId, String value) {
		if (modId.equals(Identifier.DEFAULT_NAMESPACE)) {
			return new DrawableConfigValueIcon(runtime.getJeiHelpers()
				.getGuiHelper()
				.createDrawableItemLike(Items.GRASS_BLOCK));
		}
		return createIngredientModNameFallbackIcon(value);
	}

	private <T> Optional<IConfigValueIcon> createIngredientTypeSortOrderValueIcon(IIngredientType<T> ingredientType) {
		IIngredientManager ingredientManager = runtime.getIngredientManager();
		return ingredientType.getRepresentativeIngredient()
			.flatMap(ingredient -> ingredientManager.createTypedIngredient(ingredientType, ingredient, false))
			.or(() -> ingredientManager.getAllTypedIngredients(ingredientType).stream().findFirst())
			.map(typedIngredient -> runtime.getJeiHelpers().getGuiHelper().createDrawableIngredient(typedIngredient))
			.map(DrawableConfigValueIcon::new);
	}

	private static String getSimpleClassName(String className) {
		int separatorIndex = className.lastIndexOf('.');
		if (separatorIndex < 0 || separatorIndex + 1 >= className.length()) {
			return className;
		}
		return className.substring(separatorIndex + 1);
	}

	private static String getFallbackText(String value) {
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return "?";
		}
		String[] words = trimmed.split("\\s+");
		if (words.length > 1) {
			StringBuilder text = new StringBuilder(2);
			for (String word : words) {
				if (!word.isEmpty()) {
					text.append(word.charAt(0));
				}
				if (text.length() >= 2) {
					break;
				}
			}
			if (!text.isEmpty()) {
				return text.toString().toUpperCase(Locale.ROOT);
			}
		}
		int endIndex = trimmed.offsetByCodePoints(0, Math.min(2, trimmed.codePointCount(0, trimmed.length())));
		return trimmed.substring(0, endIndex).toUpperCase(Locale.ROOT);
	}
}
