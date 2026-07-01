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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class AnvilRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.ENCHANTED_BOOK);

	private final IVanillaRecipeFactory vanillaRecipeFactory;
	private final IIngredientManager ingredientManager;
	private final IIngredientHelper<ItemStack> ingredientHelper;
	private final IPlatformItemStackHelper itemStackHelper;
	private final ContextMap contextmap;
	private final AnvilMenu anvilMenu;

	public static List<IJeiAnvilRecipe> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = Objects.requireNonNull(minecraft.level);
		ContextMap clientContextMap = SlotDisplayContext.fromLevel(level);
		AnvilMenu fakeAnvilMenu = AnvilHelper.getFakeAnvilMenu();
		return getAnvilRecipes(vanillaRecipeFactory, ingredientManager, clientContextMap, fakeAnvilMenu);
	}

	public static List<IJeiAnvilRecipe> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager, ContextMap displayContext, AnvilMenu anvilMenu) {
		AnvilRecipeMaker anvilRecipeMaker = new AnvilRecipeMaker(vanillaRecipeFactory, ingredientManager, displayContext, anvilMenu);
		return anvilRecipeMaker.getAnvilRecipes();
	}

	private AnvilRecipeMaker(
		IVanillaRecipeFactory vanillaRecipeFactory,
		IIngredientManager ingredientManager,
		ContextMap contextmap,
		AnvilMenu anvilMenu
	) {
		this.vanillaRecipeFactory = vanillaRecipeFactory;
		this.ingredientManager = ingredientManager;
		this.ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		this.itemStackHelper = Services.PLATFORM.getItemStackHelper();
		this.contextmap = contextmap;
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
		@Unmodifiable
		private final List<ItemStack> enchantedBooks;

		private EnchantmentData(Holder<Enchantment> enchantment) {
			this.enchantment = enchantment;
			this.enchantedBooks = getEnchantedBooks(enchantment);
		}

		@Unmodifiable
		public List<ItemStack> getEnchantedBooks() {
			return enchantedBooks;
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
		List<EnchantmentData> enchantmentDatas = registry.listElements()
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
			.mapMulti((data, consumer) -> {
				List<ItemStack> enchantedBooks = data.getEnchantedBooks();
				List<ItemStack> outputs = getEnchantedIngredients(ingredient, enchantedBooks);
				if (outputs.isEmpty()) {
					return;
				}
				// All lists given here are immutable, so we call the AnvilRecipe constructor directly
				AnvilRecipe anvilRecipe = new AnvilRecipe(ingredientSingletonList, enchantedBooks, outputs, null);
				consumer.accept(anvilRecipe);
			});
	}

	private List<ItemStack> getEnchantedIngredients(ItemStack ingredient, List<ItemStack> enchantedBooks) {
		return enchantedBooks.stream()
			.map(enchantedBook -> {
				AnvilMenu result = AnvilHelper.setAnvilMenu(anvilMenu, ingredient, enchantedBook);
				if (result == null) {
					return ItemStack.EMPTY;
				}
				Slot resultSlot = result.slots.get(result.getResultSlot());
				return resultSlot.getItem();
			})
			.filter(i -> !i.isEmpty())
			.toList();
	}

	private static final class RepairData {
		private final Holder.Reference<Item> item;
		private final boolean selfRepair;
		private final HolderSet<Item> repairItems;

		private RepairData(Holder.Reference<Item> item, boolean selfRepair, HolderSet<Item> repairItems) {
			this.item = item;
			this.selfRepair = selfRepair;
			this.repairItems = repairItems;
		}

		public ItemStack getDefaultItemStack() {
			return item.value().getDefaultInstance();
		}

		public boolean isSelfRepair() {
			return selfRepair;
		}

		public List<ItemStack> getRepairMaterials(ContextMap contextmap) {
			if (repairItems.size() == 0) {
				return List.of();
			}
			return Ingredient.of(repairItems)
				.display()
				.resolveForStacks(contextmap);
		}
	}
	private Stream<IJeiAnvilRecipe> getRepairRecipes() {
		return getRepairableItems()
			.mapMulti((repairData, consumer) -> {
				ItemStack itemStack = repairData.getDefaultItemStack();
				String uid = ingredientHelper.getIdentifier(itemStack).toString();
				String ingredientIdPath = ResourceLocationUtil.sanitizePath(uid);
				String itemModId = ingredientHelper.getIdentifier(itemStack).getNamespace();

				ItemStack damaged = itemStack.copy();
				damaged.setDamageValue(damaged.getMaxDamage() * 3 / 4);

				var damagedList = List.of(damaged);

				if (repairData.isSelfRepair()) {
					ItemStack sameItemRepairOutput = getSameItemRepairOutput(damaged, damaged);
					setAnvilResultRepairCost(sameItemRepairOutput);
					IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(
						damagedList,
						damagedList,
						List.of(sameItemRepairOutput),
						Identifier.fromNamespaceAndPath(itemModId, "anvil.self_repair." + ingredientIdPath)
					);
					consumer.accept(repairWithSame);
				}

				List<ItemStack> repairMaterials = repairData.getRepairMaterials(contextmap);
				if (!repairMaterials.isEmpty()) {
					ItemStack damagedFully = itemStack.copy();
					damagedFully.setDamageValue(damagedFully.getMaxDamage());
					ItemStack output = getMaterialRepairOutput(damagedFully);
					setAnvilResultRepairCost(output);
					IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(
						List.of(damagedFully),
						repairMaterials,
						List.of(output),
						Identifier.fromNamespaceAndPath(itemModId, "anvil.materials_repair." + ingredientIdPath)
					);
					consumer.accept(repairWithMaterial);
				}
			});
	}

	private static Stream<RepairData> getRepairableItems() {
		return RegistryUtil.getRegistry(Registries.ITEM)
			.listElements()
			.mapMulti((item, consumer) -> {
				ItemStack itemStack = item.value().getDefaultInstance();
				boolean selfRepair = itemStack.isDamageableItem() && EnchantmentHelper.canStoreEnchantments(itemStack);
				Repairable repairable = item.components().get(DataComponents.REPAIRABLE);
				HolderSet<Item> repairItems = repairable == null ? HolderSet.empty() : repairable.items();
				if (selfRepair || repairItems.size() > 0) {
					RepairData repairData = new RepairData(item, selfRepair, repairItems);
					consumer.accept(repairData);
				}
			});
	}

	private static ItemStack getSameItemRepairOutput(ItemStack input, ItemStack addition) {
		ItemStack result = input.copy();
		int remaining1 = input.getMaxDamage() - input.getDamageValue();
		int remaining2 = addition.getMaxDamage() - addition.getDamageValue();
		int additional = remaining2 + result.getMaxDamage() * 12 / 100;
		int remaining = remaining1 + additional;
		int resultDamage = Math.max(0, result.getMaxDamage() - remaining);
		result.setDamageValue(resultDamage);
		return result;
	}
	private static ItemStack getMaterialRepairOutput(ItemStack input) {
		ItemStack result = input.copy();
		int repairAmount = Math.min(result.getDamageValue(), result.getMaxDamage() / 4);
		result.setDamageValue(result.getDamageValue() - repairAmount);
		return result;
	}

	private static void setAnvilResultRepairCost(ItemStack stack) {
		int repairCost = stack.getOrDefault(DataComponents.REPAIR_COST, 0);
		stack.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(repairCost));
	}

}
