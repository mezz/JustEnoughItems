package mezz.jei.library.plugins.vanilla.grindstone;

import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.util.ResourceLocationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class GrindstoneRecipeMaker {
	private static @Nullable GrindstoneMenu GRINDSTONE_MENU;

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
				String enchantmentPath = enchantmentResourceLocation.map(enchantmentResourceKey -> enchantmentResourceKey.identifier().getPath()).orElse(null);
				for (int level = 1; level <= Math.min(enchantment.getMaxLevel(), 10); level++) {
					ItemStack enchantedStack = stack.copy();
					enchantedStack.enchant(enchantmentHolder, level);
					String itemId = stack.getItem().getDescriptionId();
					String asciiLevel = Integer.toString(level);
					String rawPath = "grindstone.disenchantment.%s.%s.%s".formatted(itemId, enchantmentPath, asciiLevel);
					String uidPath = ResourceLocationUtil.sanitizePath(rawPath);
					Identifier uid = Identifier.withDefaultNamespace(uidPath);
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
				.mapMulti((stack, consumer) -> {
					stack.setDamageValue(stack.getMaxDamage() * 3 / 4);
					ItemStack topInput = stack.copy();
					ItemStack bottomInput = stack.copy();
					String itemId = stack.getItem().getDescriptionId();
					String rawPath = "grindstone.self_repair." + itemId;
					String uidPath = ResourceLocationUtil.sanitizePath(rawPath);
					IJeiGrindstoneRecipe recipe = getGrindstoneRecipe(platformHelper, topInput, bottomInput, Identifier.withDefaultNamespace(uidPath));
					if (recipe != null) {
						consumer.accept(recipe);
					}
				});
	}

	@Nullable
	private static IJeiGrindstoneRecipe getGrindstoneRecipe(IPlatformRecipeHelper platformHelper, ItemStack topInput, ItemStack bottomInput, @Nullable Identifier uid) {
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
