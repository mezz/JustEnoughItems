package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
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
		private final Enchantment enchantment;
		private final List<ItemStack> enchantedBooks;

		private EnchantmentData(Enchantment enchantment) {
			this.enchantment = enchantment;
			this.enchantedBooks = getEnchantedBooks(enchantment);
		}

		public List<ItemStack> getEnchantedBooks(IPlatformItemStackHelper itemStackHelper, ItemStack ingredient) {
			var list = enchantedBooks.stream()
				.filter(enchantedBook -> itemStackHelper.isBookEnchantable(ingredient, enchantedBook))
				.toList();
			// avoid using copy of list if it contains the exact same items
			return list.size() == enchantedBooks.size() ? enchantedBooks : list;
		}

		private boolean canEnchant(ItemStack ingredient) {
			try {
				return enchantment.canEnchant(ingredient);
			} catch (RuntimeException e) {
				String stackInfo = ErrorUtil.getItemStackInfo(ingredient);
				LOGGER.error("Failed to check if ingredient can be enchanted: {}", stackInfo, e);
				return false;
			}
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

	private Stream<IJeiAnvilRecipe> getBookEnchantmentRecipes() {
		IPlatformRegistry<Enchantment> registry = Services.PLATFORM.getRegistry(Registry.ENCHANTMENT_REGISTRY);
		List<EnchantmentData> enchantmentDatas = registry.getValues()
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
			.filter(data -> data.canEnchant(ingredient))
			.map(data -> data.getEnchantedBooks(itemStackHelper, ingredient))
			.filter(enchantedBooks -> !enchantedBooks.isEmpty())
			.flatMap(enchantedBooks -> {
				List<ItemStack> outputs = getEnchantedIngredients(ingredient, enchantedBooks);
				if (outputs.isEmpty()) {
					return Stream.empty();
				}
				String ingredientId = ingredientHelper.getUniqueId(ingredient, UidContext.Recipe);
				String ingredientIdPath = ResourceLocationUtil.sanitizePath(ingredientId);
				String id = "enchantment." + ingredientIdPath;

				ResourceLocation uid = new ResourceLocation(ModIds.MINECRAFT_ID, id);
				// All lists given here are immutable, so call the AnvilRecipe constructor directly
				// to avoid copying them again.
				IJeiAnvilRecipe recipe = new AnvilRecipe(ingredientSingletonList, enchantedBooks, outputs, uid);
				return Stream.of(recipe);
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
				String ingredientIdPath = ResourceLocationUtil.sanitizePath(ingredientHelper.getUniqueId(itemStack, UidContext.Recipe));
				String itemModId = ingredientHelper.getResourceLocation(itemStack).getNamespace();

				ItemStack damagedThreeQuarters = itemStack.copy();
				damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
				ItemStack sameItemOutput = getAnvilOutput(damagedThreeQuarters, damagedThreeQuarters);

				List<ItemStack> damagedThreeQuartersSingletonList = List.of(damagedThreeQuarters);

				if (!sameItemOutput.isEmpty()) {
					IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(
						damagedThreeQuartersSingletonList,
						damagedThreeQuartersSingletonList,
						List.of(sameItemOutput),
						new ResourceLocation(itemModId, "self_repair." + ingredientIdPath)
					);
					consumer.accept(repairWithSame);
				}

				if (!repairMaterials.isEmpty()) {
					ItemStack damagedFully = itemStack.copy();
					damagedFully.setDamageValue(damagedFully.getMaxDamage());
					ItemStack materialOutput = getAnvilOutput(damagedFully, repairMaterials.get(0));
					if (!materialOutput.isEmpty()) {
						IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(
							List.of(damagedFully),
							repairMaterials,
							List.of(materialOutput),
							new ResourceLocation(itemModId, "materials_repair." + ingredientIdPath)
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
		return result.slots.get(2).getItem().copy();
	}
}
