package mezz.jei.library.plugins.debug;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.Internal;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Iterator;

public class ObnoxiouslyLargeCategory implements IRecipeCategory<ObnoxiouslyLargeRecipe> {
	public static final RecipeType<ObnoxiouslyLargeRecipe> TYPE = RecipeType.create(ModIds.JEI_ID, "obnoxiously_large_recipe", ObnoxiouslyLargeRecipe.class);
	private static final int WIDTH = 300;
	private static final int HEIGHT = 300;

	private final IIngredientManager ingredientManager;
	private final IDrawable background;
	private final IDrawable slotBackground;
	private final IDrawable icon;

	public ObnoxiouslyLargeCategory(IGuiHelper helper, IIngredientManager ingredientManager) {
		this.ingredientManager = ingredientManager;
		this.slotBackground = helper.getSlotDrawable();
		this.background = helper.createBlankDrawable(WIDTH + (2 * slotBackground.getWidth()), HEIGHT);
		this.icon = Internal.getTextures().getFlameIcon();
	}

	@Override
	public RecipeType<ObnoxiouslyLargeRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return Component.literal("Obnoxiously Large Recipe Category");
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
	public void setRecipe(IRecipeLayoutBuilder builder, ObnoxiouslyLargeRecipe recipe, IFocusGroup focuses) {
		int slotWidth = slotBackground.getWidth();
		int slotHeight = slotBackground.getHeight();
		int xCount = WIDTH / slotWidth;
		int yCount = HEIGHT / slotHeight;
		int xOffset = (WIDTH - (xCount * slotWidth)) / 2;
		int yOffset = (HEIGHT - (yCount * slotHeight)) / 2;

		Collection<ItemStack> allItems = ingredientManager.getAllIngredients(VanillaTypes.ITEM_STACK);
		Iterator<ItemStack> iterator = allItems.iterator();

		for (int x = 0; x < xCount; x++) {
			for (int y = 0; y < yCount; y++) {
				int xPos = xOffset + (x * slotWidth);
				int yPos = yOffset + (y * slotHeight);
				ItemStack stack = iterator.next();
				builder.addSlot(RecipeIngredientRole.INPUT, xPos + 1, yPos + 1)
					.setBackground(slotBackground, -1, -1)
					.addItemStack(stack);
			}
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, WIDTH + slotWidth, HEIGHT / 2)
			.setBackground(slotBackground, -1, -1)
			.addItemStack(iterator.next());
	}

	@Override
	public ResourceLocation getRegistryName(ObnoxiouslyLargeRecipe recipe) {
		return recipe.getRecipeId();
	}
}
