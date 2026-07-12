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

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.startup.IModIdHelper;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;

public class IngredientListElement<V> implements IIngredientListElement<V> {
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");
	private static final Pattern MOD_NAME_SEPARATOR_PATTERN = Pattern.compile("(?=[A-Z_-])|\\s+");

	private final V ingredient;
	private final int orderIndex;
	private final IIngredientHelper<V> ingredientHelper;
	private final IIngredientRenderer<V> ingredientRenderer;
	private final String displayName;
	private final List<String> nameStrings;
	private final IModIdHelper modIdHelper;
	private final List<String> modIds;
	private final List<String> modNames;
	private final ResourceLocation resourceLocation;
	private boolean visible = true;

	@Nullable
	public static <V> IngredientListElement<V> create(V ingredient, IIngredientRegistry ingredientRegistry, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, IModIdHelper modIdHelper, int orderIndex) {
		try {
			Collection<String> aliases = ingredientRegistry.getIngredientAliases(ingredient);
			return new IngredientListElement<>(ingredient, orderIndex, ingredientHelper, ingredientRenderer, modIdHelper, aliases);
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

	protected IngredientListElement(V ingredient, int orderIndex, IIngredientHelper<V> ingredientHelper, IIngredientRenderer<V> ingredientRenderer, IModIdHelper modIdHelper, Collection<String> aliases) {
		this.ingredient = ingredient;
		this.orderIndex = orderIndex;
		this.ingredientHelper = ingredientHelper;
		this.ingredientRenderer = ingredientRenderer;
		this.modIdHelper = modIdHelper;
		this.resourceLocation = ingredientHelper.getResourceLocation(ingredient);
		String displayModId = ingredientHelper.getDisplayModId(ingredient);
		String modId = this.resourceLocation.getNamespace();
		this.modIds = new ArrayList<>();
		this.modIds.add(displayModId);
		if (!modId.equals(displayModId)) {
			this.modIds.add(modId);
		}
		this.modNames = this.modIds.stream().map(modIdHelper::getModNameForModId).collect(Collectors.toList());
		this.displayName = IngredientInformation.getDisplayName(ingredient, ingredientHelper);
		this.nameStrings = new ArrayList<>(1 + aliases.size());
		this.nameStrings.add(Translator.toLowercaseWithLocale(this.displayName));
		for (String alias : aliases) {
			this.nameStrings.add(Translator.toLowercaseWithLocale(alias));
		}
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
	public Collection<String> getNameStrings() {
		return nameStrings;
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
			for (String alias : modIdHelper.getModAliases(modId)) {
				modNameStrings.add(removeSpacesAndLowercase(alias));
			}
			for (String shortModName : getShortModNames(modName)) {
				modNameStrings.add(shortModName.toLowerCase(Locale.ENGLISH));
			}
		}
		return modNameStrings;
	}

	private static Collection<String> getShortModNames(String modName) {
		String[] words = MOD_NAME_SEPARATOR_PATTERN.split(modName);
		if (words.length <= 1) {
			return ImmutableSet.of();
		}
		return ImmutableSet.of(combineFirstLetters(words, 1), combineFirstLetters(words, 2));
	}

	private static String combineFirstLetters(String[] words, int count) {
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			int end = Math.min(count, word.length());
			result.append(word, 0, end);
		}
		return result.toString();
	}

	private static void addModNameStrings(Set<String> modNames, String modId, String modName) {
		String modNameNoSpaces = removeSpacesAndLowercase(modName);
		String modIdNoSpaces = removeSpacesAndLowercase(modId);
		modNames.add(modId);
		modNames.add(modNameNoSpaces);
		modNames.add(modIdNoSpaces);
	}

	private static String removeSpacesAndLowercase(String value) {
		return SPACE_PATTERN.matcher(value.toLowerCase(Locale.ENGLISH)).replaceAll("");
	}

	@Override
	public final List<String> getTooltipStrings() {
		String modName = this.modNames.get(0);
		String modId = this.modIds.get(0);
		String modNameLowercase = modName.toLowerCase(Locale.ENGLISH);
		String displayNameLowercase = Translator.toLowercaseWithLocale(this.displayName);
		return IngredientInformation.getTooltipStrings(ingredient, ingredientRenderer, ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, resourceLocation.getPath()));
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
		return resourceLocation.toString();
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
