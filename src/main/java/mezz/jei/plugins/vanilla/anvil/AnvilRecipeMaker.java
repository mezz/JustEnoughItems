package mezz.jei.plugins.vanilla.anvil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

	public static List<IJeiAnvilRecipe> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
		List<IJeiAnvilRecipe> recipes = new ArrayList<>();
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

	private static void getBookEnchantmentRecipes(
		List<IJeiAnvilRecipe> recipes,
		IVanillaRecipeFactory vanillaRecipeFactory,
		IIngredientManager ingredientManager
	) {
		Collection<ItemStack> ingredients = ingredientManager.getAllIngredients(VanillaTypes.ITEM);
		Collection<Enchantment> enchantments = ForgeRegistries.ENCHANTMENTS.getValues();
		ingredients.stream()
			.filter(ItemStack::isEnchantable)
			.forEach(ingredient ->
				enchantments.stream()
					.filter(enchantment -> enchantment.canEnchant(ingredient))
					.forEach(enchantment -> {
						try {
							getBookEnchantmentRecipes(recipes, vanillaRecipeFactory, enchantment, ingredient);
						} catch (RuntimeException e) {
							String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient, VanillaTypes.ITEM);
							LOGGER.error("Failed to register book enchantment recipes for ingredient: {}", ingredientInfo, e);
						}
					})
			);
	}

	private static void getBookEnchantmentRecipes(List<IJeiAnvilRecipe> recipes, IVanillaRecipeFactory vanillaRecipeFactory, Enchantment enchantment, ItemStack ingredient) {
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
			IJeiAnvilRecipe anvilRecipe = vanillaRecipeFactory.createAnvilRecipe(ingredient, perLevelBooks, perLevelOutputs);
			recipes.add(anvilRecipe);
		}
	}

	private static void getRepairRecipes(List<IJeiAnvilRecipe> recipes, IVanillaRecipeFactory vanillaRecipeFactory) {
		Map<Ingredient, List<ItemStack>> items = Maps.newHashMap();

		Ingredient repairWoods = Tiers.WOOD.getRepairIngredient();
		items.put(repairWoods, Lists.newArrayList(
			new ItemStack(Items.WOODEN_SWORD),
			new ItemStack(Items.WOODEN_PICKAXE),
			new ItemStack(Items.WOODEN_AXE),
			new ItemStack(Items.WOODEN_SHOVEL),
			new ItemStack(Items.WOODEN_HOE)
		));

		Ingredient repairShields = Ingredient.of(ItemTags.PLANKS);
		items.put(repairShields, Lists.newArrayList(
			new ItemStack(Items.SHIELD)
		));

		items.put(Tiers.STONE.getRepairIngredient(), Lists.newArrayList(
			new ItemStack(Items.STONE_SWORD),
			new ItemStack(Items.STONE_PICKAXE),
			new ItemStack(Items.STONE_AXE),
			new ItemStack(Items.STONE_SHOVEL),
			new ItemStack(Items.STONE_HOE)
		));

		Ingredient repairLeather = ArmorMaterials.LEATHER.getRepairIngredient();
		items.put(repairLeather, Lists.newArrayList(
			new ItemStack(Items.LEATHER_HELMET),
			new ItemStack(Items.LEATHER_CHESTPLATE),
			new ItemStack(Items.LEATHER_LEGGINGS),
			new ItemStack(Items.LEATHER_BOOTS)
		));

		Ingredient repairIronItems = Tiers.IRON.getRepairIngredient();
		items.put(repairIronItems, Lists.newArrayList(
			new ItemStack(Items.IRON_SWORD),
			new ItemStack(Items.IRON_PICKAXE),
			new ItemStack(Items.IRON_AXE),
			new ItemStack(Items.IRON_SHOVEL),
			new ItemStack(Items.IRON_HOE)
		));

		Ingredient repairIronArmor = ArmorMaterials.IRON.getRepairIngredient();
		items.put(repairIronArmor, Lists.newArrayList(
			new ItemStack(Items.IRON_HELMET),
			new ItemStack(Items.IRON_CHESTPLATE),
			new ItemStack(Items.IRON_LEGGINGS),
			new ItemStack(Items.IRON_BOOTS)
		));

		Ingredient repairChain = ArmorMaterials.CHAIN.getRepairIngredient();
		items.put(repairChain, Lists.newArrayList(
			new ItemStack(Items.CHAINMAIL_HELMET),
			new ItemStack(Items.CHAINMAIL_CHESTPLATE),
			new ItemStack(Items.CHAINMAIL_LEGGINGS),
			new ItemStack(Items.CHAINMAIL_BOOTS)
		));

		Ingredient repairGoldItems = Tiers.GOLD.getRepairIngredient();
		items.put(repairGoldItems, Lists.newArrayList(
			new ItemStack(Items.GOLDEN_SWORD),
			new ItemStack(Items.GOLDEN_PICKAXE),
			new ItemStack(Items.GOLDEN_AXE),
			new ItemStack(Items.GOLDEN_SHOVEL),
			new ItemStack(Items.GOLDEN_HOE)
		));

		Ingredient repairGoldArmor = ArmorMaterials.GOLD.getRepairIngredient();
		items.put(repairGoldArmor, Lists.newArrayList(
			new ItemStack(Items.GOLDEN_HELMET),
			new ItemStack(Items.GOLDEN_CHESTPLATE),
			new ItemStack(Items.GOLDEN_LEGGINGS),
			new ItemStack(Items.GOLDEN_BOOTS)
		));

		Ingredient repairDiamondItems = Tiers.DIAMOND.getRepairIngredient();
		items.put(repairDiamondItems, Lists.newArrayList(
			new ItemStack(Items.DIAMOND_SWORD),
			new ItemStack(Items.DIAMOND_PICKAXE),
			new ItemStack(Items.DIAMOND_AXE),
			new ItemStack(Items.DIAMOND_SHOVEL),
			new ItemStack(Items.DIAMOND_HOE)
		));

		Ingredient repairDiamondArmor = ArmorMaterials.DIAMOND.getRepairIngredient();
		items.put(repairDiamondArmor, Lists.newArrayList(
			new ItemStack(Items.DIAMOND_HELMET),
			new ItemStack(Items.DIAMOND_CHESTPLATE),
			new ItemStack(Items.DIAMOND_LEGGINGS),
			new ItemStack(Items.DIAMOND_BOOTS)
		));

		Ingredient repairNetheriteItems = Tiers.NETHERITE.getRepairIngredient();
		items.put(repairNetheriteItems, Lists.newArrayList(
			new ItemStack(Items.NETHERITE_SWORD),
			new ItemStack(Items.NETHERITE_AXE),
			new ItemStack(Items.NETHERITE_HOE),
			new ItemStack(Items.NETHERITE_SHOVEL),
			new ItemStack(Items.NETHERITE_PICKAXE)
		));

		Ingredient repairNetheriteArmor = ArmorMaterials.NETHERITE.getRepairIngredient();
		items.put(repairNetheriteArmor, Lists.newArrayList(
			new ItemStack(Items.NETHERITE_BOOTS),
			new ItemStack(Items.NETHERITE_HELMET),
			new ItemStack(Items.NETHERITE_LEGGINGS),
			new ItemStack(Items.NETHERITE_CHESTPLATE)
		));

		Ingredient repairElytra = Ingredient.of(Items.PHANTOM_MEMBRANE);
		items.put(repairElytra, Lists.newArrayList(
			new ItemStack(Items.ELYTRA)
		));

		Ingredient repairTurtle = ArmorMaterials.TURTLE.getRepairIngredient();
		items.put(repairTurtle, Lists.newArrayList(
			new ItemStack(Items.TURTLE_HELMET)
		));

		for (Map.Entry<Ingredient, List<ItemStack>> entry : items.entrySet()) {

			List<ItemStack> repairMaterials = Lists.newArrayList(
				entry.getKey().getItems()
			);

			for (ItemStack ingredient : entry.getValue()) {

				ItemStack damaged1 = ingredient.copy();
				damaged1.setDamageValue(damaged1.getMaxDamage());
				ItemStack damaged2 = ingredient.copy();
				damaged2.setDamageValue(damaged2.getMaxDamage() * 3 / 4);
				ItemStack damaged3 = ingredient.copy();
				damaged3.setDamageValue(damaged3.getMaxDamage() * 2 / 4);

				if (!repairMaterials.isEmpty()) {
					IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(damaged1, repairMaterials, Collections.singletonList(damaged2));
					recipes.add(repairWithMaterial);
				}
				IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(damaged2, Collections.singletonList(damaged2), Collections.singletonList(damaged3));
				recipes.add(repairWithSame);
			}
		}
	}

	public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
		Player player = Minecraft.getInstance().player;
		if (player == null) {
			return -1;
		}
		Inventory fakeInventory = new Inventory(player);
		try {
			AnvilMenu repair = new AnvilMenu(0, fakeInventory);
			repair.slots.get(0).set(leftStack);
			repair.slots.get(1).set(rightStack);
			return repair.getCost();
		} catch (RuntimeException e) {
			String left = ErrorUtil.getItemStackInfo(leftStack);
			String right = ErrorUtil.getItemStackInfo(rightStack);
			LOGGER.error("Could not get anvil level cost for: ({} and {}).", left, right, e);
			return -1;
		}
	}
}
