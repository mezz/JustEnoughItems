package mezz.jei.test;

import java.util.Collections;

import mezz.jei.Internal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.elements.DrawableBlank;
import mezz.jei.gui.textures.JeiTextureMap;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackHelper;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.startup.ModRegistry;
import mezz.jei.startup.StackHelper;
import mezz.jei.test.lib.TestModIdHelper;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.junit.Assert;
import org.junit.Test;

public class RecipeCatalystRegistrationTest {
	@Test
	public void unavailableCatalystDoesNotAbortPluginRegistration() {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
		FluidStack water = new FluidStack(FluidRegistry.WATER, 1000);
		StackHelper stackHelper = new StackHelper(new SubtypeRegistry());
		Internal.setStackHelper(stackHelper);
		ModIngredientRegistration ingredients = new ModIngredientRegistration();
		ingredients.register(VanillaTypes.ITEM, Collections.emptyList(), new ItemStackHelper(stackHelper), new ItemStackRenderer());
		ingredients.register(VanillaTypes.FLUID, Collections.singletonList(water), new FluidStackHelper(), new FluidStackRenderer());
		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		IngredientRegistry ingredientRegistry = ingredients.createIngredientRegistry(new TestModIdHelper(), blacklist);
		GuiHelper guiHelper = new GuiHelper(ingredientRegistry, new Textures(new JeiTextureMap("textures")));
		JeiHelpers helpers = new JeiHelpers(guiHelper, ingredientRegistry, blacklist, stackHelper);
		ModRegistry registration = new ModRegistry(helpers, ingredientRegistry);
		TestCategory category = new TestCategory();

		IModPlugin plugin = new IModPlugin() {
			@Override
			public void register(IModRegistry registry) {
				registry.addRecipeCategories(category);
				registry.addRecipeCatalyst(ItemStack.EMPTY, category.getUid());
				registry.addRecipeCatalyst(water, category.getUid());
			}
		};
		plugin.register(registration);
		RecipeRegistry recipes = registration.createRecipeRegistry(ingredientRegistry);

		Assert.assertEquals(Collections.singletonList(water), recipes.getRecipeCatalysts(category, true));
	}

	private static class TestCategory implements IRecipeCategory<IRecipeWrapper> {
		@Override
		public String getUid() {
			return "jei.catalyst_test";
		}

		@Override
		public String getTitle() {
			return "Catalyst Test";
		}

		@Override
		public String getModName() {
			return "JEI";
		}

		@Override
		public IDrawable getBackground() {
			return new DrawableBlank(1, 1);
		}

		@Override
		public void setRecipe(IRecipeLayout layout, IRecipeWrapper recipe, IIngredients ingredients) {
		}
	}
}
