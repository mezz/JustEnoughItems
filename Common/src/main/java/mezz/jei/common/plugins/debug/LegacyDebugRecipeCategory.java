package mezz.jei.common.plugins.debug;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.ModIds;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IBookmarkOverlay;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientListOverlay;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Constants;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.plugins.jei.ingredients.DebugIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("removal")
public class LegacyDebugRecipeCategory<F> implements IRecipeCategory<DebugRecipe> {
	public static final ResourceLocation UID = new ResourceLocation(ModIds.JEI_ID, "legacy_debug");
	public static final int RECIPE_WIDTH = 160;
	public static final int RECIPE_HEIGHT = 60;
	private final IDrawable background;
	private final IPlatformFluidHelper<F> platformFluidHelper;
	private final Component localizedName;
	private final IDrawable tankBackground;
	private final IDrawable tankOverlay;
	private final IDrawable item;
	private boolean hiddenRecipes;

	public LegacyDebugRecipeCategory(IGuiHelper guiHelper, IPlatformFluidHelper<F> platformFluidHelper) {
		this.background = guiHelper.createBlankDrawable(RECIPE_WIDTH, RECIPE_HEIGHT);
		this.platformFluidHelper = platformFluidHelper;
		this.localizedName = new TextComponent("debug");

		ResourceLocation backgroundTexture = new ResourceLocation(ModIds.JEI_ID, Constants.TEXTURE_GUI_PATH + "debug.png");
		this.tankBackground = guiHelper.drawableBuilder(backgroundTexture, 220, 196, 18, 60)
			.addPadding(-1, -1, -1, -1)
			.build();
		this.tankOverlay = guiHelper.drawableBuilder(backgroundTexture, 238, 196, 18, 60)
			.addPadding(-1, -1, -1, -1)
			.build();
		this.item = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.ACACIA_LEAVES));
	}

	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Override
	public Class<? extends DebugRecipe> getRecipeClass() {
		return DebugRecipe.class;
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
	public void setIngredients(DebugRecipe recipe, IIngredients ingredients) {
		long bucketVolume = platformFluidHelper.bucketVolume();
		IIngredientType<F> fluidType = platformFluidHelper.getFluidIngredientType();
		F water = platformFluidHelper.create(Fluids.WATER, (int) ((1.0 + Math.random()) * bucketVolume));
		F lava = platformFluidHelper.create(Fluids.LAVA, (int) ((1.0 + Math.random()) * bucketVolume));

		ingredients.setInputs(fluidType, List.of(water, lava));

		ingredients.setInput(VanillaTypes.ITEM_STACK, new ItemStack(Items.STICK));

		ingredients.setInputLists(DebugIngredient.TYPE, List.of(
			List.of(new DebugIngredient(0), new DebugIngredient(1))
		));

		ingredients.setOutputs(DebugIngredient.TYPE, List.of(
			new DebugIngredient(2),
			new DebugIngredient(3)
		));
	}

	@Override
	public void draw(DebugRecipe recipe, PoseStack poseStack, double mouseX, double mouseY) {
		IJeiRuntime runtime = JeiDebugPlugin.jeiRuntime;
		if (runtime != null) {
			this.item.draw(poseStack, 50, 20);

			IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
			Minecraft minecraft = Minecraft.getInstance();
			minecraft.font.draw(poseStack, ingredientFilter.getFilterText(), 20, 52, 0);

			IIngredientListOverlay ingredientListOverlay = runtime.getIngredientListOverlay();
			IIngredientManager ingredientManager = runtime.getIngredientManager();
			Collection<IIngredientType<?>> ingredientTypes = ingredientManager.getRegisteredIngredientTypes();
			for (IIngredientType<?> ingredientType : ingredientTypes) {
				Object ingredientUnderMouse = ingredientListOverlay.getIngredientUnderMouse(ingredientType);
				if (ingredientUnderMouse != null) {
					drawIngredientName(minecraft, poseStack, ingredientUnderMouse);
					break;
				} else {
					IBookmarkOverlay bookmarkOverlay = runtime.getBookmarkOverlay();
					ingredientUnderMouse = bookmarkOverlay.getIngredientUnderMouse(ingredientType);
					if (ingredientUnderMouse != null) {
						drawIngredientName(minecraft, poseStack, ingredientUnderMouse);
						break;
					}
				}
			}
		}

		Button button = recipe.getButton();
		button.render(poseStack, (int) mouseX, (int) mouseY, 0);
	}

	private static <T> void drawIngredientName(Minecraft minecraft, PoseStack poseStack, T ingredient) {
		IIngredientManager ingredientManager = JeiDebugPlugin.ingredientManager;
		if (ingredientManager != null) {
			IIngredientHelper<T> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);
			String jeiUid = ingredientHelper.getUniqueId(ingredient, UidContext.Ingredient);
			minecraft.font.draw(poseStack, jeiUid, 50, 52, 0);
		}
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, DebugRecipe recipe, IIngredients ingredients) {
		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

		guiItemStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (input) {
				tooltip.add(new TextComponent(slotIndex + " Input itemStack"));
			} else {
				tooltip.add(new TextComponent(slotIndex + " Output itemStack"));
			}
		});

		guiItemStacks.init(0, false, 70, 0);
		guiItemStacks.init(1, true, 110, 0);
		guiItemStacks.set(0, new ItemStack(Items.WATER_BUCKET));
		guiItemStacks.set(1, Arrays.asList(new ItemStack(Items.LAVA_BUCKET), null));

