package mezz.jei.plugins.vanilla.anvil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemTier;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class AnvilRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.ENCHANTED_BOOK);

	private AnvilRecipeMaker() {
	}

	public static List<Object> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
		List<Object> recipes = new ArrayList<>();
		Stopwatch sw = Stopwatch.createStarted();
		try {
			getRepairRecipes(recipes, vanillaRecipeFactory);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to create repair recipes.", e);
		}
		sw.stop();
		LOGGER.debug("Registered vanilla repair recipes in {}", sw);
		sw.reset();
		sw.start();
		try {
			getBookEnchantmentRecipes(recipes, vanillaRecipeFactory, ingredientManager);
		} catch (RuntimeException e) {
			LOGGER.error("Failed to create enchantment recipes.", e);
		}
		sw.stop();
		LOGGER.debug("Registered enchantment recipes in {}", sw);
		return recipes;
	}

	private static void getBookEnchantmentRecipes(List<Object> recipes, IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
		Collection<ItemStack> ingredients = ingredientManager.getAllIngredients(VanillaTypes.ITEM);
		Collection<Enchantment> enchantments = ForgeRegistries.ENCHANTMENTS.getValues();
		for (ItemStack ingredient : ingredients) {
			if (ingredient.isEnchantable()) {
				for (Enchantment enchantment : enchantments) {
					if (enchantment.canApply(ingredient)) {
						try {
							getBookEnchantmentRecipes(recipes, vanillaRecipeFactory, enchantment, ingredient);
						} catch (RuntimeException e) {
							String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient);
							LOGGER.error("Failed to register book enchantment recipes for ingredient: {}", ingredientInfo, e);
						}
					}
				}
			}
		}
	}

	private static void getBookEnchantmentRecipes(List<Object> recipes, IVanillaRecipeFactory vanillaRecipeFactory, Enchantment enchantment, ItemStack ingredient) {
		Item item = ingredient.getItem();
		List<ItemStack> perLevelBooks = Lists.newArrayList();
		List<ItemStack> perLevelOutputs = Lists.newArrayList();
		for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
			Map<Enchantment, Integer> enchMap = Collections.singletonMap(enchantment, level);

			ItemStack bookEnchant = ENCHANTED_BOOK.copy();
			EnchantmentHelper.setEnchantments(enchMap, bookEnchant);
			if (item.isBookEnchantable(ingredient, bookEnchant)) {
				perLevelBooks.add(bookEnchant);

				ItemStack withEnchant = ingredient.copy();
				EnchantmentHelper.setEnchantments(enchMap, withEnchant);
				perLevelOutputs.add(withEnchant);
			}
		}
		if (!perLevelBooks.isEmpty() && !perLevelOutputs.isEmpty()) {
			Object anvilRecipe = vanillaRecipeFactory.createAnvilRecipe(ingredient, perLevelBooks, perLevelOutputs);
			recipes.add(anvilRecipe);
		}
	}

	private static void getRepairRecipes(List<Object> recipes, IVanillaRecipeFactory vanillaRecipeFactory) {
		Map<Ingredient, List<ItemStack>> items = Maps.newHashMap();

		Ingredient repairWoods = ItemTier.WOOD.getRepairMaterial();
		items.put(repairWoods, Lists.newArrayList(
			new ItemStack(Items.WOODEN_SWORD),
			new ItemStack(Items.WOODEN_PICKAXE),
			new ItemStack(Items.WOODEN_AXE),
			new ItemStack(Items.WOODEN_SHOVEL),
			new ItemStack(Items.WOODEN_HOE)
		));

		Ingredient repairShields = Ingredient.fromTag(ItemTags.PLANKS);
		items.put(repairShields, Lists.newArrayList(
			new ItemStack(Items.SHIELD)
		));

		items.put(ItemTier.STONE.getRepairMaterial(), Lists.newArrayList(
			new ItemStack(Items.STONE_SWORD),
			new ItemStack(Items.STONE_PICKAXE),
			new ItemStack(Items.STONE_AXE),
			new ItemStack(Items.STONE_SHOVEL),
			new ItemStack(Items.STONE_HOE)
		));

		Ingredient repairLeather = ArmorMaterial.LEATHER.getRepairMaterial();
		items.put(repairLeather, Lists.newArrayList(
			new ItemStack(Items.LEATHER_HELMET),
			new ItemStack(Items.LEATHER_CHESTPLATE),
			new ItemStack(Items.LEATHER_LEGGINGS),
			new ItemStack(Items.LEATHER_BOOTS)
		));

		Ingredient repairIronItems = ItemTier.IRON.getRepairMaterial();
		items.put(repairIronItems, Lists.newArrayList(
			new ItemStack(Items.IRON_SWORD),
			new ItemStack(Items.IRON_PICKAXE),
			new ItemStack(Items.IRON_AXE),
			new ItemStack(Items.IRON_SHOVEL),
			new ItemStack(Items.IRON_HOE)
		));

		Ingredient repairIronArmor = ArmorMaterial.IRON.getRepairMaterial();
		items.put(repairIronArmor, Lists.newArrayList(
			new ItemStack(Items.IRON_HELMET),
			new ItemStack(Items.IRON_CHESTPLATE),
			new ItemStack(Items.IRON_LEGGINGS),
			new ItemStack(Items.IRON_BOOTS)
		));

		Ingredient repairChain = ArmorMaterial.CHAIN.getRepairMaterial();
		items.put(repairChain, Lists.newArrayList(
			new ItemStack(Items.CHAINMAIL_HELMET),
			new ItemStack(Items.CHAINMAIL_CHESTPLATE),
			new ItemStack(Items.CHAINMAIL_LEGGINGS),
			new ItemStack(Items.CHAINMAIL_BOOTS)
		));

		Ingredient repairGoldItems = ItemTier.GOLD.getRepairMaterial();
		items.put(repairGoldItems, Lists.newArrayList(
			new ItemStack(Items.GOLDEN_SWORD),
			new ItemStack(Items.GOLDEN_PICKAXE),
			new ItemStack(Items.GOLDEN_AXE),
			new ItemStack(Items.GOLDEN_SHOVEL),
			new ItemStack(Items.GOLDEN_HOE)
		));

		Ingredient repairGoldArmor = ArmorMaterial.GOLD.getRepairMaterial();
		items.put(repairGoldArmor, Lists.newArrayList(
			new ItemStack(Items.GOLDEN_HELMET),
			new ItemStack(Items.GOLDEN_CHESTPLATE),
			new ItemStack(Items.GOLDEN_LEGGINGS),
			new ItemStack(Items.GOLDEN_BOOTS)
		));

		Ingredient repairDiamondItems = ItemTier.DIAMOND.getRepairMaterial();
		items.put(repairDiamondItems, Lists.newArrayList(
			new ItemStack(Items.DIAMOND_SWORD),
			new ItemStack(Items.DIAMOND_PICKAXE),
			new ItemStack(Items.DIAMOND_AXE),
			new ItemStack(Items.DIAMOND_SHOVEL),
			new ItemStack(Items.DIAMOND_HOE)
		));

		Ingredient repairDiamondArmor = ArmorMaterial.DIAMOND.getRepairMaterial();
		items.put(repairDiamondArmor, Lists.newArrayList(
			new ItemStack(Items.DIAMOND_HELMET),
			new ItemStack(Items.DIAMOND_CHESTPLATE),
			new ItemStack(Items.DIAMOND_LEGGINGS),
			new ItemStack(Items.DIAMOND_BOOTS)
		));

		Ingredient repairNetheriteItems = ItemTier.NETHERITE.getRepairMaterial();
		items.put(repairNetheriteItems, Lists.newArrayList(
			new ItemStack(Items.field_234754_kI_),
			new ItemStack(Items.field_234756_kK_),
			new ItemStack(Items.field_234757_kL_),
			new ItemStack(Items.field_234755_kJ_),
			new ItemStack(Items.field_234758_kU_)
		));

		Ingredient repairNetheriteArmor = ArmorMaterial.NETHERITE.getRepairMaterial();
		items.put(repairNetheriteArmor, Lists.newArrayList(
			new ItemStack(Items.field_234763_ls_),
			new ItemStack(Items.field_234764_lt_),
			new ItemStack(Items.field_234765_lu_),
			new ItemStack(Items.field_234766_lv_)
		));

		Ingredient repairElytra = Ingredient.fromItems(Items.PHANTOM_MEMBRANE);
		items.put(repairElytra, Lists.newArrayList(
			new ItemStack(Items.ELYTRA)
		));

		Ingredient repairTurtle = ArmorMaterial.TURTLE.getRepairMaterial();
		items.put(repairTurtle, Lists.newArrayList(
			new ItemStack(Items.TURTLE_HELMET)
		));

		for (Map.Entry<Ingredient, List<ItemStack>> entry : items.entrySet()) {

			List<ItemStack> repairMaterials = Lists.newArrayList(
				entry.getKey().getMatchingStacks()
			);

			for (ItemStack ingredient : entry.getValue()) {

				ItemStack damaged1 = ingredient.copy();
				damaged1.setDamage(damaged1.getMaxDamage());
				ItemStack damaged2 = ingredient.copy();
				damaged2.setDamage(damaged2.getMaxDamage() * 3 / 4);
				ItemStack damaged3 = ingredient.copy();
				damaged3.setDamage(damaged3.getMaxDamage() * 2 / 4);

				if (!repairMaterials.isEmpty()) {
					Object repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(damaged1, repairMaterials, Collections.singletonList(damaged2));
					recipes.add(repairWithMaterial);
				}
				Object repairWithSame = vanillaRecipeFactory.createAnvilRecipe(damaged2, Collections.singletonList(damaged2), Collections.singletonList(damaged3));
				recipes.add(repairWithSame);
			}
		}
	}

	public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
		PlayerEntity player = Minecraft.getInstance().player;
		if (player == null) {
			return -1;
		}
		PlayerInventory fakeInventory = new PlayerInventory(player);
		try {
			RepairContainer repair = new RepairContainer(0, fakeInventory);
			repair.inventorySlots.get(0).putStack(leftStack);
			repair.inventorySlots.get(1).putStack(rightStack);
			return repair.getMaximumCost();
		} catch (RuntimeException e) {
			String left = ErrorUtil.getItemStackInfo(leftStack);
			String right = ErrorUtil.getItemStackInfo(rightStack);
			LOGGER.error("Could not get anvil level cost for: ({} and {}).", left, right, e);
			return -1;
		}
	}
}
