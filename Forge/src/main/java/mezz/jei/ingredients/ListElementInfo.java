package mezz.jei.ingredients;

import com.google.common.collect.ImmutableSet;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.util.Translator;
import net.minecraft.resources.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ListElementInfo<V> implements IListElementInfo<V> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

	private final IListElement<V> element;
	private final String displayNameLowercase;
	private final List<String> modIds;
	private final List<String> modNames;
	private final ResourceLocation resourceLocation;
	private int sortedIndex = Integer.MAX_VALUE;

	@Nullable
	public static <V> IListElementInfo<V> create(IListElement<V> element, RegisteredIngredients registeredIngredients, IModIdHelper modIdHelper) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(value.getType());
		try {
			return new ListElementInfo<>(element, ingredientHelper, modIdHelper);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(value.getIngredient());
				LOGGER.warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				LOGGER.warn("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	protected ListElementInfo(IListElement<V> element, IIngredientHelper<V> ingredientHelper, IModIdHelper modIdHelper) {
		this.element = element;
		ITypedIngredient<V> value = element.getTypedIngredient();
		V ingredient = value.getIngredient();
		this.resourceLocation = ingredientHelper.getResourceLocation(ingredient);
		String displayModId = ingredientHelper.getDisplayModId(ingredient);
		String modId = this.resourceLocation.getNamespace();
		this.modIds = new ArrayList<>();
		this.modIds.add(displayModId);
		if (!modId.equals(displayModId)) {
			this.modIds.add(modId);
		}
		this.modNames = this.modIds.stream()
			.map(modIdHelper::getModNameForModId)
			.toList();
		String displayName = IngredientInformationUtil.getDisplayName(ingredient, ingredientHelper);
		this.displayNameLowercase = Translator.toLowercaseWithLocale(displayName);
	}

	@Override
	public String getName() {
		return this.displayNameLowercase;
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
	public final List<String> getTooltipStrings(IIngredientFilterConfig config, RegisteredIngredients registeredIngredients) {
		String modName = this.modNames.get(0);
		String modId = this.modIds.get(0);
		String modNameLowercase = modName.toLowerCase(Locale.ENGLISH);
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientRenderer<V> ingredientRenderer = registeredIngredients.getIngredientRenderer(value.getType());
		ImmutableSet<String> toRemove = ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, resourceLocation.getPath());
		return IngredientInformationUtil.getTooltipStrings(value.getIngredient(), ingredientRenderer, toRemove, config);
	}

	@Override
	public Collection<String> getTagStrings(RegisteredIngredients registeredIngredients) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(value.getType());
		Collection<ResourceLocation> tags = ingredientHelper.getTags(value.getIngredient());
		return tags.stream()
			.map(ResourceLocation::getPath)
			.toList();
	}

	@Override
	public Collection<ResourceLocation> getTagIds(RegisteredIngredients registeredIngredients) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(value.getType());
		return ingredientHelper.getTags(value.getIngredient());
	}

	@Override
	public Collection<String> getCreativeTabsStrings(RegisteredIngredients registeredIngredients) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(value.getType());
		Collection<String> creativeTabsStrings = ingredientHelper.getCreativeTabNames(value.getIngredient());
		return creativeTabsStrings.stream()
			.map(Translator::toLowercaseWithLocale)
			.toList();
	}

	@Override
	public Collection<String> getColorStrings(RegisteredIngredients registeredIngredients) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = registeredIngredients.getIngredientHelper(value.getType());
		return IngredientInformationUtil.getColorStrings(value.getIngredient(), ingredientHelper);
	}

	@Override
	public ResourceLocation getResourceLocation() {
		return resourceLocation;
	}

	@Override
	public IListElement<V> getElement() {
		return element;
	}

	@Override
	public ITypedIngredient<V> getTypedIngredient() {
		return element.getTypedIngredient();
	}

	@Override
	public void setSortedIndex(int sortIndex) {
		this.sortedIndex = sortIndex;
	}

	@Override
	public int getSortedIndex() {
		return sortedIndex;
	}

}
