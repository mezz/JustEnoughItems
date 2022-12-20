package mezz.jei.gui.ingredients;

import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.core.config.IngredientSortStage;
import mezz.jei.gui.config.IngredientTypeSortingConfig;
import mezz.jei.gui.config.ModNameSortingConfig;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IngredientSorterComparators {
	private final IngredientFilter ingredientFilter;
	private final IIngredientManager ingredientManager;
	private final ModNameSortingConfig modNameSortingConfig;
	private final IngredientTypeSortingConfig ingredientTypeSortingConfig;

	public IngredientSorterComparators(
		IngredientFilter ingredientFilter,
		IIngredientManager ingredientManager,
		ModNameSortingConfig modNameSortingConfig,
		IngredientTypeSortingConfig ingredientTypeSortingConfig
	) {
		this.ingredientFilter = ingredientFilter;
		this.ingredientManager = ingredientManager;
		this.modNameSortingConfig = modNameSortingConfig;
		this.ingredientTypeSortingConfig = ingredientTypeSortingConfig;
	}

	public Comparator<IListElementInfo<?>> getComparator(List<IngredientSortStage> ingredientSorterStages) {
		return ingredientSorterStages.stream()
			.map(this::getComparator)
			.reduce(Comparator::thenComparing)
			.orElseGet(this::getDefault);
	}

	public Comparator<IListElementInfo<?>> getComparator(IngredientSortStage ingredientSortStage) {
		return switch (ingredientSortStage) {
			case ALPHABETICAL -> getAlphabeticalComparator();
			case CREATIVE_MENU -> getCreativeMenuComparator();
			case INGREDIENT_TYPE -> getIngredientTypeComparator();
			case MOD_NAME -> getModNameComparator();
			case TAG -> getTagComparator();
			case ARMOR -> getArmorComparator();
			case MAX_DURABILITY -> getMaxDurabilityComparator();
		};
	}

	public Comparator<IListElementInfo<?>> getDefault() {
		return getModNameComparator()
			.thenComparing(getIngredientTypeComparator())
			.thenComparing(getCreativeMenuComparator());
	}

	private static Comparator<IListElementInfo<?>> getCreativeMenuComparator() {
		return Comparator.comparingInt(o -> {
			IListElement<?> element = o.getElement();
			return element.getOrderIndex();
		});
	}

	private static Comparator<IListElementInfo<?>> getAlphabeticalComparator() {
		return Comparator.comparing(IListElementInfo::getName);
	}

	private Comparator<IListElementInfo<?>> getModNameComparator() {
		Set<String> modNames = this.ingredientFilter.getModNamesForSorting();
		return this.modNameSortingConfig.getComparatorFromMappedValues(modNames);
	}

	private Comparator<IListElementInfo<?>> getIngredientTypeComparator() {
		Collection<IIngredientType<?>> ingredientTypes = this.ingredientManager.getRegisteredIngredientTypes();
		Set<String> ingredientTypeStrings = ingredientTypes.stream()
			.map(IngredientTypeSortingConfig::getIngredientTypeString)
			.collect(Collectors.toSet());
		return this.ingredientTypeSortingConfig.getComparatorFromMappedValues(ingredientTypeStrings);
	}

	private static Comparator<IListElementInfo<?>> getMaxDurabilityComparator() {
		Comparator<IListElementInfo<?>> maxDamage =
			Comparator.comparing(o -> getItemStack(o).getMaxDamage());
		return maxDamage.reversed();
	}

	private Comparator<IListElementInfo<?>> getTagComparator() {
		Comparator<IListElementInfo<?>> isTagged =
			Comparator.comparing(this::hasTag);
		Comparator<IListElementInfo<?>> tag =
			Comparator.comparing(this::getTagForSorting);
		return isTagged.reversed().thenComparing(tag);
	}

	private static Comparator<IListElementInfo<?>> getArmorComparator() {
		Comparator<IListElementInfo<?>> isArmorComp =
			Comparator.comparing(o -> isArmor(getItemStack(o)));
		Comparator<IListElementInfo<?>> armorSlot =
			Comparator.comparing(o -> getArmorSlotIndex(getItemStack(o)));
		Comparator<IListElementInfo<?>> armorDamage =
			Comparator.comparing(o -> getArmorDamageReduce(getItemStack(o)));
		Comparator<IListElementInfo<?>> armorToughness =
			Comparator.comparing(o -> getArmorToughness(getItemStack(o)));
		Comparator<IListElementInfo<?>> maxDamage =
			Comparator.comparing(o -> getArmorDurability(getItemStack(o)));
		return isArmorComp.reversed()
			.thenComparing(armorSlot.reversed())
			.thenComparing(armorDamage.reversed())
			.thenComparing(armorToughness.reversed())
			.thenComparing(maxDamage.reversed());
	}

	private static boolean isArmor(ItemStack itemStack) {
		Item item = itemStack.getItem();
		return item instanceof ArmorItem;
	}

	private static int getArmorSlotIndex(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof ArmorItem armorItem) {
			return armorItem.getSlot().getFilterFlag();
		}
		return 0;
	}

	private static int getArmorDamageReduce(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof ArmorItem armorItem) {
			return armorItem.getDefense();
		}
		return 0;
	}

	private static float getArmorToughness(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof ArmorItem armorItem) {
			return armorItem.getToughness();
		}
		return 0;
	}

	private static int getArmorDurability(ItemStack itemStack) {
		if (isArmor(itemStack)) {
			return itemStack.getMaxDamage();
		}
		return 0;
	}

	private String getTagForSorting(IListElementInfo<?> elementInfo) {
		Collection<ResourceLocation> tagIds = elementInfo.getTagIds(ingredientManager);
		// Choose the most popular tag it has.
		return tagIds.stream()
			.max(Comparator.comparing(IngredientSorterComparators::tagCount))
			.map(ResourceLocation::getPath)
			.orElse("");
	}

	private static int tagCount(ResourceLocation tagId) {
		//TODO: make a tag blacklist.
		if (tagId.toString().equals("itemfilters:check_nbt")) {
			return 0;
		}
		TagKey<Item> tagKey = TagKey.create(Registry.ITEM_REGISTRY, tagId);
		return Registry.ITEM.getTag(tagKey)
			.map(ListBacked::size)
			.orElse(0);
	}

	private boolean hasTag(IListElementInfo<?> elementInfo) {
		return !getTagForSorting(elementInfo).isEmpty();
	}

	public static <V> ItemStack getItemStack(IListElementInfo<V> ingredientInfo) {
		ITypedIngredient<V> ingredient = ingredientInfo.getTypedIngredient();
		if (ingredient.getIngredient() instanceof ItemStack itemStack) {
			return itemStack;
		}
		return ItemStack.EMPTY;
	}
}
