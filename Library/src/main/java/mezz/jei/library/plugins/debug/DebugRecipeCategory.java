package mezz.jei.library.plugins.debug;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.library.plugins.debug.ingredients.DebugIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DebugRecipeCategory<F> implements IRecipeCategory<DebugRecipe> {
	public static final RecipeType<DebugRecipe> TYPE = RecipeType.create(ModIds.JEI_ID, "debug", DebugRecipe.class);
	public static final int RECIPE_WIDTH = 160;
	public static final int RECIPE_HEIGHT = 60;
	private final IPlatformFluidHelper<F> platformFluidHelper;
	private final IIngredientManager ingredientManager;
	private final Component localizedName;
	private final IDrawable tankBackground;
	private final IDrawable tankOverlay;
	private final IDrawable item;
	private @Nullable IJeiRuntime runtime;
	private boolean hiddenRecipes;

	public DebugRecipeCategory(IGuiHelper guiHelper, IPlatformFluidHelper<F> platformFluidHelper, IIngredientManager ingredientManager) {
		this.platformFluidHelper = platformFluidHelper;
		this.ingredientManager = ingredientManager;
		this.localizedName = Component.literal("debug");

		ResourceLocation backgroundTexture = new ResourceLocation(ModIds.JEI_ID, "textures/jei/gui/debug.png");
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
	public int getWidth() {
		return RECIPE_WIDTH;
	}

	@Override
	public int getHeight() {
		return RECIPE_HEIGHT;
	}

	@Override
	public IDrawable getIcon() {
		Textures textures = Internal.getTextures();
		return textures.getConfigButtonIcon();
	}

	@Override
	public void draw(DebugRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		if (runtime != null) {
			this.item.draw(guiGraphics, 50, 20);

			IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
			Minecraft minecraft = Minecraft.getInstance();
			guiGraphics.drawString(minecraft.font, ingredientFilter.getFilterText(), 20, 52, 0, false);

			IIngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
			Optional<ITypedIngredient<?>> ingredientUnderMouse = getIngredientUnderMouse(ingredientListOverlay, runtime.getBookmarkOverlay());
			ingredientUnderMouse.ifPresent(typedIngredient -> drawIngredientName(minecraft, guiGraphics, typedIngredient));
		}

		Button button = recipe.getButton();
		button.render(guiGraphics, (int) mouseX, (int) mouseY, 0);
	}

	private static Optional<ITypedIngredient<?>> getIngredientUnderMouse(IIngredientListOverlay ingredientListOverlay, IBookmarkOverlay bookmarkOverlay) {
		return ingredientListOverlay.getIngredientUnderMouse()
			.or(bookmarkOverlay::getIngredientUnderMouse);
	}

	private <T> void drawIngredientName(Minecraft minecraft, GuiGraphics guiGraphics, ITypedIngredient<T> ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient.getType());
		String jeiUid = ingredientHelper.getUniqueId(ingredient.getIngredient(), UidContext.Ingredient);
		guiGraphics.drawString(minecraft.font, jeiUid, 50, 52, 0, false);
	}

	@SuppressWarnings("removal")
	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DebugRecipe recipe, IFocusGroup focuses) {
		// ITEM type
		builder.addOutputSlot(70, 0)
			.addItemStack(new ItemStack(Items.FARMLAND))
			.addItemStack(new ItemStack(Items.BUNDLE));

		builder.addInputSlot(110, 0)
				.addIngredientsUnsafe(Arrays.asList(new ItemStack(Items.RABBIT), null));

		// FLUID type
		long bucketVolume = platformFluidHelper.bucketVolume();
		IIngredientType<F> fluidType = platformFluidHelper.getFluidIngredientType();
		{
			long capacity = 10 * bucketVolume;
			// random amount between half capacity and full
			long amount = (capacity / 2) + (int) ((Math.random() * capacity) / 2);
			builder.addOutputSlot(90, 0)
				.setFluidRenderer(capacity, false, 16, 58)
				.setOverlay(tankOverlay, -1, -1)
				.setBackground(tankBackground, -1, -1)
				.addFluidStack(Fluids.WATER, amount);
		}

		{
			long capacity = 2 * bucketVolume;
			// random amount between half capacity and full
			long amount = (capacity / 2) + (int) ((Math.random() * capacity) / 2);
			builder.addInputSlot(24, 0)
				.setFluidRenderer(capacity, true, 12, 47)
				.addIngredient(fluidType, platformFluidHelper.create(Fluids.LAVA, amount));
		}

		// DEBUG type
		builder.addInputSlot(40, 0)
			.addIngredients(DebugIngredient.TYPE, List.of(new DebugIngredient(0), new DebugIngredient(1)));

		builder.addOutputSlot(40, 16)
			.addIngredient(DebugIngredient.TYPE, new DebugIngredient(2));

		// mixed types
		builder.addInputSlot(40, 32)
			.addIngredient(DebugIngredient.TYPE, new DebugIngredient(3))
			.addIngredientsUnsafe(List.of(
				platformFluidHelper.create(Fluids.LAVA, (int) ((1.0 + Math.random()) * bucketVolume)),
				new ItemStack(Items.ACACIA_LEAVES)
			))
			.addTooltipCallback(new mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback() {
				@SuppressWarnings("removal")
				@Override
				public void onTooltip(IRecipeSlotView recipeSlotView, List<Component> tooltip) {
					switch (recipeSlotView.getRole()) {
						case INPUT -> tooltip.add(Component.literal("Input DebugIngredient"));
						case OUTPUT -> tooltip.add(Component.literal( "Output DebugIngredient"));
						case CATALYST -> tooltip.add(Component.literal("Catalyst DebugIngredient"));
					}
				}

				@Override
				public void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
					switch (recipeSlotView.getRole()) {
						case INPUT -> tooltip.add(Component.literal("Input DebugIngredient"));
						case OUTPUT -> tooltip.add(Component.literal( "Output DebugIngredient"));
						case CATALYST -> tooltip.add(Component.literal("Catalyst DebugIngredient"));
					}
				}
			});
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, DebugRecipe recipe, IFocusGroup focuses) {
		builder.addInputHandler(new JeiInputHandler(recipe, new ScreenRectangle(0, 0, RECIPE_WIDTH, RECIPE_HEIGHT)));
	}

	@SuppressWarnings({"removal"})
	@Override
	public List<Component> getTooltipStrings(DebugRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		JeiTooltip tooltip = new JeiTooltip();
		getTooltip(tooltip, recipe, recipeSlotsView, mouseX, mouseY);
		return tooltip.toLegacyToComponents();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, DebugRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		tooltip.add(Component.literal("Debug Recipe Category Tooltip is very long and going to wrap").withStyle(ChatFormatting.GOLD));

		if (recipe.checkHover(mouseX, mouseY)) {
			tooltip.add(Component.literal("button tooltip!"));
		} else {
			MutableComponent debug = Component.literal("tooltip debug");
			tooltip.add(debug.withStyle(ChatFormatting.BOLD));
		}
		tooltip.add(Component.literal(mouseX + ", " + mouseY));
	}

	public class JeiInputHandler implements IJeiInputHandler {
		private final DebugRecipe recipe;
		private final ScreenRectangle area;

		public JeiInputHandler(DebugRecipe recipe, ScreenRectangle area) {
			this.recipe = recipe;
			this.area = area;
		}

		@Override
		public ScreenRectangle getArea() {
			return area;
		}

		@Override
		public boolean handleInput(double mouseX, double mouseY, IJeiUserInput userInput) {
			if (!userInput.is(Internal.getKeyMappings().getLeftClick())) {
				return false;
			}
			InputConstants.Key key = userInput.getKey();
			Button button = recipe.getButton();
			int mouseButton = key.getValue();
			if (mouseButton == 0 && button.mouseClicked(mouseX, mouseY, mouseButton)) {
				if (!userInput.isSimulate()) {
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
				}
				return true;
			}
			return false;
		}
	}

	@Override
	public @Nullable ResourceLocation getRegistryName(DebugRecipe recipe) {
		return recipe.getRegistryName();
	}
}
