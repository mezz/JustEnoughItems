package mezz.jei.gui.ingredients;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.common.util.StringUtil;
import mezz.jei.common.util.Translator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ListElementInfo<V> implements IListElementInfo<V> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	private static final Pattern MOD_NAME_SEPARATOR_PATTERN = Pattern.compile("(?=[A-Z_-])|\\s+");
	private static int elementCount = 0;

	private final IListElement<V> element;
	private final IModIdHelper modIdHelper;
	private final List<String> names;
	private final List<String> modIds;
	private final List<String> modNames;
	private final Identifier id;

	@Nullable
	public static <V> IListElementInfo<V> create(ITypedIngredient<V> value, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		int createdIndex = elementCount++;
		ListElement<V> element = new ListElement<>(value, createdIndex);
		return createFromElement(element, ingredientManager, modIdHelper);
	}

	@Nullable
	public static <V> IListElementInfo<V> createFromElement(IListElement<V> element, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		try {
			return new ListElementInfo<>(element, ingredientManager, modIdHelper);
		} catch (RuntimeException e) {
			try {
				ITypedIngredient<V> typedIngredient = element.getTypedIngredient();
				IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(typedIngredient.getType());
				String ingredientInfo = ingredientHelper.getErrorInfo(typedIngredient.getIngredient());
				LOGGER.warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				LOGGER.warn("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	protected ListElementInfo(IListElement<V> element, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		this.element = element;
		this.modIdHelper = modIdHelper;
		ITypedIngredient<V> value = element.getTypedIngredient();
		V ingredient = value.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		this.id = ingredientHelper.getIdentifier(ingredient);
		String displayModId = ingredientHelper.getDisplayModId(ingredient);
		String modId = this.id.getNamespace();
		if (modId.equals(displayModId)) {
			this.modIds = List.of(modId);
			this.modNames = List.of(modIdHelper.getModNameForModId(modId));
		} else {
			this.modIds = List.of(modId, displayModId);
			this.modNames = List.of(
				modIdHelper.getModNameForModId(modId),
				modIdHelper.getModNameForModId(displayModId)
			);
		}

		String displayNameLowercase = DisplayNameUtil.getLowercaseDisplayNameForSearch(ingredient, ingredientHelper);
		Collection<String> aliases = ingredientManager.getIngredientAliases(value);
		if (aliases.isEmpty()) {
			this.names = List.of(displayNameLowercase);
		} else {
			this.names = new ArrayList<>(1 + aliases.size());
			this.names.add(displayNameLowercase);
			for (String alias : aliases) {
				String lowercaseAlias = Translator.toLowercaseWithLocale(alias);
				this.names.add(lowercaseAlias);
			}
		}
	}

	@Override
	public List<String> getNames() {
		return names;
	}

	@Override
	public String getModNameForSorting() {
		return modNames.getFirst();
	}

	@Override
	public Collection<String> getModNames(IIngredientFilterConfig config) {
		Set<String> modNames = new HashSet<>(this.modNames);

		if (config.getSearchModIds()) {
			modNames.addAll(this.modIds);
		}

		if (config.getSearchModAliases()) {
			for (String modId : this.modIds) {
				Set<String> modAliases = modIdHelper.getModAliases(modId);
				modNames.addAll(modAliases);
			}
		}

		if (config.getSearchShortModNames()) {
			for (String modName : this.modNames) {
				List<String> shortModNames = getShortModNames(modName);
				modNames.addAll(shortModNames);
			}
		}

		Set<String> sanitizedModNames = new HashSet<>();
		for (String modName : modNames) {
			modName = modName.toLowerCase(Locale.ROOT);
			modName = WHITESPACE_PATTERN.matcher(modName).replaceAll("");
			sanitizedModNames.add(modName);
		}

		return sanitizedModNames;
	}

	@Override
	@Unmodifiable
	public final Set<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(value.getType());
		TooltipFlag.Default tooltipFlag = TooltipFlag.Default.NORMAL;
		if (config.getSearchAdvancedTooltips()) {
			tooltipFlag = TooltipFlag.Default.ADVANCED;
		}
		tooltipFlag = tooltipFlag.asCreative();

		TooltipFlag searchTooltipFlag = Services.PLATFORM.getInputHelper()
			.getSearchTooltipFlag(tooltipFlag);
		List<Component> tooltip = SafeIngredientUtil.getPlainTooltipForSearch(ingredientManager, ingredientRenderer, value, searchTooltipFlag);
		Set<String> strings = getStrings(tooltip);

		strings.remove(this.names.getFirst());
		strings.remove(this.modNames.getFirst().toLowerCase(Locale.ENGLISH));
		strings.remove(this.modIds.getFirst());
		strings.remove(id.getPath());

		return strings;
	}

	public static Set<String> getStrings(@Unmodifiable List<Component> tooltip) {
		Set<String> result = new HashSet<>();
		for (FormattedText component : tooltip) {
			String string = component.getString();
			string = StringUtil.removeChatFormatting(string);
			string = Translator.toLowercaseWithLocale(string);
			// Split tooltip strings into words to keep them from being too long.
			// Longer strings are more expensive for the suffix tree to handle.
			addSplitStrings(result, string);
		}
		return result;
	}

	private static void addSplitStrings(Set<String> result, String string) {
		string = string.trim();
		if (string.isEmpty()) {
			return;
		}
		String[] strings = WHITESPACE_PATTERN.split(string);
		for (String splitString : strings) {
			if (!splitString.isEmpty()) {
				result.add(splitString);
			}
		}
	}

	private static List<String> getShortModNames(String modName) {
		String[] words = MOD_NAME_SEPARATOR_PATTERN.split(modName);
		if (words.length <= 1) {
			return List.of();
		}
		return List.of(
			combineFirstLetters(words, 1),
			combineFirstLetters(words, 2)
		);
	}

	private static String combineFirstLetters(String[] words, final int count) {
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			int end = Math.min(count, word.length());
			sb.append(word, 0, end);
		}
		return sb.toString();
	}

	@Override
	public Collection<String> getTagStrings(IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		return ingredientHelper.getTagStream(value.getIngredient())
			.map(Identifier::getPath)
			.toList();
	}

	@Override
	public Stream<Identifier> getTagIds(IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		return ingredientHelper.getTagStream(value.getIngredient());
	}

	@Override
	public Iterable<Integer> getColors(IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		V ingredient = value.getIngredient();
		return ingredientHelper.getColors(ingredient);
	}

	@Override
	public @Unmodifiable Collection<String> getColorNames(IIngredientManager ingredientManager, IColorHelper colorHelper) {
		Iterable<Integer> colors = getColors(ingredientManager);
		return StreamSupport.stream(colors.spliterator(), false)
			.map(colorHelper::getClosestColorName)
			.map(Translator::toLowercaseWithLocale)
			.distinct()
			.toList();
	}

	@Override
	public @Unmodifiable Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager) {
		ItemStack itemStack = element.getTypedIngredient().getItemStack().orElse(ItemStack.EMPTY);
		if (itemStack.isEmpty()) {
			return List.of();
		}
		Set<String> creativeTabStrings = new HashSet<>();
		for (CreativeModeTab itemGroup : CreativeModeTabs.allTabs()) {
			if (!itemGroup.shouldDisplay() || itemGroup.getType() != CreativeModeTab.Type.CATEGORY) {
				continue;
			}
			if (itemGroup.contains(itemStack)) {
				String name = itemGroup.getDisplayName().getString();
				name = StringUtil.removeChatFormatting(name);
				name = Translator.toLowercaseWithLocale(name);
				addSplitStrings(creativeTabStrings, name);
			}
		}
		return creativeTabStrings;
	}

	@Override
	public Identifier getIdentifier() {
		return id;
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
	public int getCreatedIndex() {
		return element.getCreatedIndex();
	}
}
