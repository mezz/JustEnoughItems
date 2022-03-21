package mezz.jei.plugins.vanilla.anvil;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class AnvilRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.ENCHANTED_BOOK);

	private AnvilRecipeMaker() {
	}

	public static List<IJeiAnvilRecipe> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
		return Stream.concat(
				getRepairRecipes(vanillaRecipeFactory),
				getBookEnchantmentRecipes(vanillaRecipeFactory, ingredientManager)
			)
			.toList();
	}

	private static final class EnchantmentData {
		private final Enchantment enchantment;
		private final List<ItemStack> enchantedBooks;

		private EnchantmentData(Enchantment enchantment) {
			this.enchantment = enchantment;
			this.enchantedBooks = getEnchantedBooks(enchantment);
		}

		public List<ItemStack> getEnchantedBooks(ItemStack ingredient) {
			Item item = ingredient.getItem();
			return enchantedBooks.stream()
				.filter(enchantedBook -> item.isBookEnchantable(ingredient, enchantedBook))
				.toList();
		}

		public Enchantment getEnchantment() {
			return enchantment;
		}

		private static List<ItemStack> getEnchantedBooks(Enchantment enchantment) {
			return IntStream.rangeClosed(1, enchantment.getMaxLevel())
				.mapToObj(level -> {
					ItemStack bookEnchant = ENCHANTED_BOOK.copy();
					EnchantmentHelper.setEnchantments(Map.of(enchantment, level), bookEnchant);
					return bookEnchant;
				})
				.toList();
		}
	}

	private static Stream<IJeiAnvilRecipe> getBookEnchantmentRecipes(
		IVanillaRecipeFactory vanillaRecipeFactory,
		IIngredientManager ingredientManager
	) {
		List<EnchantmentData> enchantmentDatas = ForgeRegistries.ENCHANTMENTS
			.getValues()
			.stream()
			.map(EnchantmentData::new)
			.toList();

		return ingredientManager.getAllIngredients(VanillaTypes.ITEM)
			.stream()
			.filter(ItemStack::isEnchantable)
			.flatMap(ingredient -> getBookEnchantmentRecipes(vanillaRecipeFactory, enchantmentDatas, ingredient));
	}

	private static Stream<IJeiAnvilRecipe> getBookEnchantmentRecipes(
		IVanillaRecipeFactory vanillaRecipeFactory,
		List<EnchantmentData> enchantmentDatas,
		ItemStack ingredient
	) {
		return enchantmentDatas.stream()
			.filter(data -> data.getEnchantment().canEnchant(ingredient))
			.map(data -> data.getEnchantedBooks(ingredient))
			.filter(enchantedBooks -> !enchantedBooks.isEmpty())
			.map(enchantedBooks -> {
				List<ItemStack> outputs = getEnchantedIngredients(ingredient, enchantedBooks);
				return vanillaRecipeFactory.createAnvilRecipe(ingredient, enchantedBooks, outputs);
			});
	}

	private static List<ItemStack> getEnchantedIngredients(ItemStack ingredient, List<ItemStack> enchantedBooks) {
		return enchantedBooks.stream()
			.map(enchantedBook -> getEnchantedIngredient(ingredient, enchantedBook))
			.toList();
	}

	private static ItemStack getEnchantedIngredient(ItemStack ingredient, ItemStack enchantedBook) {
		ItemStack enchantedIngredient = ingredient.copy();
		Map<Enchantment, Integer> bookEnchantments = EnchantmentHelper.getEnchantments(enchantedBook);
		EnchantmentHelper.setEnchantments(bookEnchantments, enchantedIngredient);
		return enchantedIngredient;
	}

	private static class RepairData {
		private final Ingredient repairIngredient;
		private final List<ItemStack> repairables;

		public RepairData(Ingredient repairIngredient, ItemStack... repairables) {
			this.repairIngredient = repairIngredient;
			this.repairables = List.of(repairables);
		}

		public Ingredient getRepairIngredient() {
			return repairIngredient;
		}

		public List<ItemStack> getRepairables() {
			return repairables;
		}
	}

	private static Stream<RepairData> getRepairData() {
		return Stream.of(
			new RepairData(Tiers.WOOD.getRepairIngredient(),
				new ItemStack(Items.WOODEN_SWORD),
				new ItemStack(Items.WOODEN_PICKAXE),
				new ItemStack(Items.WOODEN_AXE),
				new ItemStack(Items.WOODEN_SHOVEL),
				new ItemStack(Items.WOODEN_HOE)
			),
			new RepairData(Ingredient.of(ItemTags.PLANKS),
				new ItemStack(Items.SHIELD)
			),
			new RepairData(Tiers.STONE.getRepairIngredient(),
				new ItemStack(Items.STONE_SWORD),
				new ItemStack(Items.STONE_PICKAXE),
				new ItemStack(Items.STONE_AXE),
				new ItemStack(Items.STONE_SHOVEL),
				new ItemStack(Items.STONE_HOE)
			),
			new RepairData(ArmorMaterials.LEATHER.getRepairIngredient(),
				new ItemStack(Items.LEATHER_HELMET),
				new ItemStack(Items.LEATHER_CHESTPLATE),
				new ItemStack(Items.LEATHER_LEGGINGS),
				new ItemStack(Items.LEATHER_BOOTS)
			),
			new RepairData(Tiers.IRON.getRepairIngredient(),
				new ItemStack(Items.IRON_SWORD),
				new ItemStack(Items.IRON_PICKAXE),
				new ItemStack(Items.IRON_AXE),
				new ItemStack(Items.IRON_SHOVEL),
				new ItemStack(Items.IRON_HOE)
			),
			new RepairData(ArmorMaterials.IRON.getRepairIngredient(),
				new ItemStack(Items.IRON_HELMET),
				new ItemStack(Items.IRON_CHESTPLATE),
				new ItemStack(Items.IRON_LEGGINGS),
				new ItemStack(Items.IRON_BOOTS)
			),
			new RepairData(ArmorMaterials.CHAIN.getRepairIngredient(),
				new ItemStack(Items.CHAINMAIL_HELMET),
				new ItemStack(Items.CHAINMAIL_CHESTPLATE),
				new ItemStack(Items.CHAINMAIL_LEGGINGS),
				new ItemStack(Items.CHAINMAIL_BOOTS)
			),
			new RepairData(Tiers.GOLD.getRepairIngredient(),
				new ItemStack(Items.GOLDEN_SWORD),
				new ItemStack(Items.GOLDEN_PICKAXE),
				new ItemStack(Items.GOLDEN_AXE),
				new ItemStack(Items.GOLDEN_SHOVEL),
				new ItemStack(Items.GOLDEN_HOE)
			),
			new RepairData(ArmorMaterials.GOLD.getRepairIngredient(),
				new ItemStack(Items.GOLDEN_HELMET),
				new ItemStack(Items.GOLDEN_CHESTPLATE),
				new ItemStack(Items.GOLDEN_LEGGINGS),
				new ItemStack(Items.GOLDEN_BOOTS)
			),
			new RepairData(Tiers.DIAMOND.getRepairIngredient(),
				new ItemStack(Items.DIAMOND_SWORD),
				new ItemStack(Items.DIAMOND_PICKAXE),
				new ItemStack(Items.DIAMOND_AXE),
				new ItemStack(Items.DIAMOND_SHOVEL),
				new ItemStack(Items.DIAMOND_HOE)
			),
			new RepairData(ArmorMaterials.DIAMOND.getRepairIngredient(),
				new ItemStack(Items.DIAMOND_HELMET),
				new ItemStack(Items.DIAMOND_CHESTPLATE),
				new ItemStack(Items.DIAMOND_LEGGINGS),
				new ItemStack(Items.DIAMOND_BOOTS)
			),
			new RepairData(Tiers.NETHERITE.getRepairIngredient(),
				new ItemStack(Items.NETHERITE_SWORD),
				new ItemStack(Items.NETHERITE_AXE),
				new ItemStack(Items.NETHERITE_HOE),
				new ItemStack(Items.NETHERITE_SHOVEL),
				new ItemStack(Items.NETHERITE_PICKAXE)
			),
			new RepairData(ArmorMaterials.NETHERITE.getRepairIngredient(),
				new ItemStack(Items.NETHERITE_BOOTS),
				new ItemStack(Items.NETHERITE_HELMET),
				new ItemStack(Items.NETHERITE_LEGGINGS),
				new ItemStack(Items.NETHERITE_CHESTPLATE)
			),
			new RepairData(Ingredient.of(Items.PHANTOM_MEMBRANE),
				new ItemStack(Items.ELYTRA)
			),
			new RepairData(ArmorMaterials.TURTLE.getRepairIngredient(),
				new ItemStack(Items.TURTLE_HELMET)
			)
		);
	}

	private static Stream<IJeiAnvilRecipe> getRepairRecipes(IVanillaRecipeFactory vanillaRecipeFactory) {
		return getRepairData()
			.flatMap(repairData -> getRepairRecipes(repairData, vanillaRecipeFactory));
	}

	private static Stream<IJeiAnvilRecipe> getRepairRecipes(RepairData repairData, IVanillaRecipeFactory vanillaRecipeFactory) {
		Ingredient repairIngredient = repairData.getRepairIngredient();
		List<ItemStack> repairables = repairData.getRepairables();

		List<ItemStack> repairMaterials = List.of(repairIngredient.getItems());

		return repairables.stream()
			.mapMulti((itemStack, consumer) -> {
				ItemStack damagedThreeQuarters = itemStack.copy();
				damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
				ItemStack damagedHalf = itemStack.copy();
				damagedHalf.setDamageValue(damagedHalf.getMaxDamage() / 2);

				IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(List.of(damagedThreeQuarters), List.of(damagedThreeQuarters), List.of(damagedHalf));
				consumer.accept(repairWithSame);

				if (!repairMaterials.isEmpty()) {
					ItemStack damagedFully = itemStack.copy();
					damagedFully.setDamageValue(damagedFully.getMaxDamage());
					IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(List.of(damagedFully), repairMaterials, List.of(damagedThreeQuarters));
					consumer.accept(repairWithMaterial);
				}
			});
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
