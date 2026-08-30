package mezz.jei.library.plugins.vanilla.anvil;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.ISmithingCategoryExtension;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SmithingRecipeCategory extends AbstractRecipeCategory<UpgradeRecipe> implements IExtendableSmithingRecipeCategory {
	private final Map<Class<? extends UpgradeRecipe>, ISmithingCategoryExtension<?>> extensions = new HashMap<>();

	public SmithingRecipeCategory(IGuiHelper guiHelper) {
		super(
			RecipeTypes.SMITHING,
			Blocks.SMITHING_TABLE.getName(),
			guiHelper.createDrawableItemLike(Blocks.SMITHING_TABLE),
			125,
			28
		);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, UpgradeRecipe recipe, IFocusGroup focuses) {
		ISmithingCategoryExtension<? super UpgradeRecipe> extension = getExtension(recipe);
		if (extension == null) {
			return;
		}

		IRecipeSlotBuilder base = builder.addInputSlot(1, 6)
			.setStandardSlotBackground();

		IRecipeSlotBuilder addition = builder.addInputSlot(50, 6)
			.setStandardSlotBackground();

		IRecipeSlotBuilder output = builder.addOutputSlot(108, 6)
			.setStandardSlotBackground();

		extension.setBase(recipe, base);
		extension.setAddition(recipe, addition);
		extension.setOutput(recipe, output);
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, UpgradeRecipe recipe, IFocusGroup focuses) {
		builder.addRecipeArrowWidget()
			.setPosition(79, 6);
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
