package mezz.jei.plugins.debug;

import mezz.jei.Internal;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.gui.textures.Textures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class DebugFocusRecipeCategory implements IRecipeCategory<DebugRecipe> {
	public static final RecipeType<DebugRecipe> TYPE = RecipeType.create(ModIds.JEI_ID, "debug_focus", DebugRecipe.class);
	public static final int RECIPE_WIDTH = 160;
	public static final int RECIPE_HEIGHT = 60;
	private final IDrawable background;
	private final Component localizedName;

	public DebugFocusRecipeCategory(IGuiHelper guiHelper) {
		this.background = guiHelper.createBlankDrawable(RECIPE_WIDTH, RECIPE_HEIGHT);
		this.localizedName = new TextComponent("debug_focus");
	}

	@SuppressWarnings("removal")
	@Override
	public ResourceLocation getUid() {
		return TYPE.getUid();
	}

	@SuppressWarnings("removal")
	@Override
	public Class<? extends DebugRecipe> getRecipeClass() {
		return TYPE.getRecipeClass();
	}

	@Override
	public RecipeType<DebugRecipe> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return localizedName;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		Textures textures = Internal.getTextures();
		return textures.getConfigButtonIcon();
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DebugRecipe recipe, IFocusGroup focuses) {
		IRecipeSlotBuilder inputSlot = builder.addSlot(RecipeIngredientRole.INPUT, 0, 0)
			.addItemStacks(List.of(
				new ItemStack(Items.BUCKET),
				new ItemStack(Items.WATER_BUCKET),
				new ItemStack(Items.LAVA_BUCKET),
				new ItemStack(Items.POWDER_SNOW_BUCKET),
				new ItemStack(Items.AXOLOTL_BUCKET),
				new ItemStack(Items.SALMON_BUCKET),
				new ItemStack(Items.COD_BUCKET),
				new ItemStack(Items.PUFFERFISH_BUCKET),
				new ItemStack(Items.TROPICAL_FISH_BUCKET)
			));

		IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, 20, 0)
			.addItemStack(ItemStack.EMPTY)
			.addIngredients(VanillaTypes.FLUID, List.of(
				new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME),
				new FluidStack(Fluids.LAVA, FluidAttributes.BUCKET_VOLUME)
			))
			.addItemStacks(List.of(
				new ItemStack(Items.SNOW_BLOCK),
				new ItemStack(Items.AXOLOTL_SPAWN_EGG),
				new ItemStack(Items.SALMON),
				new ItemStack(Items.COD),
				new ItemStack(Items.PUFFERFISH),
				new ItemStack(Items.TROPICAL_FISH)
			));

		IIngredientAcceptor<?> invisibleSlot = builder.addInvisibleIngredients(RecipeIngredientRole.INPUT)
			.addItemStacks(List.of(
				new ItemStack(Items.ACACIA_BOAT),
				new ItemStack(Items.ACACIA_BUTTON),
				new ItemStack(Items.ACACIA_DOOR),
				new ItemStack(Items.ACACIA_LOG),
				new ItemStack(Items.ACACIA_PLANKS),
				new ItemStack(Items.ACACIA_FENCE),
				new ItemStack(Items.ACACIA_FENCE_GATE),
				new ItemStack(Items.ACACIA_LEAVES),
				new ItemStack(Items.ACACIA_PRESSURE_PLATE)
			));

		builder.createFocusLink(inputSlot, outputSlot, invisibleSlot);
	}

}
