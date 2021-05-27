package mezz.jei.ingredients;

import com.google.common.collect.ImmutableSet;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.Translator;
import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IngredientListElementInfo<V> implements IIngredientListElementInfo<V> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

	private final IIngredientListElement<V> element;
	private final String displayName;
	private final List<String> modIds;
	private final List<String> modNames;
	private final String resourceId;

	@Nullable
	public static <V> IIngredientListElementInfo<V> create(IIngredientListElement<V> element, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		try {
			return new IngredientListElementInfo<>(element, ingredientHelper, modIdHelper);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				LOGGER.warn("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	protected IngredientListElementInfo(IIngredientListElement<V> element, IIngredientHelper<V> ingredientHelper, IModIdHelper modIdHelper) {
		this.element = element;
		V ingredient = element.getIngredient();
		String displayModId = ingredientHelper.getDisplayModId(ingredient);
		String modId = ingredientHelper.getModId(ingredient);
		this.modIds = new ArrayList<>();
		this.modIds.add(displayModId);
		if (!modId.equals(displayModId)) {
			this.modIds.add(modId);
		}
		this.modNames = this.modIds.stream()
			.map(modIdHelper::getModNameForModId)
			.collect(Collectors.toList());
		this.displayName = IngredientInformation.getDisplayName(ingredient, ingredientHelper);
		this.resourceId = ingredientHelper.getResourceId(ingredient);
	}

	@Override
	public String getName() {
		return Translator.toLowercaseWithLocale(this.displayName);
	}

	@Override
	public String getModNameForSorting() {
		return modNames.get(0);
	}

	@Override
	public Set<String> getModNameStrings() {
		Set<String> modNameStrings = new HashSet<>();
		for (int i = 0; i < modIds.size(); i++) {
			String modId = modIds.get(i);
			String modName = modNames.get(i);
			addModNameStrings(modNameStrings, modId, modName);
		}
		return modNameStrings;
	}

	private static void addModNameStrings(Set<String> modNames, String modId, String modName) {
		String modNameLowercase = modName.toLowerCase(Locale.ENGLISH);
		String modNameNoSpaces = SPACE_PATTERN.matcher(modNameLowercase).replaceAll("");
		String modIdNoSpaces = SPACE_PATTERN.matcher(modId).replaceAll("");
		modNames.add(modId);
		modNames.add(modNameNoSpaces);
		modNames.add(modIdNoSpaces);
	}

	@Override
	public final List<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager) {
		String modName = this.modNames.get(0);
		String modId = this.modIds.get(0);
		String modNameLowercase = modName.toLowerCase(Locale.ENGLISH);
		String displayNameLowercase = Translator.toLowercaseWithLocale(this.displayName);
		V ingredient = element.getIngredient();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredient);
		return IngredientInformation.getTooltipStrings(ingredient, ingredientRenderer, ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, resourceId), config);
	}

	@Override
	public Collection<String> getTagStrings(IIngredientManager ingredientManager) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		Collection<ResourceLocation> tags = ingredientHelper.getTags(ingredient);
		return tags.stream()
			.map(ResourceLocation::getPath)
			.collect(Collectors.toList());
	}

	@Override
	public Collection<ResourceLocation> getTagIds(IIngredientManager ingredientManager) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return ingredientHelper.getTags(ingredient);
	}

	@Override
	public Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		Collection<String> creativeTabsStrings = ingredientHelper.getCreativeTabNames(ingredient);
		return creativeTabsStrings.stream()
			.map(Translator::toLowercaseWithLocale)
			.collect(Collectors.toList());
	}

	@Override
	public Collection<String> getColorStrings(IIngredientManager ingredientManager) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return IngredientInformation.getColorStrings(ingredient, ingredientHelper);
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}

	@Override
	public IIngredientListElement<V> getElement() {
		return element;
	}

}
