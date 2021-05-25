package mezz.jei.ingredients;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.IIngredientFilterConfig;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.util.Translator;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IngredientListElementInfo<V> implements IIngredientListElementInfo<V> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

	private final IIngredientListElement<V> element;
	private final String displayName;
	private final List<String> modIds;
	private final List<String> modNames;
	private final String resourceId;

	private final int orderIndex;         //Maybe if I implement InvTools Tree reading in JEI.
	private final int damage;             //Not sure if this is even useful.
	private final String toolClass;       //For segregating the tools.
	private final int harvestLevel;       //For quality of the tools.
	private final Double attackDamage;    //For quality of weapons.
	private final Double attackSpeed;     //For quality of weapons.
	private final boolean isArmorFlag;    //For Armor Sorting
	private final int armorSlotIndex;     //For Armor Sorting
	private final int armorDamageReduce;  //For quality of armor.
	private final float armorToughness;   //For quality of armor.
	private final int maxDamage;          //For quality of damagable items.
	private final String bestTag;         //For grouping like items.
	

	@Nullable
	public static <V> IIngredientListElementInfo<V> create(IIngredientListElement<V> element, IIngredientManager ingredientManager, IModIdHelper modIdHelper) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		try {
			return new IngredientListElementInfo<>(element, ingredientHelper, modIdHelper);
		} catch (RuntimeException e) {
			try {
				String ingredientInfo = ingredientHelper.getErrorInfo(ingredient);
				LOGGER.warn("Found a broken ingredient {}", ingredientInfo, e);
			} catch (RuntimeException e2) {
				LOGGER.warn("Found a broken ingredient.", e2);
			}
			return null;
		}
	}

	protected IngredientListElementInfo(IIngredientListElement<V> element, IIngredientHelper<V> ingredientHelper, IModIdHelper modIdHelper) {
		this.element = element;
		V ingredient = element.getIngredient();
		String displayModId = ingredientHelper.getDisplayModId(ingredient);
		String modId = ingredientHelper.getModId(ingredient);
		this.modIds = new ArrayList<>();
		this.modIds.add(displayModId);
		if (!modId.equals(displayModId)) {
			this.modIds.add(modId);
		}
		this.modNames = this.modIds.stream()
			.map(modIdHelper::getModNameForModId)
			.collect(Collectors.toList());
		this.displayName = IngredientInformation.getDisplayName(ingredient, ingredientHelper);
		this.resourceId = ingredientHelper.getResourceId(ingredient);
		ItemStack itemStack = ingredientHelper.getCheatItemStack(ingredient);
		if (itemStack != null) {
			Item item = itemStack.getItem();
			this.damage = itemStack.getDamage();
			this.maxDamage = itemStack.getMaxDamage();
			//These are framework dependent.
			this.toolClass = getToolClass(itemStack, item);
			this.harvestLevel = (itemStack.getToolTypes().size() > 0 ? itemStack.getHarvestLevel(itemStack.getToolTypes().iterator().next(), null, null) : -1);
			
			Multimap<Attribute, AttributeModifier> multimap = itemStack.getAttributeModifiers(EquipmentSlotType.MAINHAND);
			boolean hasDamage = multimap.containsKey(Attributes.ATTACK_DAMAGE);
			boolean hasSpeed = multimap.containsKey(Attributes.ATTACK_SPEED);			
			if (hasDamage) {
				Collection<AttributeModifier> damageMap = multimap.get(Attributes.ATTACK_DAMAGE);
				this.attackDamage = ((AttributeModifier) damageMap.toArray()[0]).getAmount();
				if (hasSpeed) {
					Collection<AttributeModifier> speedMap = multimap.get(Attributes.ATTACK_SPEED);
					this.attackSpeed = ((AttributeModifier) speedMap.toArray()[0]).getAmount();
				}
				else {
					this.attackSpeed = Double.MIN_VALUE;
				}
			} else {
				this.attackDamage = Double.MIN_VALUE;
				this.attackSpeed = Double.MIN_VALUE;		
			}
		
			if (item instanceof ArmorItem) {
				this.isArmorFlag = true;
				ArmorItem armorItem = (ArmorItem) item;				
				this.armorSlotIndex = armorItem.getEquipmentSlot().getSlotIndex();
				this.armorDamageReduce = armorItem.getDamageReduceAmount();
				this.armorToughness = armorItem.getToughness();
			} else {
				this.isArmorFlag = false;
				this.armorSlotIndex = 0;
				this.armorDamageReduce = Integer.MIN_VALUE;
				this.armorToughness = Float.MIN_VALUE;				
			}
		}
		else {
			this.damage = 0;
			this.toolClass = "";
			this.harvestLevel = -1;
			this.attackDamage = Double.MIN_VALUE;
			this.attackSpeed = Double.MIN_VALUE;
			this.armorSlotIndex = 0;
			this.armorDamageReduce = Integer.MIN_VALUE;
			this.armorToughness = Float.MIN_VALUE;
			this.isArmorFlag = false;
			this.maxDamage = 0;
			}
		this.orderIndex = 0;
		
		Collection<ResourceLocation> tags = ingredientHelper.getTags(ingredient);
		if (tags.size() > 0 ) {
			ResourceLocation maxTag = Items.AIR.getRegistryName();
			int maxTagSize = Integer.MIN_VALUE;
			//Group things by the most popular tag it has.
			for (ResourceLocation tag : tags) {			
				int thisTagSize = ItemTags.getCollection().getTagByID(tag).getAllElements().size();
				if (thisTagSize > maxTagSize) {
					maxTag = tag;
					maxTagSize = thisTagSize;
				}
			}
			bestTag = maxTag.toString();
		}
		else {
			bestTag = "";
		}		

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
		V ingredient = element.getIngredient();
		IIngredientRenderer<V> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredient);
		return IngredientInformation.getTooltipStrings(ingredient, ingredientRenderer, ImmutableSet.of(modId, modNameLowercase, displayNameLowercase, resourceId), config);
	}

	@Override
	public Collection<String> getTagStrings(IIngredientManager ingredientManager) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		Collection<ResourceLocation> tags = ingredientHelper.getTags(ingredient);
		return tags.stream()
			.map(ResourceLocation::getPath)
			.collect(Collectors.toList());
	}

	@Override
	public Collection<String> getCreativeTabsStrings(IIngredientManager ingredientManager) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		Collection<String> creativeTabsStrings = ingredientHelper.getCreativeTabNames(ingredient);
		return creativeTabsStrings.stream()
			.map(Translator::toLowercaseWithLocale)
			.collect(Collectors.toList());
	}

	@Override
	public Collection<String> getColorStrings(IIngredientManager ingredientManager) {
		V ingredient = element.getIngredient();
		IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
		return IngredientInformation.getColorStrings(ingredient, ingredientHelper);
	}

	@Override
	public String getResourceId() {
		return resourceId;
	}

	@Override
	public IIngredientListElement<V> getElement() {
		return element;
	}

	@Override
	public int getOrderIndex() {
		return orderIndex;
	};

	@Override
	public int getDamage() {
		return damage;
	};

	@Override
	public int getMaxDamage() {
		return maxDamage;
	};

	@Override
	public String getToolClass() {
		return toolClass;
	};
	
	@Override
	public int getHarvestLevel() {
		return harvestLevel;
	};

	@Override
	public boolean isTool(){
		//Sort non-tools after the tools.
		return !toolClass.isEmpty();
	};

	@Override
	public int getToolDurability() {
		if (isTool())
			return maxDamage;
		return 0;
	}

	@Override
	public boolean isWeapon() {
		//Sort Weapons apart from tools, armor, and other random things..
		//0 did not successfully filter out the non-weapons. 1/1024 should do the trick.
		return toolClass.isEmpty() && attackDamage > 0.000976562f && !isArmorFlag;
	};

	@Override
	public Double getAttackDamage() {
		if (isWeapon()) {
			return attackDamage;
		}
		return Double.MIN_VALUE;
	};

	@Override
	public Double getAttackSpeed() {
		if (isWeapon()) {
			return attackSpeed;
		}
		return Double.MIN_VALUE;
	};

	@Override
	public int getWeaponDurability() {
		if (isWeapon()) {
			return maxDamage;
		}
		return 0;
	}

	@Override
	public boolean isArmor() {
		return isArmorFlag;
	};

	@Override
	public int getArmorSlotIndex() {
		if (isArmor())
			return armorSlotIndex;
		return 0;
	};

	@Override
	public int getArmorDamageReduce() {
		return armorDamageReduce;
	};

	@Override
	public float getArmorToughness() {
		return armorToughness;
	};

	@Override
	public int getArmorDurability() {
		if (isArmor())
			return maxDamage;
		return 0;
	}


	@Override
	public String getTagForSorting() {
		return bestTag;
	};

	@Override
	public boolean hasTag(){
		//Sort non-tools after the tools.
		return !bestTag.isEmpty();
	};



	private static String getToolClass(ItemStack itemStack, Item item)
    {
        if (itemStack == null || item == null) return "";
        Set<ToolType> toolTypeSet = item.getToolTypes(itemStack);
        
        Set<String> toolClassSet = new HashSet<String>();

        for (ToolType toolClass: toolTypeSet) {
            //Swords are not "tools".
            if (toolClass.getName() != "sword") {
            	toolClassSet.add(toolClass.getName());
            }
        }

        //Minecraft hoes, shears, and fishing rods don't have tool class names.
        if (toolClassSet.isEmpty()) {
            if (item instanceof HoeItem) return "hoe";
            if (item instanceof ShearsItem) return "shears";
            if (item instanceof FishingRodItem) return "fishingrod";
            return "";
        }
        
        //Get the only thing.
        if (toolClassSet.size() == 1)
            return (String) toolClassSet.toArray()[0];
        
        //We have a preferred type to list tools under, primarily the pickaxe for harvest level.
        String[] prefOrder = {"pickaxe", "axe", "shovel", "hoe", "shears", "wrench"};
        for (int i = 0; i < prefOrder.length; i++)
            if (toolClassSet.contains(prefOrder[i])) 
                return prefOrder[i];
        
        //Whatever happens to be the first thing:
        return (String) toolClassSet.toArray()[0];
    }

}
