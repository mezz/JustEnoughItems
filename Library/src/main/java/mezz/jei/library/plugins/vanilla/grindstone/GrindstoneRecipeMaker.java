package mezz.jei.library.plugins.vanilla.grindstone;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityEquipment;
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
import java.util.Optional;
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
		Registry<Enchantment> registry = RegistryUtil.getRegistry(Registries.ENCHANTMENT);
		List<Holder.Reference<Enchantment>> enchantments = registry.listElements().toList();
		List<IJeiGrindstoneRecipe> grindstoneRecipes = new ArrayList<>();
		for (Holder.Reference<Enchantment> enchantmentHolder : enchantments) {
			if (enchantmentHolder.is(EnchantmentTags.CURSE)) {
				continue;
			}
			Enchantment enchantment = enchantmentHolder.value();
			for (Holder<Item> itemHolder : enchantment.getSupportedItems()) {
				ItemStack stack = itemHolder.value().getDefaultInstance();
				if (!stack.isEnchantable() ||
					!platformHelper.isItemEnchantable(stack, enchantmentHolder)
				) {
					continue;
				}
				Optional<ResourceKey<Enchantment>> enchantmentResourceLocation = registry.getResourceKey(enchantment);
				String enchantmentPath = enchantmentResourceLocation.map(enchantmentResourceKey -> enchantmentResourceKey.location().getPath()).orElse(null);
				for (int level = 1; level <= Math.min(enchantment.getMaxLevel(), 10); level++) {
					ItemStack enchantedStack = stack.copy();
					enchantedStack.enchant(enchantmentHolder, level);
					String itemId = stack.getItem().getDescriptionId();
					ResourceLocation uid = enchantmentPath != null ? ResourceLocation.withDefaultNamespace("grindstone.disenchantment.%s.%s.%d".formatted(itemId, enchantmentPath, level)) : null;
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
					return getGrindstoneRecipe(platformHelper, topInput, bottomInput, ResourceLocation.withDefaultNamespace("grindstone.self_repair." + itemId));
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
			Inventory fakeInventory = new Inventory(player, new EntityEquipment());
			GRINDSTONE_MENU = new GrindstoneMenu(0, fakeInventory);
			return GRINDSTONE_MENU;
		}
		return GRINDSTONE_MENU;
	}
}
