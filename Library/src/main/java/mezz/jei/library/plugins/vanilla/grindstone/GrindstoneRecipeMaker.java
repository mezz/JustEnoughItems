package mezz.jei.library.plugins.vanilla.grindstone;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mezz.jei.api.recipe.vanilla.IJeiGrindstoneRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import mezz.jei.common.util.MathUtil;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
		List<Holder.Reference<Enchantment>> enchantments = registry.listElements().toList();
		List<IJeiGrindstoneRecipe> grindstoneRecipes = new ArrayList<>();
		for (ItemStack stack : ingredientManager.getAllItemStacks()) {
			if (!stack.isEnchantable()) {
				continue;
			}
			for (Holder.Reference<Enchantment> enchantmentHolder : enchantments) {
				if (enchantmentHolder.is(EnchantmentTags.CURSE)) {
					continue;
				}
				Enchantment enchantment = enchantmentHolder.value();
				if (!enchantment.isSupportedItem(stack) ||
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
				return getGrindstoneRecipe(topInput, bottomInput, ResourceLocation.withDefaultNamespace("grindstone.self_repair." + itemId));
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
		grindstoneMenu.slots.get(BOTTOM_SLOT).set(topInput);
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
		ItemEnchantments itemEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);

		for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
			Holder<Enchantment> holder = entry.getKey();
			int j = entry.getIntValue();
			if (!holder.is(EnchantmentTags.CURSE)) {
				i += holder.value().getMinCost(j);
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
			Inventory fakeInventory = new Inventory(player, new EntityEquipment());
			GRINDSTONE_MENU = new GrindstoneMenu(0, fakeInventory);
			return GRINDSTONE_MENU;
		}
		return GRINDSTONE_MENU;
	}
}
