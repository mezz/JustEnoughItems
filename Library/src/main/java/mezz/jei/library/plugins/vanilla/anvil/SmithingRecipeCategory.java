package mezz.jei.library.plugins.vanilla.anvil;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SmithingRecipeCategory implements IRecipeCategory<UpgradeRecipe>, IExtendableSmithingRecipeCategory {
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slot;
	private final IDrawable recipeArrow;
	private final Map<Class<? extends UpgradeRecipe>, ISmithingCategoryExtension<?>> extensions = new HashMap<>();

	public SmithingRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(125, 28);
		slot = guiHelper.getSlotDrawable();
		icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.SMITHING_TABLE));
		Textures textures = Internal.getTextures();
		recipeArrow = textures.getRecipeArrow();
	}

	@Override
	public RecipeType<UpgradeRecipe> getRecipeType() {
		return RecipeTypes.SMITHING;
	}

	@Override
	public Component getTitle() {
		return Blocks.SMITHING_TABLE.getName();
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, UpgradeRecipe recipe, IFocusGroup focuses) {
		ISmithingCategoryExtension<? super UpgradeRecipe> extension = getExtension(recipe);
		if (extension == null) {
			return;
		}

		IRecipeSlotBuilder base = builder.addSlot(RecipeIngredientRole.INPUT, 1, 6)
			.setBackground(slot, -1, -1);

		IRecipeSlotBuilder addition = builder.addSlot(RecipeIngredientRole.INPUT, 50, 6)
			.setBackground(slot, -1, -1);

		extension.setBase(recipe, base);
		extension.setAddition(recipe, addition);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 108, 6)
			.addItemStack(recipe.getResultItem())
			.setBackground(slot, -1, -1);
	}

	@Override
	public void draw(UpgradeRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
		recipeArrow.draw(stack, 79, 7);
	}

	@Override
	public boolean isHandled(UpgradeRecipe recipe) {
		return getExtension(recipe) != null;
	}

	@Override
	public ResourceLocation getRegistryName(UpgradeRecipe recipe) {
		return recipe.getId();
	}

	@Override
	public <R extends UpgradeRecipe> void addExtension(Class<? extends R> recipeClass, ISmithingCategoryExtension<R> extension) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		ErrorUtil.checkNotNull(extension, "extension");
		if (extensions.containsKey(recipeClass)) {
			throw new IllegalArgumentException("An extension has already been registered for: " + recipeClass);
		}
		extensions.put(recipeClass, extension);
	}

	@Nullable
	private <R extends UpgradeRecipe> ISmithingCategoryExtension<? super R> getExtension(UpgradeRecipe recipe) {
		{
			ISmithingCategoryExtension<?> extension = extensions.get(recipe.getClass());
			if (extension != null) {
				//noinspection unchecked
				return (ISmithingCategoryExtension<? super R>) extension;
			}
		}
		for (Map.Entry<Class<? extends UpgradeRecipe>, ISmithingCategoryExtension<?>> e : extensions.entrySet()) {
			if (e.getKey().isInstance(recipe)) {
				//noinspection unchecked
				return (ISmithingCategoryExtension<? super R>) e.getValue();
			}
		}
		return null;
	}
}
