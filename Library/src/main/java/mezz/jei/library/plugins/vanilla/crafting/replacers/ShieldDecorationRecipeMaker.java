package mezz.jei.library.plugins.vanilla.crafting.replacers;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.util.RegistryUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

public final class ShieldDecorationRecipeMaker {
	public static List<RecipeHolder<CraftingRecipe>> createRecipes() {
		Iterable<Holder<Item>> banners = RegistryUtil.getRegistry(Registries.ITEM).getTagOrEmpty(ItemTags.BANNERS);

		Set<DyeColor> colors = EnumSet.noneOf(DyeColor.class);

		return StreamSupport.stream(banners.spliterator(), false)
			.filter(Holder::isBound)
			.map(Holder::value)
			.filter(BannerItem.class::isInstance)
			.map(BannerItem.class::cast)
			.filter(item -> colors.add(item.getColor()))
			.map(ShieldDecorationRecipeMaker::createRecipe)
			.toList();
	}

	private static RecipeHolder<CraftingRecipe> createRecipe(BannerItem banner) {
		ItemStackTemplate output = createOutput(banner);

		Identifier id = Identifier.fromNamespaceAndPath(ModIds.MINECRAFT_ID, "jei.shield.decoration." + banner.getDescriptionId());
		ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, id);
		CraftingRecipe recipe = new ShapelessRecipe(
			"jei.shield.decoration",
			CraftingBookCategory.MISC,
			output,
			List.of(
				Ingredient.of(Items.SHIELD),
				Ingredient.of(banner)
			)
		);
		return new RecipeHolder<>(resourceKey, recipe);
	}

	private static ItemStackTemplate createOutput(BannerItem banner) {
		DyeColor color = banner.getColor();
		DataComponentPatch components = DataComponentPatch.builder()
			.set(DataComponents.BASE_COLOR, color)
			.build();
		return new ItemStackTemplate(Items.SHIELD, components);
	}

	private ShieldDecorationRecipeMaker() {

	}
}
