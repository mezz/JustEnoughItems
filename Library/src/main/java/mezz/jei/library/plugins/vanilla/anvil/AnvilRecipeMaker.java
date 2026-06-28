package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class AnvilRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.ENCHANTED_BOOK);

	private final IVanillaRecipeFactory vanillaRecipeFactory;
	private final IIngredientManager ingredientManager;
	private final IIngredientHelper<ItemStack> ingredientHelper;
	private final IPlatformItemStackHelper itemStackHelper;
	private final AnvilMenu anvilMenu;

	public static List<IJeiAnvilRecipe> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
		AnvilMenu fakeAnvilMenu = AnvilHelper.getFakeAnvilMenu();
		return getAnvilRecipes(vanillaRecipeFactory, ingredientManager, fakeAnvilMenu);
	}

	public static List<IJeiAnvilRecipe> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager, AnvilMenu anvilMenu) {
		AnvilRecipeMaker anvilRecipeMaker = new AnvilRecipeMaker(vanillaRecipeFactory, ingredientManager, anvilMenu);
		return anvilRecipeMaker.getAnvilRecipes();
	}

	private AnvilRecipeMaker(
		IVanillaRecipeFactory vanillaRecipeFactory,
		IIngredientManager ingredientManager,
		AnvilMenu anvilMenu
	) {
		this.vanillaRecipeFactory = vanillaRecipeFactory;
		this.ingredientManager = ingredientManager;
		this.ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		this.itemStackHelper = Services.PLATFORM.getItemStackHelper();
		this.anvilMenu = anvilMenu;
	}

	public List<IJeiAnvilRecipe> getAnvilRecipes() {
		return Stream.concat(
				getRepairRecipes(),
				getBookEnchantmentRecipes()
			)
			.toList();
	}

	private static final class EnchantmentData {
		private final Holder<Enchantment> enchantment;
		private final List<ItemStack> enchantedBooks;

		private EnchantmentData(Holder<Enchantment> enchantment) {
			this.enchantment = enchantment;
			this.enchantedBooks = getEnchantedBooks(enchantment);
		}

		public List<ItemStack> getEnchantedBooks(ItemStack ingredient) {
			IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
			var list = enchantedBooks.stream()
				.filter(enchantedBook -> itemStackHelper.isBookEnchantable(ingredient, enchantedBook))
				.toList();
			// avoid using copy of list if it contains the exact same items
			return list.size() == enchantedBooks.size() ? enchantedBooks : list;
		}

		private boolean canEnchant(IPlatformItemStackHelper itemStackHelper, ItemStack ingredient) {
			try {
				return itemStackHelper.canEnchant(enchantment, ingredient);
			} catch (RuntimeException e) {
				String stackInfo = ErrorUtil.getItemStackInfo(ingredient);
				LOGGER.error("Failed to check if ingredient can be enchanted: {}", stackInfo, e);
				return false;
			}
		}

		private static List<ItemStack> getEnchantedBooks(Holder<Enchantment> enchantment) {
			return IntStream.rangeClosed(1, enchantment.value().getMaxLevel())
				.mapToObj(level -> {
					ItemStack bookEnchant = ENCHANTED_BOOK.copy();
					ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(bookEnchant));
					itemEnchantments.set(enchantment, level);
					EnchantmentHelper.setEnchantments(bookEnchant, itemEnchantments.toImmutable());
					return bookEnchant;
				})
				.toList();
		}
	}

	private Stream<IJeiAnvilRecipe> getBookEnchantmentRecipes() {
		Registry<Enchantment> registry = RegistryUtil.getRegistry(Registries.ENCHANTMENT);
		List<EnchantmentData> enchantmentDatas = registry.holders()
			.map(EnchantmentData::new)
			.toList();

		return ingredientManager.getAllItemStacks()
			.stream()
			.filter(ItemStack::isEnchantable)
			.flatMap(ingredient -> getBookEnchantmentRecipes(enchantmentDatas, ingredient));
	}

	private Stream<IJeiAnvilRecipe> getBookEnchantmentRecipes(
		List<EnchantmentData> enchantmentDatas,
		ItemStack ingredient
	) {
		var ingredientSingletonList = List.of(ingredient);
		return enchantmentDatas.stream()
			.filter(data -> data.canEnchant(itemStackHelper, ingredient))
			.map(data -> data.getEnchantedBooks(ingredient))
			.filter(enchantedBooks -> !enchantedBooks.isEmpty())
			.map(enchantedBooks -> {
				List<ItemStack> outputs = getEnchantedIngredients(ingredient, enchantedBooks);
				// All lists given here are immutable, and we want to keep the transforming list from outputs,
				// so we call the AnvilRecipe constructor directly
				return new AnvilRecipe(ingredientSingletonList, enchantedBooks, outputs, null);
			});
	}

	private List<ItemStack> getEnchantedIngredients(ItemStack ingredient, List<ItemStack> enchantedBooks) {
		return enchantedBooks.stream()
			.map(enchantedBook -> getAnvilOutput(ingredient, enchantedBook))
			.filter(i -> !i.isEmpty())
			.toList();
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
			new RepairData(ArmorMaterials.LEATHER.value().repairIngredient().get(),
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
			new RepairData(ArmorMaterials.IRON.value().repairIngredient().get(),
				new ItemStack(Items.IRON_HELMET),
				new ItemStack(Items.IRON_CHESTPLATE),
				new ItemStack(Items.IRON_LEGGINGS),
				new ItemStack(Items.IRON_BOOTS)
			),
			new RepairData(ArmorMaterials.CHAIN.value().repairIngredient().get(),
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
			new RepairData(ArmorMaterials.GOLD.value().repairIngredient().get(),
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
			new RepairData(ArmorMaterials.DIAMOND.value().repairIngredient().get(),
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
			new RepairData(ArmorMaterials.NETHERITE.value().repairIngredient().get(),
				new ItemStack(Items.NETHERITE_BOOTS),
				new ItemStack(Items.NETHERITE_HELMET),
				new ItemStack(Items.NETHERITE_LEGGINGS),
				new ItemStack(Items.NETHERITE_CHESTPLATE)
			),
			new RepairData(Ingredient.of(Items.PHANTOM_MEMBRANE),
				new ItemStack(Items.ELYTRA)
			),
			new RepairData(ArmorMaterials.TURTLE.value().repairIngredient().get(),
				new ItemStack(Items.TURTLE_HELMET)
			)
		);
	}

	private Stream<IJeiAnvilRecipe> getRepairRecipes() {
		return getRepairData()
			.flatMap(this::getRepairRecipes);
	}

	private Stream<IJeiAnvilRecipe> getRepairRecipes(RepairData repairData) {
		Ingredient repairIngredient = repairData.getRepairIngredient();
		List<ItemStack> repairables = repairData.getRepairables();

		List<ItemStack> repairMaterials = List.of(repairIngredient.getItems());

		return repairables.stream()
			.mapMulti((itemStack, consumer) -> {
				String uid = ingredientHelper.getResourceLocation(itemStack).toString();
				String ingredientIdPath = ResourceLocationUtil.sanitizePath(uid);
				String itemModId = ingredientHelper.getResourceLocation(itemStack).getNamespace();

				ItemStack damagedThreeQuarters = itemStack.copy();
				damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
				ItemStack sameItemOutput = getAnvilOutput(damagedThreeQuarters, damagedThreeQuarters);

				var damagedThreeQuartersSingletonList = List.of(damagedThreeQuarters);

				if (!sameItemOutput.isEmpty()) {
					IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(
						damagedThreeQuartersSingletonList,
						damagedThreeQuartersSingletonList,
						List.of(sameItemOutput),
						ResourceLocation.fromNamespaceAndPath(itemModId, "anvil.self_repair." + ingredientIdPath)
					);
					consumer.accept(repairWithSame);
				}

				if (!repairMaterials.isEmpty()) {
					ItemStack damagedFully = itemStack.copy();
					damagedFully.setDamageValue(damagedFully.getMaxDamage());
					ItemStack materialOutput = getAnvilOutput(damagedFully, repairMaterials.getFirst());
					if (!materialOutput.isEmpty()) {
						IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(
							List.of(damagedFully),
							repairMaterials,
							List.of(materialOutput),
							ResourceLocation.fromNamespaceAndPath(itemModId, "anvil.materials_repair." + ingredientIdPath)
						);
						consumer.accept(repairWithMaterial);
					}
				}
			});
	}

	public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
		return AnvilHelper.findLevelsCost(leftStack, rightStack);
	}

	private ItemStack getAnvilOutput(ItemStack leftStack, ItemStack rightStack) {
		AnvilMenu result = AnvilHelper.setAnvilMenu(anvilMenu, leftStack, rightStack);
		if (result == null) {
			return ItemStack.EMPTY;
		}
		Slot resultSlot = result.getSlot(AnvilMenu.RESULT_SLOT);
		return resultSlot.getItem().copy();
	}
}
