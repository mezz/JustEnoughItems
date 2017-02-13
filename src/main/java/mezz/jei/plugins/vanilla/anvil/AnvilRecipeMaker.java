package mezz.jei.plugins.vanilla.anvil;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import mezz.jei.api.IModRegistry;
import mezz.jei.util.Log;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

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

	@SuppressWarnings("unchecked")
	private static void registerRepairRecipes(IModRegistry registry) {
		ItemStack repairWood = new ItemStack(Blocks.PLANKS, 1, OreDictionary.WILDCARD_VALUE);
		ItemStack repairStone = new ItemStack(Blocks.COBBLESTONE);
		ItemStack repairIron = new ItemStack(Items.IRON_INGOT);
		ItemStack repairGold = new ItemStack(Items.GOLD_INGOT);
		ItemStack repairDiamond = new ItemStack(Items.DIAMOND);
		ItemStack repairLeather = new ItemStack(Items.LEATHER);
		List<Pair<ItemStack,ItemStack>> items = Lists.newArrayList(
				Pair.of(new ItemStack(Items.WOODEN_SWORD), repairWood),
				Pair.of(new ItemStack(Items.WOODEN_PICKAXE), repairWood),
				Pair.of(new ItemStack(Items.WOODEN_AXE), repairWood),
				Pair.of(new ItemStack(Items.WOODEN_SHOVEL), repairWood),
				Pair.of(new ItemStack(Items.WOODEN_HOE), repairWood),
				Pair.of(new ItemStack(Items.STONE_SWORD), repairStone),
				Pair.of(new ItemStack(Items.STONE_PICKAXE), repairStone),
				Pair.of(new ItemStack(Items.STONE_AXE), repairStone),
				Pair.of(new ItemStack(Items.STONE_SHOVEL), repairStone),
				Pair.of(new ItemStack(Items.STONE_HOE), repairStone),
				Pair.of(new ItemStack(Items.LEATHER_HELMET), repairLeather),
				Pair.of(new ItemStack(Items.LEATHER_CHESTPLATE), repairLeather),
				Pair.of(new ItemStack(Items.LEATHER_LEGGINGS), repairLeather),
				Pair.of(new ItemStack(Items.LEATHER_BOOTS), repairLeather),
				Pair.of(new ItemStack(Items.IRON_SWORD), repairIron),
				Pair.of(new ItemStack(Items.IRON_PICKAXE), repairIron),
				Pair.of(new ItemStack(Items.IRON_AXE), repairIron),
				Pair.of(new ItemStack(Items.IRON_SHOVEL), repairIron),
				Pair.of(new ItemStack(Items.IRON_HOE), repairIron),
				Pair.of(new ItemStack(Items.IRON_HELMET), repairIron),
				Pair.of(new ItemStack(Items.IRON_CHESTPLATE), repairIron),
				Pair.of(new ItemStack(Items.IRON_LEGGINGS), repairIron),
				Pair.of(new ItemStack(Items.IRON_BOOTS), repairIron),
				Pair.of(new ItemStack(Items.CHAINMAIL_HELMET), repairIron),
				Pair.of(new ItemStack(Items.CHAINMAIL_CHESTPLATE), repairIron),
				Pair.of(new ItemStack(Items.CHAINMAIL_LEGGINGS), repairIron),
				Pair.of(new ItemStack(Items.CHAINMAIL_BOOTS), repairIron),
				Pair.of(new ItemStack(Items.GOLDEN_SWORD), repairGold),
				Pair.of(new ItemStack(Items.GOLDEN_PICKAXE), repairGold),
				Pair.of(new ItemStack(Items.GOLDEN_AXE), repairGold),
				Pair.of(new ItemStack(Items.GOLDEN_SHOVEL), repairGold),
				Pair.of(new ItemStack(Items.GOLDEN_HOE), repairGold),
				Pair.of(new ItemStack(Items.GOLDEN_HELMET), repairGold),
				Pair.of(new ItemStack(Items.GOLDEN_CHESTPLATE), repairGold),
				Pair.of(new ItemStack(Items.GOLDEN_LEGGINGS), repairGold),
				Pair.of(new ItemStack(Items.GOLDEN_BOOTS), repairGold),
				Pair.of(new ItemStack(Items.DIAMOND_SWORD), repairDiamond),
				Pair.of(new ItemStack(Items.DIAMOND_PICKAXE), repairDiamond),
				Pair.of(new ItemStack(Items.DIAMOND_AXE), repairDiamond),
				Pair.of(new ItemStack(Items.DIAMOND_SHOVEL), repairDiamond),
				Pair.of(new ItemStack(Items.DIAMOND_HOE), repairDiamond),
				Pair.of(new ItemStack(Items.DIAMOND_HELMET), repairDiamond),
				Pair.of(new ItemStack(Items.DIAMOND_CHESTPLATE), repairDiamond),
				Pair.of(new ItemStack(Items.DIAMOND_LEGGINGS), repairDiamond),
				Pair.of(new ItemStack(Items.DIAMOND_BOOTS), repairDiamond),
				Pair.of(new ItemStack(Items.SHIELD), repairWood),
				Pair.of(new ItemStack(Items.ELYTRA), repairLeather)
				);
		for (Pair<ItemStack,ItemStack> entry : items) {
			ItemStack damaged1 = entry.getLeft().copy();
			damaged1.setItemDamage(damaged1.getMaxDamage() * 3 / 4);
			ItemStack damaged2 = entry.getLeft().copy();
			damaged2.setItemDamage(damaged2.getMaxDamage() / 2);
			ItemStack damaged3 = entry.getLeft().copy();
			damaged3.setItemDamage(damaged2.getMaxDamage() / 4);
			registry.addAnvilRecipe(damaged2, entry.getRight(), damaged3);
			registry.addAnvilRecipe(damaged1, damaged2, damaged3);
		}
	}
}
