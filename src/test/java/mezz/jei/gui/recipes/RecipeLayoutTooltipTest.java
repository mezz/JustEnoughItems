package mezz.jei.gui.recipes;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import mezz.jei.Internal;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.GuiHelper;
import mezz.jei.gui.textures.JeiTextureMap;
import mezz.jei.gui.textures.Textures;
import mezz.jei.ingredients.IngredientBlacklistInternal;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.plugins.vanilla.crafting.ShapelessRecipeWrapper;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackHelper;
import mezz.jei.plugins.vanilla.ingredients.fluid.FluidStackRenderer;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackHelper;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import mezz.jei.runtime.JeiHelpers;
import mezz.jei.runtime.SubtypeRegistry;
import mezz.jei.startup.ModIngredientRegistration;
import mezz.jei.startup.StackHelper;
import mezz.jei.test.lib.TestModIdHelper;
import mezz.jei.util.Translator;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.input.Keyboard;

public class RecipeLayoutTooltipTest {
	private RecipeLayout layout;

	@Before
	public void setup() throws ReflectiveOperationException {
		if (!Bootstrap.isRegistered()) {
			Bootstrap.register();
		}
		StackHelper stackHelper = new StackHelper(new SubtypeRegistry());
		Internal.setStackHelper(stackHelper);
		ModIngredientRegistration registration = new ModIngredientRegistration();
		registration.register(VanillaTypes.ITEM, Collections.emptyList(), new ItemStackHelper(stackHelper), new ItemStackRenderer());
		registration.register(VanillaTypes.FLUID, Collections.emptyList(), new FluidStackHelper(), new FluidStackRenderer());
		IngredientBlacklistInternal blacklist = new IngredientBlacklistInternal();
		IngredientRegistry ingredients = registration.createIngredientRegistry(new TestModIdHelper(), blacklist);
		Internal.setIngredientRegistry(ingredients);
		GuiHelper guiHelper = new GuiHelper(ingredients, new Textures(new JeiTextureMap("textures")));
		JeiHelpers helpers = new JeiHelpers(guiHelper, ingredients, blacklist, stackHelper);
		Internal.setHelpers(helpers);
		// Install the real filter without subscribing to the untransformed Forge event bus.
		Field filterField = Internal.class.getDeclaredField("ingredientFilter");
		filterField.setAccessible(true);
		filterField.set(null, new IngredientFilter(blacklist));

		IRecipe recipe = CraftingManager.REGISTRY.getObject(new ResourceLocation("minecraft:book"));
		Assert.assertTrue("Use Minecraft's actual shapeless book recipe", recipe instanceof ShapelessRecipes);
		layout = RecipeLayout.create(0, new CraftingRecipeCategory(guiHelper), new ShapelessRecipeWrapper<>(helpers, recipe), null, 0, 0);
		Assert.assertNotNull("The native crafting category must create the recipe layout", layout);
	}

	@Test
	public void hoveringRecipeBodyOutsideShapelessIconCanShowTransferShortcut() {
		// Empty recipe space is outside the shapeless icon and every ingredient slot.
		List<String> tooltip = layout.getTooltipStrings(70, 35);
		Assert.assertTrue("The icon must not supply a tooltip outside its bounds", tooltip.isEmpty());

		appendTransferShortcut(tooltip);

		Assert.assertEquals("The transfer hint must still be available away from the icon", 1, tooltip.size());
	}

	@Test
	public void hoveringShapelessIconPreservesItsTooltipAlongsideTransferShortcut() {
		List<String> tooltip = layout.getTooltipStrings(CraftingRecipeCategory.width - 4, 4);
		Assert.assertEquals(Collections.singletonList(Translator.translateToLocal("jei.tooltip.shapeless.recipe")), tooltip);

		appendTransferShortcut(tooltip);

		Assert.assertEquals("Appending the hint must accept the icon's immutable tooltip list", 2, tooltip.size());
		Assert.assertEquals(Translator.translateToLocal("jei.tooltip.shapeless.recipe"), tooltip.get(0));
	}

	private void appendTransferShortcut(List<String> tooltip) {
		int oldTransferKey = KeyBindings.transferRecipeGui.getKeyCode();
		int oldMaxTransferKey = KeyBindings.maxTransferRecipeGui.getKeyCode();
		try {
			KeyBindings.transferRecipeGui.setKeyCode(Keyboard.KEY_F7);
			KeyBindings.maxTransferRecipeGui.setKeyCode(Keyboard.KEY_NONE);
			RecipeTransferButton button = layout.getRecipeTransferButton();
			Assert.assertNotNull(button);
			button.addShortcutTooltips(tooltip);
		} finally {
			KeyBindings.transferRecipeGui.setKeyCode(oldTransferKey);
			KeyBindings.maxTransferRecipeGui.setKeyCode(oldMaxTransferKey);
		}
	}
}
