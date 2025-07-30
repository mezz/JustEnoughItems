package mezz.jei.library.plugins.vanilla.grindstone;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class GrindstoneRecipeMaker {
	private static GrindstoneMenu GRINDSTONE_MENU;
	private static final int TOP_SLOT = 0;
	private static final int BOTTOM_SLOT = 1;
	private static final int OUTPUT_SLOT = 2;
	public static List<IJeiGrindstoneRecipe> getGrindstoneRecipes(IIngredientManager ingredientManager, IPlatformRecipeHelper platformHelper) {
		return Stream.concat(
						getRepairRecipes(ingredientManager),
						getDisenchantRecipes(ingredientManager, platformHelper)
				)
				.toList();
	}
	private static Stream<IJeiGrindstoneRecipe> getDisenchantRecipes(
			IIngredientManager ingredientManager,
			IPlatformRecipeHelper platformHelper
	) {
		Registry<Enchantment> registry = RegistryUtil.getRegistry(Registries.ENCHANTMENT);
		List<Enchantment> enchantments = registry.stream().toList();
		List<IJeiGrindstoneRecipe> grindstoneRecipes = new ArrayList<>();
		for (ItemStack stack : ingredientManager.getAllItemStacks()) {
			if (!stack.isEnchantable()) {
				continue;
			}
			for (Enchantment enchantment : enchantments) {
				if (enchantment.isCurse()) {
					continue;
				}
				if (!enchantment.canEnchant(stack) ||
						!platformHelper.isItemEnchantable(stack, enchantment)
				) {
					continue;
				}
				ResourceLocation enchantmentResourceLocation = registry.getKey(enchantment);
				String enchantmentPath = enchantmentResourceLocation == null ? null : enchantmentResourceLocation.getPath();
				for (int level = 1; level <= Math.min(enchantment.getMaxLevel(), 10); level++) {
					ItemStack enchantedStack = stack.copy();
					enchantedStack.enchant(enchantment, level);
					String itemId = stack.getItem().getDescriptionId();
					ResourceLocation uid = enchantmentPath != null ? new ResourceLocation("minecraft", "grindstone.disenchantment.%s.%s.%d".formatted(itemId, enchantmentPath, level)) : null;
					IJeiGrindstoneRecipe grindstoneRecipe = getGrindstoneRecipe(enchantedStack, ItemStack.EMPTY, uid);
					if (grindstoneRecipe != null) {
						grindstoneRecipes.add(grindstoneRecipe);
					}
				}
			}
		}

		return grindstoneRecipes.stream();
	}


	private static Stream<IJeiGrindstoneRecipe> getRepairRecipes(IIngredientManager ingredientManager) {
		return ingredientManager.getAllItemStacks()
				.stream()
				.filter(ItemStack::isDamageableItem)
				.map(stack -> {
					stack.setDamageValue(stack.getMaxDamage() * 3 / 4);
					ItemStack topInput = stack.copy();
					ItemStack bottomInput = stack.copy();
					String itemId = stack.getItem().getDescriptionId();
					return getGrindstoneRecipe(topInput, bottomInput, new ResourceLocation("minecraft", "grindstone.self_repair." + itemId));
				})
				.filter(Objects::nonNull);
	}

	@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
	@Nullable
	private static IJeiGrindstoneRecipe getGrindstoneRecipe(ItemStack topInput, ItemStack bottomInput, @Nullable ResourceLocation uid) {
		GrindstoneMenu grindstoneMenu = getFakeGrindstoneMenu();
		if (grindstoneMenu == null) {
			return null;
		}
		grindstoneMenu.slots.get(TOP_SLOT).set(topInput);
		grindstoneMenu.slots.get(BOTTOM_SLOT).set(bottomInput);
		ItemStack output = grindstoneMenu.slots.get(OUTPUT_SLOT).getItem();
		if (output.isEmpty()) {
			return null;
		}
		int minXp = getMinXp(grindstoneMenu);
		int maxXp = minXp * 2;
		return new GrindstoneRecipe(List.of(topInput), List.of(bottomInput), List.of(output), minXp, maxXp, uid);
	}

	@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
	private static int getMinXp(GrindstoneMenu grindstoneMenu) {
		ItemStack topItem = grindstoneMenu.slots.get(TOP_SLOT).getItem();
		ItemStack bottomItem = grindstoneMenu.slots.get(BOTTOM_SLOT).getItem();
		int topXp = getExperienceFromItem(topItem);
		int bottomXp = getExperienceFromItem(bottomItem);
		return MathUtil.divideCeil(topXp + bottomXp, 2);
	}

	private static int getExperienceFromItem(ItemStack stack) {
		int i = 0;
		Map<Enchantment, Integer> itemEnchantments = EnchantmentHelper.getEnchantments(stack);

		for (Map.Entry<Enchantment, Integer> entry : itemEnchantments.entrySet()) {
			Enchantment enchantment = entry.getKey();
			int j = entry.getValue();
			if (!enchantment.isCurse()) {
				i += enchantment.getMinCost(j);
			}
		}

		return i;
	}

	@Nullable
	private static GrindstoneMenu getFakeGrindstoneMenu() {
		if (GRINDSTONE_MENU == null) {
			Player player = Minecraft.getInstance().player;
			if (player == null) {
				return null;
			}
			Inventory fakeInventory = new Inventory(player);
			GRINDSTONE_MENU = new GrindstoneMenu(0, fakeInventory);
			return GRINDSTONE_MENU;
		}
		return GRINDSTONE_MENU;
	}
}
