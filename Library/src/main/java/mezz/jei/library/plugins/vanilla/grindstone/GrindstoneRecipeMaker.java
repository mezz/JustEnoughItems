package mezz.jei.library.plugins.vanilla.grindstone;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class GrindstoneRecipeMaker {
	private static GrindstoneMenu GRINDSTONE_MENU;

	public static List<IJeiGrindstoneRecipe> getGrindstoneRecipes(IIngredientManager ingredientManager, IPlatformRecipeHelper platformHelper) {
		return Stream.concat(
						getRepairRecipes(platformHelper, ingredientManager),
						getDisenchantRecipes(platformHelper)
				)
				.toList();
	}

	private static Stream<IJeiGrindstoneRecipe> getDisenchantRecipes(IPlatformRecipeHelper platformHelper) {
		IPlatformRegistry<Enchantment> enchantmentRegistry = Services.PLATFORM.getRegistry(Registry.ENCHANTMENT_REGISTRY);
		IPlatformRegistry<Item> itemRegistry = Services.PLATFORM.getRegistry(Registry.ITEM_REGISTRY);
		List<Enchantment> enchantments = enchantmentRegistry.getValues().toList();
		List<Item> items = itemRegistry.getValues().toList();
		List<IJeiGrindstoneRecipe> grindstoneRecipes = new ArrayList<>();
		for (Enchantment enchantment : enchantments) {
			if (enchantment.isCurse()) {
				continue;
			}
			for (Item item : items) {
				ItemStack stack = new ItemStack(item);
				if (!stack.isEnchantable() ||
						!enchantment.canEnchant(stack) ||
						!platformHelper.isItemEnchantable(stack, enchantment)
				) {
					continue;
				}

				ResourceLocation enchantmentResourceLocation = enchantmentRegistry.getRegistryName(enchantment).orElse(null);
				String enchantmentPath = enchantmentResourceLocation == null ? null : enchantmentResourceLocation.getPath();
				for (int level = 1; level <= Math.min(enchantment.getMaxLevel(), 10); level++) {
					ItemStack enchantedStack = stack.copy();
					enchantedStack.enchant(enchantment, level);
					String itemId = stack.getItem().getDescriptionId();
					ResourceLocation uid = enchantmentPath != null ? new ResourceLocation("minecraft", "grindstone.disenchantment.%s.%s.%d".formatted(itemId, enchantmentPath, level)) : null;
					IJeiGrindstoneRecipe grindstoneRecipe = getGrindstoneRecipe(platformHelper, enchantedStack, ItemStack.EMPTY, uid);
					if (grindstoneRecipe != null) {
						grindstoneRecipes.add(grindstoneRecipe);
					}
				}
			}
		}

		return grindstoneRecipes.stream();
	}

	private static Stream<IJeiGrindstoneRecipe> getRepairRecipes(IPlatformRecipeHelper platformHelper, IIngredientManager ingredientManager) {
		return ingredientManager.getAllItemStacks()
				.stream()
				.filter(ItemStack::isDamageableItem)
				.map(stack -> {
					stack.setDamageValue(stack.getMaxDamage() * 3 / 4);
					ItemStack topInput = stack.copy();
					ItemStack bottomInput = stack.copy();
					String itemId = stack.getItem().getDescriptionId();
					return getGrindstoneRecipe(platformHelper, topInput, bottomInput, new ResourceLocation("minecraft", "grindstone.self_repair." + itemId));
				})
				.filter(Objects::nonNull);
	}

	@Nullable
	private static IJeiGrindstoneRecipe getGrindstoneRecipe(IPlatformRecipeHelper platformHelper, ItemStack topInput, ItemStack bottomInput, @Nullable ResourceLocation uid) {
		GrindstoneMenu grindstoneMenu = getFakeGrindstoneMenu();
		if (grindstoneMenu == null) {
			return null;
		}
		ItemStack output = platformHelper.getGrindstoneResult(grindstoneMenu, topInput, bottomInput);
		if (output.isEmpty()) {
			return null;
		}
		return new GrindstoneRecipe(List.of(topInput), List.of(bottomInput), List.of(output), -1, -1, uid);
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
