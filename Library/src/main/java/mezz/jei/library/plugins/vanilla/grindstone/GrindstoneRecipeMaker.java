package mezz.jei.library.plugins.vanilla.grindstone;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
		return getGrindstoneRecipes(
			ingredientManager,
			platformHelper,
			Services.PLATFORM.getIngredientHelper(),
			grindstoneMenu
		);
	}

	public static List<IJeiGrindstoneRecipe> getGrindstoneRecipes(
		IIngredientManager ingredientManager,
		IPlatformRecipeHelper platformHelper,
		IPlatformIngredientHelper ingredientHelper,
		GrindstoneMenu grindstoneMenu
	) {
		return Stream.concat(
						getRepairRecipes(platformHelper, ingredientManager, grindstoneMenu),
						getDisenchantRecipes(platformHelper, ingredientHelper, grindstoneMenu)
				)
				.toList();
	}

	private static Stream<IJeiGrindstoneRecipe> getDisenchantRecipes(
		IPlatformRecipeHelper platformHelper,
		IPlatformIngredientHelper ingredientHelper,
		GrindstoneMenu grindstoneMenu
	) {
		Registry<Enchantment> registry = RegistryUtil.getRegistry(Registries.ENCHANTMENT);
		List<Holder.Reference<Enchantment>> enchantments = registry.holders().toList();
		List<IJeiGrindstoneRecipe> grindstoneRecipes = new ArrayList<>();
		for (Holder.Reference<Enchantment> enchantmentHolder : enchantments) {
			if (enchantmentHolder.is(EnchantmentTags.CURSE)) {
				continue;
			}
			Enchantment enchantment = enchantmentHolder.value();
			Optional<ResourceKey<Enchantment>> enchantmentResourceKey = registry.getResourceKey(enchantment);
			ResourceLocation enchantmentId = enchantmentResourceKey.map(ResourceKey::location).orElse(null);
			String enchantmentPath = enchantmentId == null ? null : enchantmentId.getPath();
			for (Holder<Item> itemHolder : ingredientHelper.getSupportedItems(enchantmentHolder)) {
				ItemStack stack = new ItemStack(itemHolder);
				if (!stack.isEnchantable() ||
					!canEnchant(platformHelper, stack, enchantmentHolder, enchantmentId)
				) {
					continue;
				}
				for (int level = 1; level <= Math.min(enchantment.getMaxLevel(), 10); level++) {
					ItemStack enchantedStack = stack.copy();
					enchantedStack.enchant(enchantmentHolder, level);
					String itemId = stack.getItem().getDescriptionId();
					String asciiLevel = Integer.toString(level);
					String rawPath = "grindstone.disenchantment.%s.%s.%s".formatted(itemId, enchantmentPath, asciiLevel);
					String uidPath = ResourceLocationUtil.sanitizePath(rawPath);
					ResourceLocation uid = ResourceLocation.withDefaultNamespace(uidPath);
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
		Holder<Enchantment> enchantment,
		ResourceLocation enchantmentId
	) {
		try {
			return platformHelper.isItemEnchantable(stack, enchantment);
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
					return getGrindstoneRecipe(platformHelper, grindstoneMenu, topInput, bottomInput, ResourceLocation.withDefaultNamespace(uidPath));
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

	public static void clearCache() {
		GrindstoneHelper.clearCache();
	}
}
