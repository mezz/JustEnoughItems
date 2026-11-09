package mezz.jei.neoforge.tests.client;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

@JeiPlugin
public final class InspectionTestPlugin implements IModPlugin {
	static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("jeiclienttests", "inspection");
	static final RecipeType<Integer> TYPE = RecipeType.create("jeiclienttests", "inspection", Integer.class);
	static volatile IJeiRuntime runtime;

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		runtime = jeiRuntime;
	}

	@Override
	public void onRuntimeUnavailable() {
		runtime = null;
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new Category());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(TYPE, IntStream.range(0, 30).boxed().toList());
	}

	private static final class Category implements IRecipeCategory<Integer> {
		@Override
		public RecipeType<Integer> getRecipeType() { return TYPE; }
		@Override
		public Component getTitle() { return Component.literal("Inspection test"); }
		@Override
		public @Nullable IDrawable getIcon() { return null; }
		@Override
		public int getWidth() { return 120; }
		@Override
		public int getHeight() { return 60; }

		@Override
		public void setRecipe(IRecipeLayoutBuilder builder, Integer recipe, IFocusGroup focuses) {
			builder.addSlot(RecipeIngredientRole.INPUT, 4, 4).addItemStack(new ItemStack(Items.OAK_LOG));
			builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 4).addItemStack(new ItemStack(Items.APPLE, recipe + 1));
		}
	}
}
