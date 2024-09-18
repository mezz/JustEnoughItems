package mezz.jei.gui.ingredients;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.config.IIngredientFilterConfig;
import mezz.jei.common.util.SafeIngredientUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public class ListElementInfo<V> implements IListElementInfo<V> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static int elementCount = 0;

	private final IListElement<V> element;
	private final List<String> names;
	private final List<String> modIds;
	private final List<String> modNames;
	private final ResourceLocation resourceLocation;

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
		ITypedIngredient<V> value = element.getTypedIngredient();
		V ingredient = value.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(value.getType());
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
		this.names = List.of(displayNameLowercase);
	}

	@Override
	public List<String> getNames() {
		return names;
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
	@Unmodifiable
	public final Set<String> getTooltipStrings(IIngredientFilterConfig config, IIngredientManager ingredientManager) {
		ITypedIngredient<V> value = element.getTypedIngredient();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(value.getType());
		TooltipFlag.Default tooltipFlag = config.getSearchAdvancedTooltips() ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;

		ListElementInfoTooltip tooltip = new ListElementInfoTooltip();
		SafeIngredientUtil.getTooltip(tooltip, ingredientManager, ingredientRenderer, value, tooltipFlag);
		Set<String> strings = tooltip.getStrings();

		strings.remove(this.names.get(0));
		strings.remove(this.modNames.get(0).toLowerCase(Locale.ENGLISH));
		strings.remove(this.modIds.get(0));
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
	public int getCreatedIndex() {
		return element.getCreatedIndex();
	}
}
