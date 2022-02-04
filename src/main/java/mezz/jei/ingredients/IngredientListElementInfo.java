package mezz.jei.ingredients;

import com.google.common.collect.ImmutableSet;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
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

public class IngredientListElementInfo<V> implements IIngredientListElementInfo<V> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

	private final IIngredientListElement<V> element;
	private final String displayName;
	private final List<String> modIds;
	private final List<String> modNames;
	private final ResourceLocation resourceLocation;
	private Integer sortedIndex;

	@Nullable
	public static <V> IIngredientListElementInfo<V> create(IIngredientListElement<V> element, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		try {
			return new IngredientListElementInfo<>(element, ingredientHelper, modIdHelper);
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

	protected IngredientListElementInfo(IIngredientListElement<V> element, IIngredientHelper<V> ingredientHelper, IModIdHelper modIdHelper) {
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
		this.displayName = IngredientInformationUtil.getDisplayName(ingredient, ingredientHelper);
		this.sortedIndex = -1;
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
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(value.getType());
		ImmutableSet<String> toRemove = ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, resourceLocation.getPath());
		return IngredientInformationUtil.getTooltipStrings(value.getIngredient(), ingredientRenderer, toRemove, config);
	}

	@Override
	public Collection<String> getTagStrings(IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		Collection<ResourceLocation> tags = ingredientHelper.getTags(value.getIngredient());
		return tags.stream()
			.map(ResourceLocation::getPath)
			.toList();
	}

	@Override
	public Collection<ResourceLocation> getTagIds(IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		return ingredientHelper.getTags(value.getIngredient());
	}

	@Override
	public Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		Collection<String> creativeTabsStrings = ingredientHelper.getCreativeTabNames(value.getIngredient());
		return creativeTabsStrings.stream()
			.map(Translator::toLowercaseWithLocale)
			.toList();
	}

	@Override
	public Collection<String> getColorStrings(IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		return IngredientInformationUtil.getColorStrings(value.getIngredient(), ingredientHelper);
	}

	@Override
	public ResourceLocation getResourceLocation() {
		return resourceLocation;
	}

	@Override
	public IIngredientListElement<V> getElement() {
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
