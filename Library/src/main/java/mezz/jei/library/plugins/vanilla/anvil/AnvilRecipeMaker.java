package mezz.jei.library.plugins.vanilla.anvil;

import java.util.Collections;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.plugins.vanilla.ingredients.subtypes.EnchantedBookSubtypeInterpreter;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
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
import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class AnvilRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final ItemStack ENCHANTED_BOOK = new ItemStack(Items.ENCHANTED_BOOK);
	private static @Nullable AnvilMenu ANVIL_MENU = null;

	private AnvilRecipeMaker() {
	}

	public static List<IJeiAnvilRecipe> getAnvilRecipes(IVanillaRecipeFactory vanillaRecipeFactory, IIngredientManager ingredientManager) {
		IIngredientHelper<ItemStack> ingredientHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		return Stream.concat(
				getRepairRecipes(vanillaRecipeFactory, ingredientHelper),
				getBookEnchantmentRecipes(ingredientManager)
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

	private static Stream<IJeiAnvilRecipe> getBookEnchantmentRecipes(
		IIngredientManager ingredientManager
	) {
		Registry<Enchantment> registry = RegistryUtil.getRegistry(Registries.ENCHANTMENT);
		List<EnchantmentData> enchantmentDatas = registry.listElements()
			.map(EnchantmentData::new)
			.toList();

		return ingredientManager.getAllItemStacks()
			.stream()
			.filter(ItemStack::isEnchantable)
			.flatMap(ingredient -> getBookEnchantmentRecipes(enchantmentDatas, ingredient));
	}

	private static Stream<IJeiAnvilRecipe> getBookEnchantmentRecipes(
		List<EnchantmentData> enchantmentDatas,
		ItemStack ingredient
	) {
		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
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

	private static List<ItemStack> getEnchantedIngredients(ItemStack ingredient, List<ItemStack> enchantedBooks) {
		AnvilMenu anvilMenu = getFakeAnvilMenu();
		if (anvilMenu == null) {
			return List.of();
		}
		return enchantedBooks.stream()
			.map(enchantedBook -> {
				AnvilMenu result = setAnvilMenu(anvilMenu, ingredient, enchantedBook);
				if (result == null) {
					return ItemStack.EMPTY;
				}
				Slot resultSlot = result.slots.get(result.getResultSlot());
				return resultSlot.getItem();
			})
			.filter(i -> !i.isEmpty())
			.toList();
	}

	private static Stream<IJeiAnvilRecipe> getRepairRecipes(
		IVanillaRecipeFactory vanillaRecipeFactory,
		IIngredientHelper<ItemStack> ingredientHelper
	) {
		Minecraft minecraft = Minecraft.getInstance();
		ContextMap contextmap = SlotDisplayContext.fromLevel(Objects.requireNonNull(minecraft.level));
		record RepairData(Holder.Reference<Item> item, List<ItemStack> repairMaterials) {
		}
		return RegistryUtil.getRegistry(Registries.ITEM).listElements()
			.map(item -> {
				Repairable repairable = item.components().get(DataComponents.REPAIRABLE);
				if (repairable == null) {
					return null;
				}
				HolderSet<Item> items = repairable.items();
				if (items.size() == 0) {//Is unlikely to be the case, but better safe than sorry
					return new RepairData(item, Collections.emptyList());
				}
				return new RepairData(item, Ingredient.of(items).display().resolveForStacks(contextmap));
			}).filter(Objects::nonNull)
			.mapMulti((repairData, consumer) -> {
				ItemStack itemStack = new ItemStack(repairData.item());
				String uid = ingredientHelper.getIdentifier(itemStack).toString();
				String ingredientIdPath = ResourceLocationUtil.sanitizePath(uid);
				String itemModId = ingredientHelper.getIdentifier(itemStack).getNamespace();

				ItemStack damagedThreeQuarters = itemStack.copy();
				damagedThreeQuarters.setDamageValue(damagedThreeQuarters.getMaxDamage() * 3 / 4);
				ItemStack damagedHalf = itemStack.copy();
				damagedHalf.setDamageValue(damagedHalf.getMaxDamage() / 2);

				var damagedThreeQuartersSingletonList = List.of(damagedThreeQuarters);

				IJeiAnvilRecipe repairWithSame = vanillaRecipeFactory.createAnvilRecipe(
					damagedThreeQuartersSingletonList,
					damagedThreeQuartersSingletonList,
					List.of(damagedHalf),
					Identifier.fromNamespaceAndPath(itemModId, "anvil.self_repair." + ingredientIdPath)
				);
				consumer.accept(repairWithSame);

				List<ItemStack> repairMaterials = repairData.repairMaterials();
				if (!repairMaterials.isEmpty()) {
					ItemStack damagedFully = itemStack.copy();
					damagedFully.setDamageValue(damagedFully.getMaxDamage());
					IJeiAnvilRecipe repairWithMaterial = vanillaRecipeFactory.createAnvilRecipe(
						List.of(damagedFully),
						repairMaterials,
						damagedThreeQuartersSingletonList,
						Identifier.fromNamespaceAndPath(itemModId, "anvil.materials_repair." + ingredientIdPath)
					);
					consumer.accept(repairWithMaterial);
				}
			});
	}

	public static int findLevelsCost(ItemStack leftStack, ItemStack rightStack) {
		AnvilMenu anvilMenu = getFakeAnvilMenu();
		if (anvilMenu == null) {
			return -1;
		}
		AnvilMenu result = setAnvilMenu(anvilMenu, leftStack, rightStack);
		if (result == null) {
			return -1;
		}
		return result.getCost();
	}

	@Nullable
	private static AnvilMenu getFakeAnvilMenu() {
		if (ANVIL_MENU == null) {
			Player player = Minecraft.getInstance().player;
			if (player == null) {
				return null;
			}
			Inventory fakeInventory = new Inventory(player, new EntityEquipment());
			ANVIL_MENU = new AnvilMenu(0, fakeInventory);
			return ANVIL_MENU;
		}
		return ANVIL_MENU;
	}

	@Nullable
	private static AnvilMenu setAnvilMenu(AnvilMenu anvilMenu, ItemStack leftStack, ItemStack rightStack) {
		try {
			Slot leftSlot = anvilMenu.slots.get(0);
			Slot rightSlot = anvilMenu.slots.get(1);

			// setting the stack triggers a recalculation of the recipe, so avoid it when possible
			if (leftSlot.getItem() != leftStack) {
				leftSlot.set(leftStack);
			}
			if (rightSlot.getItem() != rightStack) {
				rightSlot.set(rightStack);
			}
			return anvilMenu;
		} catch (RuntimeException e) {
			String left = ErrorUtil.getItemStackInfo(leftStack);
			String right = ErrorUtil.getItemStackInfo(rightStack);
			LOGGER.error("Could not set anvil recipe for: ({} and {}).", left, right, e);
			return null;
		}
	}
}
