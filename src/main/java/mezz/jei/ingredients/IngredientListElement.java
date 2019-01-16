package mezz.jei.ingredients;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.LegacyUtil;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;

public class IngredientListElement<V> implements IIngredientListElement<V> {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

	private final V ingredient;
	private final int orderIndex;
	private final IIngredientHelper<V> ingredientHelper;
	private final IIngredientRenderer<V> ingredientRenderer;
	private final String displayName;
	private final List<String> modIds;
	private final List<String> modNames;
	private final String resourceId;
	private boolean visible = true;

	@Nullable
	public static <V> IngredientListElement<V> create(V ingredient, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, IModIdHelper modIdHelper, int orderIndex) {
		try {
			return new IngredientListElement<>(ingredient, orderIndex, ingredientHelper, ingredientRenderer, modIdHelper);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				Log.get().warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				Log.get().warn("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	protected IngredientListElement(V ingredient, int orderIndex, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, IModIdHelper modIdHelper) {
		this.ingredient = ingredient;
		this.orderIndex = orderIndex;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;
		String displayModId = ingredientHelper.getDisplayModId(ingredient);
		String modId = ingredientHelper.getModId(ingredient);
		this.modIds = new ArrayList<>();
		this.modIds.add(displayModId);
		if (!modId.equals(displayModId)) {
			this.modIds.add(modId);
		}
		this.modNames = this.modIds.stream().map(modIdHelper::getModNameForModId).collect(Collectors.toList());
		this.displayName = IngredientInformation.getDisplayName(ingredient, ingredientHelper);
		this.resourceId = LegacyUtil.getResourceId(ingredient, ingredientHelper);
	}

	@Override
	public final V getIngredient() {
		return ingredient;
	}

	@Override
	public int getOrderIndex() {
		return orderIndex;
	}

	@Override
	public IIngredientHelper<V> getIngredientHelper() {
		return ingredientHelper;
	}

	@Override
	public IIngredientRenderer<V> getIngredientRenderer() {
		return ingredientRenderer;
	}

	@Override
	public final String getDisplayName() {
		return displayName;
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
	public final List<String> getTooltipStrings() {
		String modName = this.modNames.get(0);
		String modId = this.modIds.get(0);
		String modNameLowercase = modName.toLowerCase(Locale.ENGLISH);
		String displayNameLowercase = Translator.toLowercaseWithLocale(this.displayName);
		return IngredientInformation.getTooltipStrings(ingredient, ingredientRenderer, ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, resourceId));
	}

	@Override
	public Collection<String> getOreDictStrings() {
		Collection<String> oreDictNames = ingredientHelper.getOreDictNames(ingredient);
		return oreDictNames.stream()
			.map(s -> s.toLowerCase(Locale.ENGLISH))
			.collect(Collectors.toList());
	}

	@Override
	public Collection<String> getCreativeTabsStrings() {
		Collection<String> creativeTabsStrings = ingredientHelper.getCreativeTabNames(ingredient);
		return creativeTabsStrings.stream()
			.map(Translator::toLowercaseWithLocale)
			.collect(Collectors.toList());
	}

	@Override
	public Collection<String> getColorStrings() {
		return IngredientInformation.getColorStrings(ingredient, ingredientHelper);
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
