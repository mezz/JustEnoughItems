package mezz.jei.plugins.vanilla.anvil;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import mezz.jei.api.IModRegistry;
import mezz.jei.util.Log;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AnvilRecipeMaker {

	public static void registerVanillaAnvilRecipes(IModRegistry registry) {
		Stopwatch sw = Stopwatch.createStarted();
		registerRepairRecipes(registry);
		sw.stop();
		Log.info("Registered vanilla repair recipes in %s ms", sw.elapsed(TimeUnit.MILLISECONDS));
		sw.reset();
		sw.start();
		registerBookEnchantmentRecipes(registry);
		sw.stop();
		Log.info("Registered enchantment recipes in %s ms", sw.elapsed(TimeUnit.MILLISECONDS));
	}

	private static void registerBookEnchantmentRecipes(IModRegistry registry) {
		List<ItemStack> ingredients = registry.getIngredientRegistry().getIngredients(ItemStack.class);
		List<Enchantment> enchantments = ForgeRegistries.ENCHANTMENTS.getValues();
		ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
		for (ItemStack ingredient : ingredients) {
			for (Enchantment enchantment : enchantments) {
				if (enchantment.canApply(ingredient)) {
					for (int level = 1; level <= enchantment.getMaxLevel(); level++) {
						ItemStack withEnchant = ingredient.copy();
						ItemStack bookEnchant = book.copy();
						Map<Enchantment, Integer> enchMap = Collections.singletonMap(enchantment, level);
						EnchantmentHelper.setEnchantments(enchMap, withEnchant);
						EnchantmentHelper.setEnchantments(enchMap, bookEnchant);
						registry.addAnvilRecipe(ingredient, bookEnchant, withEnchant);
					}
				}
			}
		}
	}

	private static <T1, T2> Pair<T1, T2> pairOf(T1 left, T2 right) { return new Pair<T1, T2>(left, right); }

	@SuppressWarnings("unchecked")
	private static void registerRepairRecipes(IModRegistry registry) {
		ItemStack repairWood = new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE);
		ItemStack repairStone = new ItemStack(Blocks.COBBLESTONE);
		ItemStack repairIron = new ItemStack(Items.IRON_INGOT);
		ItemStack repairGold = new ItemStack(Items.GOLD_INGOT);
		ItemStack repairDiamond = new ItemStack(Items.DIAMOND);
		ItemStack repairLeather = new ItemStack(Items.LEATHER);
		List<Pair<ItemStack,ItemStack>> items = Lists.newArrayList(
				pairOf(new ItemStack(Items.WOODEN_SWORD), repairWood),
				pairOf(new ItemStack(Items.WOODEN_PICKAXE), repairWood),
				pairOf(new ItemStack(Items.WOODEN_AXE), repairWood),
				pairOf(new ItemStack(Items.WOODEN_SHOVEL), repairWood),
				pairOf(new ItemStack(Items.WOODEN_HOE), repairWood),
				pairOf(new ItemStack(Items.STONE_SWORD), repairStone),
				pairOf(new ItemStack(Items.STONE_PICKAXE), repairStone),
				pairOf(new ItemStack(Items.STONE_AXE), repairStone),
				pairOf(new ItemStack(Items.STONE_SHOVEL), repairStone),
				pairOf(new ItemStack(Items.STONE_HOE), repairStone),
				pairOf(new ItemStack(Items.LEATHER_HELMET), repairLeather),
				pairOf(new ItemStack(Items.LEATHER_CHESTPLATE), repairLeather),
				pairOf(new ItemStack(Items.LEATHER_LEGGINGS), repairLeather),
				pairOf(new ItemStack(Items.LEATHER_BOOTS), repairLeather),
				pairOf(new ItemStack(Items.IRON_SWORD), repairIron),
				pairOf(new ItemStack(Items.IRON_PICKAXE), repairIron),
				pairOf(new ItemStack(Items.IRON_AXE), repairIron),
				pairOf(new ItemStack(Items.IRON_SHOVEL), repairIron),
				pairOf(new ItemStack(Items.IRON_HOE), repairIron),
				pairOf(new ItemStack(Items.IRON_HELMET), repairIron),
				pairOf(new ItemStack(Items.IRON_CHESTPLATE), repairIron),
				pairOf(new ItemStack(Items.IRON_LEGGINGS), repairIron),
				pairOf(new ItemStack(Items.IRON_BOOTS), repairIron),
				pairOf(new ItemStack(Items.CHAINMAIL_HELMET), repairIron),
				pairOf(new ItemStack(Items.CHAINMAIL_CHESTPLATE), repairIron),
				pairOf(new ItemStack(Items.CHAINMAIL_LEGGINGS), repairIron),
				pairOf(new ItemStack(Items.CHAINMAIL_BOOTS), repairIron),
				pairOf(new ItemStack(Items.GOLDEN_SWORD), repairGold),
				pairOf(new ItemStack(Items.GOLDEN_PICKAXE), repairGold),
				pairOf(new ItemStack(Items.GOLDEN_AXE), repairGold),
				pairOf(new ItemStack(Items.GOLDEN_SHOVEL), repairGold),
				pairOf(new ItemStack(Items.GOLDEN_HOE), repairGold),
				pairOf(new ItemStack(Items.GOLDEN_HELMET), repairGold),
				pairOf(new ItemStack(Items.GOLDEN_CHESTPLATE), repairGold),
				pairOf(new ItemStack(Items.GOLDEN_LEGGINGS), repairGold),
				pairOf(new ItemStack(Items.GOLDEN_BOOTS), repairGold),
				pairOf(new ItemStack(Items.DIAMOND_SWORD), repairDiamond),
				pairOf(new ItemStack(Items.DIAMOND_PICKAXE), repairDiamond),
				pairOf(new ItemStack(Items.DIAMOND_AXE), repairDiamond),
				pairOf(new ItemStack(Items.DIAMOND_SHOVEL), repairDiamond),
				pairOf(new ItemStack(Items.DIAMOND_HOE), repairDiamond),
				pairOf(new ItemStack(Items.DIAMOND_HELMET), repairDiamond),
				pairOf(new ItemStack(Items.DIAMOND_CHESTPLATE), repairDiamond),
				pairOf(new ItemStack(Items.DIAMOND_LEGGINGS), repairDiamond),
				pairOf(new ItemStack(Items.DIAMOND_BOOTS), repairDiamond),
				pairOf(new ItemStack(Items.SHIELD), repairWood),
				pairOf(new ItemStack(Items.ELYTRA), repairLeather)
				);
		for (Pair<ItemStack,ItemStack> entry : items) {
			ItemStack damaged1 = entry.getKey().copy();
			damaged1.setItemDamage(damaged1.getMaxDamage() * 3 / 4);
			ItemStack damaged2 = entry.getKey().copy();
			damaged2.setItemDamage(damaged2.getMaxDamage() / 2);
			ItemStack damaged3 = entry.getKey().copy();
			damaged3.setItemDamage(damaged2.getMaxDamage() / 4);
			registry.addAnvilRecipe(damaged2, entry.getValue(), damaged3);
			registry.addAnvilRecipe(damaged1, damaged2, damaged3);
		}
	}
}
