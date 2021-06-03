package mezz.jei.ingredients;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IClientConfig;
import mezz.jei.config.sorting.IngredientTreeSortingConfig;
import mezz.jei.config.sorting.IngredientTypeSortingConfig;
import mezz.jei.config.sorting.ModNameSortingConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class IngredientSorter implements IIngredientSorter {

	//0 did not successfully filter out the non-weapons. 1/1024 should do the trick.
	final static Double notQuiteZero = 1.0/ 1024.0;

	private static final Comparator<IIngredientListElementInfo<?>> CREATIVE_MENU =
		Comparator.comparingInt(o -> {
			IIngredientListElement<?> element = o.getElement();
			return element.getOrderIndex();
		});

	private static final Comparator<IIngredientListElementInfo<?>> PRE_SORTED =
		Comparator.comparing(IIngredientListElementInfo::getSortedIndex);

	private static final Comparator<IIngredientListElementInfo<?>> ALPHABETICAL =
		Comparator.comparing(IIngredientListElementInfo::getName);

	private final IClientConfig clientConfig;
	private final ModNameSortingConfig modNameSortingConfig;
	private final IngredientTypeSortingConfig ingredientTypeSortingConfig;
	private final IngredientTreeSortingConfig ingredientTreeSortingConfig;

	private boolean isCacheValid;

	public IngredientSorter(IClientConfig clientConfig, ModNameSortingConfig modNameSortingConfig, IngredientTypeSortingConfig ingredientTypeSortingConfig, IngredientTreeSortingConfig ingredientTreeSortingConfig) {
		this.clientConfig = clientConfig;
		this.modNameSortingConfig = modNameSortingConfig;
		this.ingredientTypeSortingConfig = ingredientTypeSortingConfig;
		this.ingredientTreeSortingConfig = ingredientTreeSortingConfig;
		this.isCacheValid = false;
	}

	@Override
	public void doPreSort(IngredientFilter ingredientFilter, IIngredientManager ingredientManager) {
		//When this is called, we always resort, regardless of valid status.
		Set<String> modNames = ingredientFilter.getModNamesForSorting();
		Collection<IIngredientType<?>> ingredientTypes = ingredientManager.getRegisteredIngredientTypes();

		Comparator<IIngredientListElementInfo<?>> modName = createModNameComparator(modNames);
		Comparator<IIngredientListElementInfo<?>> ingredientType = createIngredientTypeComparator(ingredientTypes);
		Comparator<IIngredientListElementInfo<?>> ingredientTree = ingredientTreeSortingConfig.getComparator();
		

		EnumMap<IngredientSortStage, Comparator<IIngredientListElementInfo<?>>> comparatorsForStages = new EnumMap<>(IngredientSortStage.class);
		comparatorsForStages.put(IngredientSortStage.ALPHABETICAL, ALPHABETICAL);
		comparatorsForStages.put(IngredientSortStage.CREATIVE_MENU, CREATIVE_MENU);
		comparatorsForStages.put(IngredientSortStage.INGREDIENT_TYPE, ingredientType);
		comparatorsForStages.put(IngredientSortStage.MOD_NAME, modName);
		comparatorsForStages.put(IngredientSortStage.TOOL_TYPE, createToolsComparator());
		comparatorsForStages.put(IngredientSortStage.TAG, createTagComparator());
		comparatorsForStages.put(IngredientSortStage.WEAPON_DAMAGE, createAttackComparator());
		comparatorsForStages.put(IngredientSortStage.ARMOR, createArmorComparator());
		comparatorsForStages.put(IngredientSortStage.MAX_DURABILITY, createMaxDurabilityComparator());
		comparatorsForStages.put(IngredientSortStage.ITEM_TREE, ingredientTree);

		
		List<IngredientSortStage> ingredientSorterStages = this.clientConfig.getIngredientSorterStages();

		Comparator<IIngredientListElementInfo<?>> completeComparator = ingredientSorterStages.stream()
			.map(comparatorsForStages::get)
			.reduce(Comparator::thenComparing)
			.orElseGet(() -> modName.thenComparing(ingredientType).thenComparing(CREATIVE_MENU));

		//Get all of the items sorted with our custom comparator.
		List<IIngredientListElementInfo<?>> results = ingredientFilter.getIngredientListPreSort(completeComparator);
		
		//Go through all of the items and set their home.
		int index = 0;
		for(IIngredientListElementInfo<?> element: results){
			element.setSortedIndex(index);
			index++;
		}
		this.isCacheValid = true;
		//Don't need to hang on to this any more.
		ingredientTreeSortingConfig.reset();
	}

	@Override
	public Comparator<IIngredientListElementInfo<?>> getComparator(IngredientFilter ingredientFilter, IIngredientManager ingredientManager) {
		if (!this.isCacheValid) {
			this.doPreSort(ingredientFilter, ingredientManager);
		}
		//Now the comparator just uses that index value to order everything.
		return PRE_SORTED;
	}

	private Comparator<IIngredientListElementInfo<?>> createModNameComparator(Collection<String> modNames) {
		return this.modNameSortingConfig.getComparatorFromMappedValues(modNames);
	}

	private Comparator<IIngredientListElementInfo<?>> createIngredientTypeComparator(Collection<IIngredientType<?>> ingredientTypes) {
		Set<String> ingredientTypeStrings = ingredientTypes.stream()
			.map(IIngredientType::getIngredientClass)
			.map(IngredientTypeSortingConfig::getIngredientType)
			.collect(Collectors.toSet());
		return this.ingredientTypeSortingConfig.getComparatorFromMappedValues(ingredientTypeStrings);
	}

	@Override
	public void invalidateCache() {
		this.isCacheValid = false;
	}

	private Comparator<IIngredientListElementInfo<?>> createMaxDurabilityComparator() {
		Comparator<IIngredientListElementInfo<?>> maxDamage = 
			Comparator.comparing(o -> getMaxDamage(IngredientUtils.getItemStack(o)));
		return maxDamage.reversed();
	}

	private Comparator<IIngredientListElementInfo<?>> createTagComparator() {
		Comparator<IIngredientListElementInfo<?>> isTagged = 
			Comparator.comparing(o -> hasTag(o));
		Comparator<IIngredientListElementInfo<?>> tag = 
			Comparator.comparing(o -> getTagForSorting(o));
		return isTagged.reversed().thenComparing(tag);
	}

	private Comparator<IIngredientListElementInfo<?>> createToolsComparator() {
		Comparator<IIngredientListElementInfo<?>> isToolComp = 
			Comparator.comparing(o -> isTool(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> toolType = 
			Comparator.comparing(o -> getToolClass(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> harvestLevel = 
			Comparator.comparing(o -> getHarvestLevel(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> maxDamage = 
			Comparator.comparing(o -> getToolDurability(IngredientUtils.getItemStack(o)));
		return isToolComp.reversed().thenComparing(toolType).thenComparing(harvestLevel.reversed()).thenComparing(maxDamage.reversed());
	}

	private Comparator<IIngredientListElementInfo<?>> createAttackComparator() {
		Comparator<IIngredientListElementInfo<?>> isWeaponComp = 
			Comparator.comparing(o -> isWeapon(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> attackDamage = 
			Comparator.comparing(o -> getAttackDamage(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> attackSpeed = 
			Comparator.comparing(o -> getAttackSpeed(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> maxDamage = 
			Comparator.comparing(o -> getWeaponDurability(IngredientUtils.getItemStack(o)));
		return isWeaponComp.reversed().thenComparing(attackDamage.reversed()).thenComparing(attackSpeed.reversed()).thenComparing(maxDamage.reversed());
	}

	private Comparator<IIngredientListElementInfo<?>> createArmorComparator() {
		Comparator<IIngredientListElementInfo<?>> isArmorComp = 
			Comparator.comparing(o -> isArmor(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> armorSlot = 
			Comparator.comparing(o -> getArmorSlotIndex(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> armorDamage = 
			Comparator.comparing(o -> getArmorDamageReduce(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> armorToughness = 
			Comparator.comparing(o -> getArmorToughness(IngredientUtils.getItemStack(o)));
		Comparator<IIngredientListElementInfo<?>> maxDamage = 
			Comparator.comparing(o -> getArmorDurability(IngredientUtils.getItemStack(o)));
		return isArmorComp.reversed().thenComparing(armorSlot.reversed()).thenComparing(armorDamage.reversed()).thenComparing(armorToughness.reversed()).thenComparing(maxDamage.reversed());
	}

	private static int getMaxDamage(ItemStack itemStack) {
		int maxDamage = Integer.MAX_VALUE;
		if (itemStack != ItemStack.EMPTY) {
			maxDamage = itemStack.getMaxDamage();	
		}
		return maxDamage;
	};

	private static int getHarvestLevel(ItemStack itemStack) {		
		if (itemStack != ItemStack.EMPTY) {
			return (itemStack.getToolTypes().size() > 0 ? itemStack.getHarvestLevel(itemStack.getToolTypes().iterator().next(), null, null) : -1);
		}
		return -1;
	};

	private static boolean isTool(ItemStack itemStack){
		//Sort non-tools after the tools.
		return !getToolClass(itemStack).isEmpty();
	};

	private static int getToolDurability(ItemStack itemStack) {
		if (isTool(itemStack))
			return itemStack.getMaxDamage();
		return 0;
	}

	private static boolean isWeapon(ItemStack itemStack) {
		//Sort Weapons apart from tools, armor, and other random things..		
		//AttackDamage also filters out Tools and Armor.  Anything that deals extra damage is a weapon.
		return getAttackDamage(itemStack) > notQuiteZero;
	};

	private static Double getAttackDamage(ItemStack itemStack) {
		Double attackDamage = Double.MIN_VALUE;
		if (!isTool(itemStack) && !isArmor(itemStack)) {
			Multimap<Attribute, AttributeModifier> multimap = itemStack.getAttributeModifiers(EquipmentSlotType.MAINHAND);
			boolean hasDamage = multimap.containsKey(Attributes.ATTACK_DAMAGE);
			if (hasDamage) {
				Collection<AttributeModifier> damageMap = multimap.get(Attributes.ATTACK_DAMAGE);
				attackDamage = ((AttributeModifier) damageMap.toArray()[0]).getAmount();
			}
		}
		return attackDamage;
	};

	private static Double getAttackSpeed(ItemStack itemStack) {
		Double attackDamage = Double.MIN_VALUE;
		Double attackSpeed = Double.MIN_VALUE;
		//This is the isWeapon test so we don't order by these properties for non-weapons.
		if (!isTool(itemStack) && !isArmor(itemStack)) {
			Multimap<Attribute, AttributeModifier> multimap = itemStack.getAttributeModifiers(EquipmentSlotType.MAINHAND);
			boolean hasDamage = multimap.containsKey(Attributes.ATTACK_DAMAGE);
			boolean hasSpeed = multimap.containsKey(Attributes.ATTACK_SPEED);			
			if (hasDamage) {
				Collection<AttributeModifier> damageMap = multimap.get(Attributes.ATTACK_DAMAGE);
				attackDamage = ((AttributeModifier) damageMap.toArray()[0]).getAmount();
				//Apply the isWeapon final test here.
				if (attackDamage > notQuiteZero && hasSpeed) {
					Collection<AttributeModifier> speedMap = multimap.get(Attributes.ATTACK_SPEED);
					attackSpeed = ((AttributeModifier) speedMap.toArray()[0]).getAmount();
				}
			}
		}
		return attackSpeed;
	};

	private static int getWeaponDurability(ItemStack itemStack) {
		if (isWeapon(itemStack)) {
			return getMaxDamage(itemStack);
		}
		return 0;
	}

	private static boolean isArmor(ItemStack itemStack) {
		Item item = itemStack.getItem();	
		return item instanceof ArmorItem;
	};

	private static int getArmorSlotIndex(ItemStack itemStack) {
		Item item = itemStack.getItem();	
		if (item instanceof ArmorItem) {
			ArmorItem armorItem = (ArmorItem) item;				
			return armorItem.getEquipmentSlot().getSlotIndex();
		}
		return 0;
	};

	private static int getArmorDamageReduce(ItemStack itemStack) {
		Item item = itemStack.getItem();	
		if (item instanceof ArmorItem) {
			ArmorItem armorItem = (ArmorItem) item;				
			return armorItem.getDamageReduceAmount();
		}
		return Integer.MIN_VALUE;
	};

	private static float getArmorToughness(ItemStack itemStack) {
		Item item = itemStack.getItem();	
		if (item instanceof ArmorItem) {
			ArmorItem armorItem = (ArmorItem) item;				
			return armorItem.getToughness();
		}
		return Float.MIN_VALUE;
	};

	private static int getArmorDurability(ItemStack itemStack) {
		if (isArmor(itemStack))
			return getMaxDamage(itemStack);
		return 0;
	}

	private static String getTagForSorting (IIngredientListElementInfo<?> elementInfo) {
		IIngredientManager ingredientManager = Internal.getIngredientManager();
		Collection<ResourceLocation> tags = elementInfo.getTagIds(ingredientManager);
		String bestTag = "";
		if (tags.size() > 0 ) {
			int maxTagSize = Integer.MIN_VALUE;
			//Group things by the most popular tag it has.
			for (ResourceLocation tag : tags) {			
				//TODO: make a tag blacklist.
				if (!tag.toString().equals("itemfilters:check_nbt")) {
					int thisTagSize = ItemTags.getCollection().getTagByID(tag).getAllElements().size();
					if (thisTagSize > maxTagSize) {
						bestTag = tag.getPath();
						maxTagSize = thisTagSize;
					}
				}
			}
		}
		return bestTag;
	};

	private static boolean hasTag(IIngredientListElementInfo<?> elementInfo){
		//Sort non-tools after the tools.
		return !getTagForSorting(elementInfo).isEmpty();
	};

	private static String getToolClass(ItemStack itemStack)
    {		
        return IngredientUtils.getToolClass(itemStack);
    }

}