//		IGuiFluidStackGroup guiFluidStacks = recipeLayout.getFluidStacks();
//		guiFluidStacks.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
//			if (input) {
//				tooltip.add(new TextComponent(slotIndex + " Input fluidStack"));
//			} else {
//				tooltip.add(new TextComponent(slotIndex + " Output fluidStack"));
//			}
//		});
//
//		guiFluidStacks.init(0, false, 90, 0, 16, 58, 16000, false, tankOverlay);
//		guiFluidStacks.init(1, true, 24, 0, 12, 47, 2000, true, null);
//
//		guiFluidStacks.setBackground(0, tankBackground);
//
//		List<List<FluidStack>> fluidInputs = ingredients.getInputs(ForgeTypes.FLUID_STACK);
//		guiFluidStacks.set(0, fluidInputs.get(0));
//		guiFluidStacks.set(1, fluidInputs.get(1));

		IGuiIngredientGroup<DebugIngredient> debugIngredientsGroup = recipeLayout.getIngredientsGroup(DebugIngredient.TYPE);
		debugIngredientsGroup.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
			if (input) {
				tooltip.add(new TextComponent(slotIndex + " Input DebugIngredient"));
			} else {
				tooltip.add(new TextComponent(slotIndex + " Output DebugIngredient"));
			}
		});

		debugIngredientsGroup.init(0, true, 40, 0);
		debugIngredientsGroup.init(1, false, 40, 16);
		debugIngredientsGroup.init(2, false, 40, 32);

		debugIngredientsGroup.set(ingredients);
	}

	@Override
	public List<Component> getTooltipStrings(DebugRecipe recipe, double mouseX, double mouseY) {
		List<Component> tooltipStrings = new ArrayList<>();
		tooltipStrings.add(new TextComponent("Debug Recipe Category Tooltip is very long and going to wrap").withStyle(ChatFormatting.GOLD));

		if (recipe.checkHover(mouseX, mouseY)) {
			tooltipStrings.add(new TextComponent("button tooltip!"));
		} else {
			TextComponent debug = new TextComponent("tooltip debug");
			tooltipStrings.add(debug.withStyle(ChatFormatting.BOLD));
		}
		tooltipStrings.add(new TextComponent(mouseX + ", " + mouseY));
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
			IJeiRuntime runtime = JeiDebugPlugin.jeiRuntime;
			if (runtime != null) {
				IIngredientFilter ingredientFilter = runtime.getIngredientFilter();
				String filterText = ingredientFilter.getFilterText();
				ingredientFilter.setFilterText(filterText + " test");

				IRecipeManager recipeManager = runtime.getRecipeManager();
				if (!hiddenRecipes) {
					recipeManager.hideRecipeCategory(VanillaRecipeCategoryUid.CRAFTING);
					hiddenRecipes = true;
				} else {
					recipeManager.unhideRecipeCategory(VanillaRecipeCategoryUid.CRAFTING);
					hiddenRecipes = false;
				}
			}
			return true;
		}
		return false;
	}
}
