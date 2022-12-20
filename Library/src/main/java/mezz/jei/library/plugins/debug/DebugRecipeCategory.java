package mezz.jei.library.plugins.debug;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Constants;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.library.plugins.jei.ingredients.DebugIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DebugRecipeCategory<F> implements IRecipeCategory<DebugRecipe> {
	public static final RecipeType<DebugRecipe> TYPE = RecipeType.create(ModIds.JEI_ID, "debug", DebugRecipe.class);
	public static final int RECIPE_WIDTH = 160;
	public static final int RECIPE_HEIGHT = 60;
	private final IDrawable background;
	private final IPlatformFluidHelper<F> platformFluidHelper;
	private final IIngredientManager ingredientManager;
	private final Component localizedName;
	private final IDrawable tankBackground;
	private final IDrawable tankOverlay;
	private final IDrawable item;
	private @Nullable IJeiRuntime runtime;
	private boolean hiddenRecipes;

	public DebugRecipeCategory(IGuiHelper guiHelper, IPlatformFluidHelper<F> platformFluidHelper, IIngredientManager ingredientManager) {
		this.background = guiHelper.createBlankDrawable(RECIPE_WIDTH, RECIPE_HEIGHT);
		this.platformFluidHelper = platformFluidHelper;
		this.ingredientManager = ingredientManager;
		this.localizedName = Component.literal("debug");

		ResourceLocation backgroundTexture = new ResourceLocation(ModIds.JEI_ID, Constants.TEXTURE_GUI_PATH + "debug.png");
		this.tankBackground = guiHelper.createDrawable(backgroundTexture, 220, 196, 18, 60);
		this.tankOverlay = guiHelper.createDrawable(backgroundTexture, 238, 196, 18, 60);
		this.item = guiHelper.createDrawableItemStack(new ItemStack(Items.ACACIA_LEAVES));
	}

	public void setRuntime(IJeiRuntime runtime) {
		this.runtime = runtime;
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
	public void draw(DebugRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
		if (runtime != null) {
			this.item.draw(poseStack, 50, 20);

			IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.font.draw(poseStack, ingredientFilter.getFilterText(), 20, 52, 0);

			IIngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
			Optional<ITypedIngredient<?>> ingredientUnderMouse = getIngredientUnderMouse(ingredientListOverlay, runtime.getBookmarkOverlay());
			ingredientUnderMouse.ifPresent(typedIngredient -> drawIngredientName(minecraft, poseStack, typedIngredient));
		}

		Button button = recipe.getButton();
		button.render(poseStack, (int) mouseX, (int) mouseY, 0);
	}

	private static Optional<ITypedIngredient<?>> getIngredientUnderMouse(IIngredientListOverlay ingredientListOverlay, IBookmarkOverlay bookmarkOverlay) {
		return ingredientListOverlay.getIngredientUnderMouse()
			.or(bookmarkOverlay::getIngredientUnderMouse);
	}

	private <T> void drawIngredientName(Minecraft minecraft, PoseStack poseStack, ITypedIngredient<T> ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient.getType());
		String jeiUid = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Ingredient);
		minecraft.font.draw(poseStack, jeiUid, 50, 52, 0);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DebugRecipe recipe, IFocusGroup focuses) {
		// ITEM type
		builder.addSlot(RecipeIngredientRole.OUTPUT, 70, 0)
			.addItemStack(new ItemStack(Items.WATER_BUCKET))
			.addItemStack(new ItemStack(Items.BUNDLE));

		builder.addSlot(RecipeIngredientRole.INPUT, 110, 0)
				.addIngredientsUnsafe(Arrays.asList(new ItemStack(Items.LAVA_BUCKET), null));

		// FLUID type
		long bucketVolume = platformFluidHelper.bucketVolume();
		IIngredientType<F> fluidType = platformFluidHelper.getFluidIngredientType();
		{
			long capacity = 10 * bucketVolume;
			// random amount between half capacity and full
			long amount = (capacity / 2) + (int) ((Math.random() * capacity) / 2);
			builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 0)
				.setFluidRenderer(capacity, false, 16, 58)
				.setOverlay(tankOverlay, -1, -1)
				.setBackground(tankBackground, -1, -1)
				.addFluidStack(Fluids.WATER, amount);
		}

		{
			long capacity = 2 * bucketVolume;
			// random amount between half capacity and full
			long amount = (capacity / 2) + (int) ((Math.random() * capacity) / 2);
			builder.addSlot(RecipeIngredientRole.INPUT, 24, 0)
				.setFluidRenderer(capacity, true, 12, 47)
				.addIngredient(fluidType, platformFluidHelper.create(Fluids.LAVA, amount));
		}

		// DEBUG type
		builder.addSlot(RecipeIngredientRole.INPUT, 40, 0)
			.addIngredients(DebugIngredient.TYPE, List.of(new DebugIngredient(0), new DebugIngredient(1)));

		builder.addSlot(RecipeIngredientRole.OUTPUT, 40, 16)
			.addIngredient(DebugIngredient.TYPE, new DebugIngredient(2));

		// mixed types
		builder.addSlot(RecipeIngredientRole.INPUT, 40, 32)
			.addIngredient(DebugIngredient.TYPE, new DebugIngredient(3))
			.addIngredientsUnsafe(List.of(
				platformFluidHelper.create(Fluids.LAVA, (int) ((1.0 + Math.random()) * bucketVolume)),
				new ItemStack(Items.LAVA_BUCKET)
			))
			.addTooltipCallback((recipeSlotView, tooltip) -> {
				switch (recipeSlotView.getRole()) {
					case INPUT -> tooltip.add(Component.literal("Input DebugIngredient"));
					case OUTPUT -> tooltip.add(Component.literal( "Output DebugIngredient"));
					case CATALYST -> tooltip.add(Component.literal("Catalyst DebugIngredient"));
				}
			});
	}

	@Override
	public List<Component> getTooltipStrings(DebugRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		List<Component> tooltipStrings = new ArrayList<>();
		tooltipStrings.add(Component.literal("Debug Recipe Category Tooltip is very long and going to wrap").withStyle(ChatFormatting.GOLD));

		if (recipe.checkHover(mouseX, mouseY)) {
			tooltipStrings.add(Component.literal("button tooltip!"));
		} else {
			MutableComponent debug = Component.literal("tooltip debug");
			tooltipStrings.add(debug.withStyle(ChatFormatting.BOLD));
		}
		tooltipStrings.add(Component.literal(mouseX + ", " + mouseY));
		return tooltipStrings;
	}

	@Override
	public boolean handleInput(DebugRecipe recipe, double mouseX, double mouseY, InputConstants.Key input) {
		if (input.getType() != InputConstants.Type.MOUSE) {
			return false;
		}
		Button button = recipe.getButton();
		int mouseButton = input.getValue();
		if (mouseButton == 0 && button.mouseClicked(mouseX, mouseY, mouseButton)) {
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			if (player != null) {
				Screen screen = new InventoryScreen(player);
				minecraft.setScreen(screen);
			}
			if (runtime != null) {
				IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
				String filterText = ingredientFilter.getFilterText();
				ingredientFilter.setFilterText(filterText + " test");

				IRecipeManager recipeManager = runtime.getRecipeManager();
				if (!hiddenRecipes) {
					recipeManager.hideRecipeCategory(RecipeTypes.CRAFTING);
					hiddenRecipes = true;
				} else {
					recipeManager.unhideRecipeCategory(RecipeTypes.CRAFTING);
					hiddenRecipes = false;
				}
			}
			return true;
		}
		return false;
	}
}
