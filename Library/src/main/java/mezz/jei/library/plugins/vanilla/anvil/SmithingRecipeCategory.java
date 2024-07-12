package mezz.jei.library.plugins.vanilla.anvil;

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
import mezz.jei.library.util.RecipeUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SmithingRecipeCategory implements IRecipeCategory<RecipeHolder<SmithingRecipe>>, IExtendableSmithingRecipeCategory {
	private final IDrawable background;
	private final IDrawable icon;
	private final IDrawable slot;
	private final IDrawable recipeArrow;
	private final Map<Class<? extends SmithingRecipe>, ISmithingCategoryExtension<?>> extensions = new HashMap<>();

	public SmithingRecipeCategory(IGuiHelper guiHelper) {
		background = guiHelper.createBlankDrawable(108, 28);
		slot = guiHelper.getSlotDrawable();
		icon = guiHelper.createDrawableItemStack(new ItemStack(Blocks.SMITHING_TABLE));
		Textures textures = Internal.getTextures();
		recipeArrow = textures.getRecipeArrow();
	}

	@Override
	public RecipeType<RecipeHolder<SmithingRecipe>> getRecipeType() {
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
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<SmithingRecipe> recipeHolder, IFocusGroup focuses) {
		SmithingRecipe recipe = recipeHolder.value();

		ISmithingCategoryExtension<? super SmithingRecipe> extension = getExtension(recipe);
		if (extension == null) {
			return;
		}

		IRecipeSlotBuilder template = builder.addSlot(RecipeIngredientRole.INPUT, 1, 6)
			.setBackground(slot, -1, -1);

		IRecipeSlotBuilder base = builder.addSlot(RecipeIngredientRole.INPUT, 19, 6)
			.setBackground(slot, -1, -1);

		IRecipeSlotBuilder addition = builder.addSlot(RecipeIngredientRole.INPUT, 37, 6)
			.setBackground(slot, -1, -1);

		extension.setTemplate(recipe, template);
		extension.setBase(recipe, base);
		extension.setAddition(recipe, addition);

		builder.addSlot(RecipeIngredientRole.OUTPUT, 91, 6)
			.addItemStack(RecipeUtil.getResultItem(recipe))
			.setBackground(slot, -1, -1);
	}

	@Override
	public void draw(RecipeHolder<SmithingRecipe> recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		recipeArrow.draw(guiGraphics, 61, 7);
	}

	@Override
	public boolean isHandled(RecipeHolder<SmithingRecipe> recipeHolder) {
		SmithingRecipe recipe = recipeHolder.value();
		var extension = getExtension(recipe);
		return extension != null;
	}

	@Override
	public ResourceLocation getRegistryName(RecipeHolder<SmithingRecipe> recipe) {
		return recipe.id();
	}

	@Override
	public <R extends SmithingRecipe> void addExtension(Class<? extends R> recipeClass, ISmithingCategoryExtension<R> extension) {
		ErrorUtil.checkNotNull(recipeClass, "recipeClass");
		ErrorUtil.checkNotNull(extension, "extension");
		if (extensions.containsKey(recipeClass)) {
			throw new IllegalArgumentException("An extension has already been registered for: " + recipeClass);
		}
		extensions.put(recipeClass, extension);
	}

	@Nullable
	private <R extends SmithingRecipe> ISmithingCategoryExtension<? super R> getExtension(SmithingRecipe recipe) {
		{
			ISmithingCategoryExtension<?> extension = extensions.get(recipe.getClass());
			if (extension != null) {
				//noinspection unchecked
				return (ISmithingCategoryExtension<? super R>) extension;
			}
		}
		for (Map.Entry<Class<? extends SmithingRecipe>, ISmithingCategoryExtension<?>> e : extensions.entrySet()) {
			if (e.getKey().isInstance(recipe)) {
				//noinspection unchecked
				return (ISmithingCategoryExtension<? super R>) e.getValue();
			}
		}
		return null;
	}
}
