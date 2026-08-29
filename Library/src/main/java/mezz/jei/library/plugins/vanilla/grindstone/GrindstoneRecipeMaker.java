package mezz.jei.library.plugins.vanilla.grindstone;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class GrindstoneRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();

	public static List<IJeiGrindstoneRecipe> getGrindstoneRecipes(IIngredientManager ingredientManager, IPlatformRecipeHelper platformHelper) {
		GrindstoneMenu grindstoneMenu = GrindstoneHelper.getFakeGrindstoneMenu();
		if (grindstoneMenu == null) {
			return List.of();
		}
		return getGrindstoneRecipes(ingredientManager, platformHelper, grindstoneMenu);
	}

	public static List<IJeiGrindstoneRecipe> getGrindstoneRecipes(
		IIngredientManager ingredientManager,
		IPlatformRecipeHelper platformHelper,
		GrindstoneMenu grindstoneMenu
	) {
		return Stream.concat(
						getRepairRecipes(platformHelper, ingredientManager, grindstoneMenu),
						getDisenchantRecipes(platformHelper, grindstoneMenu)
				)
				.toList();
	}

	private static Stream<IJeiGrindstoneRecipe> getDisenchantRecipes(
		IPlatformRecipeHelper platformHelper,
		GrindstoneMenu grindstoneMenu
	) {
		Registry<Enchantment> enchantmentRegistry = RegistryUtil.getRegistry(Registries.ENCHANTMENT);
		Registry<Item> itemRegistry = RegistryUtil.getRegistry(Registries.ITEM);
		List<Enchantment> enchantments = enchantmentRegistry.stream().toList();
		List<Item> items = itemRegistry.stream().toList();
		List<IJeiGrindstoneRecipe> grindstoneRecipes = new ArrayList<>();
		for (Enchantment enchantment : enchantments) {
			if (enchantment.isCurse()) {
				continue;
			}
			ResourceLocation enchantmentResourceLocation = enchantmentRegistry.getKey(enchantment);
			String enchantmentPath = enchantmentResourceLocation == null ? null : enchantmentResourceLocation.getPath();
			for (Item item : items) {
				ItemStack stack = new ItemStack(item);
				if (!stack.isEnchantable() ||
						!canEnchant(platformHelper, stack, enchantment, enchantmentResourceLocation)
				) {
					continue;
				}

				for (int level = 1; level <= Math.min(enchantment.getMaxLevel(), 10); level++) {
					ItemStack enchantedStack = stack.copy();
					enchantedStack.enchant(enchantment, level);
					String itemId = stack.getItem().getDescriptionId();
					String asciiLevel = Integer.toString(level);
					String rawPath = "grindstone.disenchantment.%s.%s.%s".formatted(itemId, enchantmentPath, asciiLevel);
					String uidPath = ResourceLocationUtil.sanitizePath(rawPath);
					ResourceLocation uid = new ResourceLocation("minecraft", uidPath);
					IJeiGrindstoneRecipe grindstoneRecipe = getGrindstoneRecipe(platformHelper, grindstoneMenu, enchantedStack, ItemStack.EMPTY, uid);
					if (grindstoneRecipe != null) {
						grindstoneRecipes.add(grindstoneRecipe);
					}
				}
			}
		}

		return grindstoneRecipes.stream();
	}

	private static boolean canEnchant(
		IPlatformRecipeHelper platformHelper,
		ItemStack stack,
		Enchantment enchantment,
		@Nullable ResourceLocation enchantmentId
	) {
		try {
			return enchantment.canEnchant(stack) &&
				platformHelper.isItemEnchantable(stack, enchantment);
		} catch (RuntimeException e) {
			String stackInfo = ErrorUtil.getItemStackInfo(stack);
			LOGGER.error("Failed to check if enchantment {} can be applied to item: {}", enchantmentId, stackInfo, e);
			return false;
		}
	}

	private static Stream<IJeiGrindstoneRecipe> getRepairRecipes(IPlatformRecipeHelper platformHelper, IIngredientManager ingredientManager, GrindstoneMenu grindstoneMenu) {
		return ingredientManager.getAllItemStacks()
				.stream()
				.filter(ItemStack::isDamageableItem)
				.map(stack -> {
					stack.setDamageValue(stack.getMaxDamage() * 3 / 4);
					ItemStack topInput = stack.copy();
					ItemStack bottomInput = stack.copy();
					String itemId = stack.getItem().getDescriptionId();
					String rawPath = "grindstone.self_repair." + itemId;
					String uidPath = ResourceLocationUtil.sanitizePath(rawPath);
					return getGrindstoneRecipe(platformHelper, grindstoneMenu, topInput, bottomInput, new ResourceLocation("minecraft", uidPath));
				})
				.filter(Objects::nonNull);
	}

	@Nullable
	private static IJeiGrindstoneRecipe getGrindstoneRecipe(IPlatformRecipeHelper platformHelper, GrindstoneMenu grindstoneMenu, ItemStack topInput, ItemStack bottomInput, @Nullable ResourceLocation uid) {
		ItemStack output = platformHelper.getGrindstoneResult(grindstoneMenu, topInput, bottomInput);
		if (output.isEmpty()) {
			return null;
		}
		return new GrindstoneRecipe(List.of(topInput), List.of(bottomInput), List.of(output), -1, -1, uid);
	}
}
