package mezz.jei.debug;

import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.widgets.IButtonWidget;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.debug.ingredients.DebugIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
	private final IDrawable alternateIcon;
	private @Nullable IJeiRuntime runtime;
	private boolean hiddenRecipes;

	public DebugRecipeCategory(IGuiHelper guiHelper, IPlatformFluidHelper<F> platformFluidHelper, IIngredientManager ingredientManager) {
		this.platformFluidHelper = platformFluidHelper;
		this.ingredientManager = ingredientManager;
		this.localizedName = Component.literal("debug");

		ResourceLocation backgroundTexture = ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "textures/jei/gui/debug.png");
		this.tankBackground = guiHelper.createDrawable(backgroundTexture, 220, 196, 18, 60);
		this.tankOverlay = guiHelper.createDrawable(backgroundTexture, 238, 196, 18, 60);
		this.item = guiHelper.createDrawableItemStack(new ItemStack(Items.ACACIA_LEAVES));
		this.alternateIcon = guiHelper.createDrawableItemStack(new ItemStack(Items.OAK_SAPLING));
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
		return item;
	}

	@Override
	public void draw(DebugRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		if (!recipe.getLargeIngredientList().isEmpty()) {
			var font = Minecraft.getInstance().font;
			guiGraphics.drawString(font, "10,000 candidates", 50, 0, 0xFF000000, false);
			guiGraphics.drawString(font, "Last: #10,000 (coal)", 0, 42, 0xFF000000, false);
		} else if (runtime != null) {
			this.item.draw(guiGraphics, 50, 20);

			IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
			Minecraft minecraft = Minecraft.getInstance();
			guiGraphics.drawString(minecraft.font, ingredientFilter.getFilterText(), 20, 52, 0, false);

			IIngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
			Optional<ITypedIngredient<?>> ingredientUnderMouse = getIngredientUnderMouse(ingredientListOverlay, runtime.getBookmarkOverlay());
			ingredientUnderMouse.ifPresent(typedIngredient -> drawIngredientName(minecraft, guiGraphics, typedIngredient));
		}
	}

	private static Optional<ITypedIngredient<?>> getIngredientUnderMouse(IIngredientListOverlay ingredientListOverlay, IBookmarkOverlay bookmarkOverlay) {
		return ingredientListOverlay.getIngredientUnderMouse()
			.or(bookmarkOverlay::getIngredientUnderMouse);
	}

	private <T> void drawIngredientName(Minecraft minecraft, GuiGraphics guiGraphics, ITypedIngredient<T> ingredient) {
		IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient.getType());
		String serialized = ingredientHelper.getResourceLocation(ingredient.getIngredient()).toString();
		guiGraphics.drawString(minecraft.font, serialized, 50, 52, 0, false);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, DebugRecipe recipe, IFocusGroup focuses) {
		if (!recipe.getLargeIngredientList().isEmpty()) {
			builder.addInputSlot(50, 18)
				.setStandardSlotBackground()
				.addItemStacks(recipe.getLargeIngredientList())
				.addRichTooltipCallback((slot, tooltip) -> tooltip.add(Component.literal("10,000 candidates; rotation limited to 100")));
			builder.addOutputSlot(110, 18)
				.setStandardSlotBackground()
				.addItemStack(new ItemStack(Items.COAL));
			return;
		}
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
				.addIngredient(fluidType, platformFluidHelper.create(Fluids.LAVA.defaultFluidState().holder(), amount));
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
				platformFluidHelper.create(Fluids.LAVA.defaultFluidState().holder(), (int) ((1.0 + Math.random()) * bucketVolume)),
				new ItemStack(Items.ACACIA_LEAVES)
			))
			.addRichTooltipCallback((recipeSlotView, tooltip) -> {
				switch (recipeSlotView.getRole()) {
					case INPUT -> tooltip.add(Component.literal("Input DebugIngredient"));
					case OUTPUT -> tooltip.add(Component.literal("Output DebugIngredient"));
					case CATALYST -> tooltip.add(Component.literal("Catalyst DebugIngredient"));
				}
			});
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, DebugRecipe recipe, IFocusGroup focuses) {
		IButtonWidget<?> button = builder.addButton(40, 20, this::onButtonPress)
			.setMessage(recipe.getButtonText())
			.setTooltip(Component.literal("button tooltip!"));
		if (recipe.getLargeIngredientList().isEmpty()) {
			button.setPosition(120, 16);
			IButtonWidget<?> iconButton = builder.addButton(20, 20, this::onButtonPress)
				.setIcon(item)
				.setPosition(0, 24)
				.setTooltip(Component.literal("icon button tooltip!"));
			var iconCycle = builder.addCycleButton(List.of(item, alternateIcon), item, 20, 20, iconButton::setIcon)
				.setIcon(Function.identity())
				.setPosition(0, 0)
				.setTooltip(Component.literal("Cycle the test icon"));
			builder.addCycleButton(
					List.of(true, false),
					true,
					40,
					20,
					active -> {
						button.setActive(active);
						iconButton.setActive(active);
						iconCycle.setActive(active);
					}
				)
				.setMessage(CommonComponents::optionStatus)
				.setPosition(120, 38)
				.setTooltip(Component.literal("Enable or disable the test buttons"));
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, DebugRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		tooltip.add(Component.literal("Debug Recipe Category Tooltip is very long and going to wrap").withStyle(ChatFormatting.GOLD));

		tooltip.add(Component.literal("tooltip debug").withStyle(ChatFormatting.BOLD));
		tooltip.add(Component.literal(mouseX + ", " + mouseY));
	}

	private void onButtonPress() {
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

	@Override
	public @Nullable ResourceLocation getRegistryName(DebugRecipe recipe) {
		return recipe.getRegistryName();
	}

	@Override
	public boolean needsRecipeBorder() {
		return false;
	}
}
