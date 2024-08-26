package mezz.jei.gui.ingredients;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.util.SafeIngredientUtil;
import mezz.jei.common.util.Translator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class ListElementInfo<V> implements IListElementInfo<V> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IListElement<V> element;
	private final List<String> names;
	private final List<String> modIds;
	private final List<String> modNames;
	private final ResourceLocation resourceLocation;
	private int sortedIndex = Integer.MAX_VALUE;

	@Nullable
	public static <V> IListElementInfo<V> create(IListElement<V> element, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		try {
			return new ListElementInfo<>(element, ingredientHelper, ingredientManager, modIdHelper);
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

	protected ListElementInfo(IListElement<V> element, IIngredientHelper<V> ingredientHelper, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		this.element = element;
		ITypedIngredient<V> value = element.getTypedIngredient();
		V ingredient = value.getIngredient();
		this.resourceLocation = ingredientHelper.getResourceLocation(ingredient);
		String displayModId = ingredientHelper.getDisplayModId(ingredient);
		String modId = this.resourceLocation.getNamespace();
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
	public List<String> getModNames() {
		return modNames;
	}

	@Override
	public List<String> getModIds() {
		return modIds;
	}

	@Override
	@Unmodifiable
	public final Set<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(value.getType());
		TooltipFlag.Default tooltipFlag = config.getSearchAdvancedTooltips() ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
		tooltipFlag = tooltipFlag.asCreative();

		ListElementInfoTooltip tooltip = new ListElementInfoTooltip();
		SafeIngredientUtil.getTooltip(tooltip, ingredientManager, ingredientRenderer, value, tooltipFlag);
		Set<String> strings = tooltip.getStrings();

		strings.remove(this.names.getFirst());
		strings.remove(this.modNames.getFirst().toLowerCase(Locale.ENGLISH));
		strings.remove(this.modIds.getFirst());
		strings.remove(resourceLocation.getPath());

		return strings;
	}

	@Override
	public Collection<String> getTagStrings(IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		return ingredientHelper.getTagStream(value.getIngredient())
			.map(ResourceLocation::getPath)
			.toList();
	}

	@Override
	public Stream<ResourceLocation> getTagIds(IIngredientManager ingredientManager) {
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
