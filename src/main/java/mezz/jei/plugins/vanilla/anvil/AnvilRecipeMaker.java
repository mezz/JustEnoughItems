package mezz.jei.plugins.vanilla.anvil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
import mezz.jei.util.ErrorUtil;
import mezz.jei.util.Log;

public final class AnvilRecipeMaker {
	private static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.ENCHANTED_BOOK);

	private AnvilRecipeMaker() {
	}

	public static List<IRecipeWrapper> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientRegistry ingredientRegistry) {
		List<IRecipeWrapper> recipes = new ArrayList<>();
		Stopwatch sw = Stopwatch.createStarted();
		try {
			getRepairRecipes(recipes, vanillaRecipeFactory);
		} catch (RuntimeException e) {
			Log.get().error("Failed to create repair recipes.", e);
		}
		sw.stop();
		Log.get().debug("Registered vanilla repair recipes in {}", sw);
		sw.reset();
		sw.start();
		try {
			getBookEnchantmentRecipes(recipes, vanillaRecipeFactory, ingredientRegistry);
		} catch (RuntimeException e) {
			Log.get().error("Failed to create enchantment recipes.", e);
		}
		sw.stop();
		Log.get().debug("Registered enchantment recipes in {}", sw);
		return recipes;
	}

	private static void getBookEnchantmentRecipes(List<IRecipeWrapper> recipes, IVanillaRecipeFactory vanillaRecipeFactory, IIngredientRegistry ingredientRegistry) {
		Collection<ItemStack> ingredients = ingredientRegistry.getAllIngredients(VanillaTypes.ITEM);
		Collection<Enchantment> enchantments = ForgeRegistries.ENCHANTMENTS.getValuesCollection();
		for (ItemStack ingredient : ingredients) {
			if (ingredient.isItemEnchantable()) {
				for (Enchantment enchantment : enchantments) {
					if (enchantment.canApply(ingredient)) {
						try {
							getBookEnchantmentRecipes(recipes, vanillaRecipeFactory, enchantment, ingredient);
						} catch (RuntimeException e) {
							String ingredientInfo = ErrorUtil.getIngredientInfo(ingredient);
							Log.get().error("Failed to register book enchantment recipes for ingredient: {}", ingredientInfo, e);
						}
					}
				}
			}
		}
	}

	private static void getBookEnchantmentRecipes(List<IRecipeWrapper> recipes, IVanillaRecipeFactory vanillaRecipeFactory, Enchantment enchantment, ItemStack ingredient) {
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
			IRecipeWrapper anvilRecipe = vanillaRecipeFactory.createAnvilRecipe(ingredient, perLevelBooks, perLevelOutputs);
			recipes.add(anvilRecipe);
		}
	}

	private static void getRepairRecipes(List<IRecipeWrapper> recipes, IVanillaRecipeFactory vanillaRecipeFactory) {
		Map<ItemStack, List<ItemStack>> items = Maps.newHashMap();

		ItemStack repairWood = new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE);
		items.put(repairWood, Lists.newArrayList(
			new ItemStack(Items.WOODEN_SWORD),
			new ItemStack(Items.WOODEN_PICKAXE),
			new ItemStack(Items.WOODEN_AXE),
			new ItemStack(Items.WOODEN_SHOVEL),
			new ItemStack(Items.WOODEN_HOE),
			new ItemStack(Items.SHIELD)
		));

		ItemStack repairStone = new ItemStack(Blocks.COBBLESTONE);
		items.put(repairStone, Lists.newArrayList(
			new ItemStack(Items.STONE_SWORD),
			new ItemStack(Items.STONE_PICKAXE),
			new ItemStack(Items.STONE_AXE),
			new ItemStack(Items.STONE_SHOVEL),
			new ItemStack(Items.STONE_HOE)
		));

		ItemStack repairLeather = new ItemStack(Items.LEATHER);
		items.put(repairLeather, Lists.newArrayList(
			new ItemStack(Items.LEATHER_HELMET),
			new ItemStack(Items.LEATHER_CHESTPLATE),
			new ItemStack(Items.LEATHER_LEGGINGS),
			new ItemStack(Items.LEATHER_BOOTS),
			new ItemStack(Items.ELYTRA)
		));

		ItemStack repairIron = new ItemStack(Items.IRON_INGOT);
		items.put(repairIron, Lists.newArrayList(
			new ItemStack(Items.IRON_SWORD),
			new ItemStack(Items.IRON_PICKAXE),
			new ItemStack(Items.IRON_AXE),
			new ItemStack(Items.IRON_SHOVEL),
			new ItemStack(Items.IRON_HOE),
			new ItemStack(Items.IRON_HELMET),
			new ItemStack(Items.IRON_CHESTPLATE),
			new ItemStack(Items.IRON_LEGGINGS),
			new ItemStack(Items.IRON_BOOTS),
			new ItemStack(Items.CHAINMAIL_HELMET),
			new ItemStack(Items.CHAINMAIL_CHESTPLATE),
			new ItemStack(Items.CHAINMAIL_LEGGINGS),
			new ItemStack(Items.CHAINMAIL_BOOTS)
		));

		ItemStack repairGold = new ItemStack(Items.GOLD_INGOT);
		items.put(repairGold, Lists.newArrayList(
			new ItemStack(Items.GOLDEN_SWORD),
			new ItemStack(Items.GOLDEN_PICKAXE),
			new ItemStack(Items.GOLDEN_AXE),
			new ItemStack(Items.GOLDEN_SHOVEL),
			new ItemStack(Items.GOLDEN_HOE),
			new ItemStack(Items.GOLDEN_HELMET),
			new ItemStack(Items.GOLDEN_CHESTPLATE),
			new ItemStack(Items.GOLDEN_LEGGINGS),
			new ItemStack(Items.GOLDEN_BOOTS)
		));

		ItemStack repairDiamond = new ItemStack(Items.DIAMOND);
		items.put(repairDiamond, Lists.newArrayList(
			new ItemStack(Items.DIAMOND_SWORD),
			new ItemStack(Items.DIAMOND_PICKAXE),
			new ItemStack(Items.DIAMOND_AXE),
			new ItemStack(Items.DIAMOND_SHOVEL),
			new ItemStack(Items.DIAMOND_HOE),
			new ItemStack(Items.DIAMOND_HELMET),
			new ItemStack(Items.DIAMOND_CHESTPLATE),
			new ItemStack(Items.DIAMOND_LEGGINGS),
			new ItemStack(Items.DIAMOND_BOOTS)
		));

		for (Map.Entry<ItemStack, List<ItemStack>> entry : items.entrySet()) {

			ItemStack repairMaterial = entry.getKey();

			for (ItemStack ingredient : entry.getValue()) {

				ItemStack damaged1 = ingredient.copy();
				damaged1.setItemDamage(damaged1.getMaxDamage());
				ItemStack damaged2 = ingredient.copy();
				damaged2.setItemDamage(damaged2.getMaxDamage() * 3 / 4);
				ItemStack damaged3 = ingredient.copy();
				damaged3.setItemDamage(damaged3.getMaxDamage() * 2 / 4);

				IRecipeWrapper repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(damaged1, Collections.singletonList(repairMaterial), Collections.singletonList(damaged2));
				IRecipeWrapper repairWithSame = vanillaRecipeFactory.createAnvilRecipe(damaged2, Collections.singletonList(damaged2), Collections.singletonList(damaged3));
				recipes.add(repairWithMaterial);
				recipes.add(repairWithSame);
			}
		}
	}

	public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null) {
			return -1;
		}
		InventoryPlayer fakeInventory = new InventoryPlayer(player);
		try {
			ContainerRepair repair = new ContainerRepair(fakeInventory, player.world, player);
			repair.inventorySlots.get(0).putStack(leftStack);
			repair.inventorySlots.get(1).putStack(rightStack);
			return repair.maximumCost;
		} catch (RuntimeException e) {
			String left = ErrorUtil.getItemStackInfo(leftStack);
			String right = ErrorUtil.getItemStackInfo(rightStack);
			Log.get().error("Could not get anvil level cost for: ({} and {}).", left, right, e);
			return -1;
		}
	}
}
