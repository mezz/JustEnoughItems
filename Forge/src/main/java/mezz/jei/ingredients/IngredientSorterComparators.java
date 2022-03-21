package mezz.jei.ingredients;

import com.google.common.collect.Multimap;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.config.sorting.ModNameSortingConfig;
import mezz.jei.gui.ingredients.IListElement;
import net.minecraft.core.HolderSet.ListBacked;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IngredientSorterComparators {
	private final IngredientFilter ingredientFilter;
	private final RegisteredIngredients registeredIngredients;
	private final ModNameSortingConfig modNameSortingConfig;
	private final IngredientTypeSortingConfig ingredientTypeSortingConfig;

	public IngredientSorterComparators(
		IngredientFilter ingredientFilter,
		RegisteredIngredients registeredIngredients,
		ModNameSortingConfig modNameSortingConfig,
		IngredientTypeSortingConfig ingredientTypeSortingConfig
	) {
		this.ingredientFilter = ingredientFilter;
		this.registeredIngredients = registeredIngredients;
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
			case TOOL_TYPE -> getToolsComparator();
			case TAG -> getTagComparator();
			case WEAPON_DAMAGE -> getWeaponDamageComparator();
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
		Collection<IIngredientType<?>> ingredientTypes = this.registeredIngredients.getIngredientTypes();
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

	private static Comparator<IListElementInfo<?>> getToolsComparator() {
		Comparator<IListElementInfo<?>> toolType =
			Comparator.comparing(o -> getToolClass(getItemStack(o)));
		Comparator<IListElementInfo<?>> tier =
			Comparator.comparing(o -> getTier(getItemStack(o)));
		Comparator<IListElementInfo<?>> maxDamage =
			Comparator.comparing(o -> getToolDurability(getItemStack(o)));

		return toolType.reversed() // Sort non-tools after the tools.
			.thenComparing(tier.reversed())
			.thenComparing(maxDamage.reversed());
	}

	private static Comparator<IListElementInfo<?>> getWeaponDamageComparator() {
		Comparator<IListElementInfo<?>> isWeaponComp =
			Comparator.comparing(o -> isWeapon(getItemStack(o)));
		Comparator<IListElementInfo<?>> attackDamage =
			Comparator.comparing(o -> getWeaponDamage(getItemStack(o)));
		Comparator<IListElementInfo<?>> attackSpeed =
			Comparator.comparing(o -> getWeaponSpeed(getItemStack(o)));
		Comparator<IListElementInfo<?>> maxDamage =
			Comparator.comparing(o -> getWeaponDurability(getItemStack(o)));
		return isWeaponComp.reversed()
			.thenComparing(attackDamage.reversed())
			.thenComparing(attackSpeed.reversed())
			.thenComparing(maxDamage.reversed());
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

	private static int getTier(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (item instanceof TieredItem tieredItem) {
			Tier tier = tieredItem.getTier();
			List<Tier> sortedTiers = TierSortingRegistry.getSortedTiers();
			return sortedTiers.indexOf(tier);
		}
		return -1;
	}

	private static boolean isTool(ItemStack itemStack) {
		return getToolActions(itemStack).stream()
			.anyMatch(itemStack::canPerformAction);
	}

	private static int getToolDurability(ItemStack itemStack) {
		if (!isTool(itemStack)) {
			return 0;
		}
		return itemStack.getMaxDamage();
	}

	private static boolean isWeapon(ItemStack itemStack) {
		//Sort Weapons apart from tools, armor, and other random things..
		//AttackDamage also filters out Tools and Armor.  Anything that deals extra damage is a weapon.
		return getWeaponDamage(itemStack) > 0;
	}

	private static double getWeaponDamage(ItemStack itemStack) {
		if (isTool(itemStack) || isArmor(itemStack)) {
			return 0;
		}
		Multimap<Attribute, AttributeModifier> multimap = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);
		return max(multimap, Attributes.ATTACK_DAMAGE);
	}

	private static double getWeaponSpeed(ItemStack itemStack) {
		if (!isWeapon(itemStack)) {
			return 0;
		}
		Multimap<Attribute, AttributeModifier> multimap = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);
		return max(multimap, Attributes.ATTACK_SPEED);
	}

	private static double max(Multimap<Attribute, AttributeModifier> multimap, Attribute attribute) {
		Collection<AttributeModifier> modifiers = multimap.get(attribute);
		return max(modifiers);
	}

	private static double max(Collection<AttributeModifier> modifiers) {
		return modifiers.stream()
			.mapToDouble(AttributeModifier::getAmount)
			.max()
			.orElse(0);
	}

	private static int getWeaponDurability(ItemStack itemStack) {
		if (isWeapon(itemStack)) {
			return itemStack.getMaxDamage();
		}
		return 0;
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
		Collection<ResourceLocation> tagIds = elementInfo.getTagIds(registeredIngredients);
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

	private static String getToolClass(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return "";
		}

		return getToolActions(itemStack).stream()
			.filter(itemStack::canPerformAction)
			.findFirst()
			.map(ToolAction::name)
			.orElse("");
	}

	private static Collection<ToolAction> getToolActions(ItemStack itemStack) {
		// HACK: ensure the actions for the itemStack get loaded before we call ToolAction.getActions(),
		// so the ToolAction.getActions() map is populated with whatever actions the itemStack uses.
		itemStack.canPerformAction(ToolActions.AXE_DIG);

		return ToolAction.getActions();
	}

	public static <V> ItemStack getItemStack(IListElementInfo<V> ingredientInfo) {
		ITypedIngredient<V> ingredient = ingredientInfo.getTypedIngredient();
		if (ingredient.getIngredient() instanceof ItemStack itemStack) {
			return itemStack;
		}
		return ItemStack.EMPTY;
	}
}
