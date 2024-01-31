package mezz.jei.gui.ingredients;

import com.google.common.collect.ImmutableSet;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.Translator;
import mezz.jei.common.config.IIngredientFilterConfig;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public class ListElementInfo<V> implements IListElementInfo<V> {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IListElement<V> element;
	private final String displayNameLowercase;
	private final List<String> modIds;
	private final List<String> modNames;
	private final ResourceLocation resourceLocation;
	private int sortedIndex = Integer.MAX_VALUE;

	public static <V> Optional<IListElementInfo<V>> create(IListElement<V> element, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
		try {
			ListElementInfo<V> info = new ListElementInfo<>(element, ingredientHelper, modIdHelper);
			return Optional.of(info);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(value.getIngredient());
				LOGGER.warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				LOGGER.warn("Found a broken ingredient.", e2);
			}
			return Optional.empty();
		}
	}

	protected ListElementInfo(IListElement<V> element, IIngredientHelper<V> ingredientHelper, IModIdHelper modIdHelper) {
		this.element = element;
		ITypedIngredient<V> value = element.getTypedIngredient();
		V ingredient = value.getIngredient();
		this.resourceLocation = ingredientHelper.getResourceLocation(ingredient);
		String displayModId = ingredientHelper.getDisplayModId(ingredient);
		String modId = this.resourceLocation.getNamespace();
		this.modIds = Stream.of(displayModId, modId)
			.distinct()
			.toList();
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
	public List<String> getModNames() {
		return modNames;
	}

	@Override
	public List<String> getModIds() {
		return modIds;
	}

	@Override
	public final List<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager) {
		String modName = this.modNames.get(0);
		String modId = this.modIds.get(0);
		String modNameLowercase = modName.toLowerCase(Locale.ENGLISH);
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(value.getType());
		ImmutableSet<String> toRemove = ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, resourceLocation.getPath());
		return IngredientInformationUtil.getTooltipStrings(ingredientManager, value, ingredientRenderer, toRemove, config);
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
