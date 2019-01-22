package mezz.jei.gui;

import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableBuilder;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.gui.elements.DrawableAnimated;
import mezz.jei.gui.elements.DrawableBlank;
import mezz.jei.gui.elements.DrawableBuilder;
import mezz.jei.gui.elements.DrawableIngredient;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.elements.DrawableSprite;
import mezz.jei.gui.textures.TextureInfo;
import mezz.jei.gui.textures.Textures;
import mezz.jei.util.ErrorUtil;

public class GuiHelper implements IGuiHelper {
	private final IIngredientRegistry ingredientRegistry;
	private final IDrawableStatic slotDrawable;
	private final IDrawableStatic tabSelected;
	private final IDrawableStatic tabUnselected;
	private final IDrawableStatic shapelessIcon;
	private final IDrawableStatic arrowPrevious;
	private final IDrawableStatic arrowNext;
	private final IDrawableStatic recipeTransfer;
	private final IDrawableStatic configButtonIcon;
	private final IDrawableStatic configButtonCheatIcon;
	private final IDrawableStatic bookmarkButtonDisabledIcon;
	private final IDrawableStatic bookmarkButtonEnabledIcon;
	private final DrawableNineSliceTexture buttonDisabled;
	private final DrawableNineSliceTexture buttonEnabled;
	private final DrawableNineSliceTexture buttonHighlight;
	private final DrawableNineSliceTexture guiBackground;
	private final DrawableNineSliceTexture recipeBackground;
	private final DrawableNineSliceTexture searchBackground;
	private final DrawableNineSliceTexture catalystTab;
	private final DrawableNineSliceTexture nineSliceSlot;
	private final IDrawableStatic infoIcon;
	private final IDrawableStatic flameIcon;

	public GuiHelper(IIngredientRegistry ingredientRegistry, Textures textures) {
		this.ingredientRegistry = ingredientRegistry;
		this.slotDrawable = createDrawable(textures.slot);
		this.nineSliceSlot = createNineSliceDrawable(textures.slot);

		this.tabSelected = createDrawable(textures.tabSelected);
		this.tabUnselected = createDrawable(textures.tabUnselected);

		this.buttonDisabled = createNineSliceDrawable(textures.buttonDisabled);
		this.buttonEnabled = createNineSliceDrawable(textures.buttonEnabled);
		this.buttonHighlight = createNineSliceDrawable(textures.buttonHighlight);
		this.guiBackground = createNineSliceDrawable(textures.guiBackground);
		this.recipeBackground = createNineSliceDrawable(textures.recipeBackground);
		this.searchBackground = createNineSliceDrawable(textures.searchBackground);
		this.catalystTab = createNineSliceDrawable(textures.catalystTab);

		this.shapelessIcon = createDrawable(textures.shapelessIcon);
		this.arrowPrevious = createDrawable(textures.arrowPrevious);
		this.arrowNext = createDrawable(textures.arrowNext);
		this.recipeTransfer = createDrawable(textures.recipeTransfer);

		this.configButtonIcon = createDrawable(textures.configButtonIcon);
		this.configButtonCheatIcon = createDrawable(textures.configButtonCheatIcon);
		this.bookmarkButtonDisabledIcon = createDrawable(textures.bookmarkButtonDisabledIcon);
		this.bookmarkButtonEnabledIcon = createDrawable(textures.bookmarkButtonEnabledIcon);

		this.infoIcon = createDrawable(textures.infoIcon);
		this.flameIcon = createDrawable(textures.flameIcon);
	}

	@Override
	public IDrawableBuilder drawableBuilder(ResourceLocation resourceLocation, int u, int v, int width, int height) {
		return new DrawableBuilder(resourceLocation, u, v, width, height);
	}

	@Override
	public IDrawableAnimated createAnimatedDrawable(IDrawableStatic drawable, int ticksPerCycle, IDrawableAnimated.StartDirection startDirection, boolean inverted) {
		ErrorUtil.checkNotNull(drawable, "drawable");
		ErrorUtil.checkNotNull(startDirection, "startDirection");
		return new DrawableAnimated(drawable, ticksPerCycle, startDirection, inverted);
	}

	@Override
	public IDrawableStatic getSlotDrawable() {
		return slotDrawable;
	}

	@Override
	public IDrawableStatic createBlankDrawable(int width, int height) {
		return new DrawableBlank(width, height);
	}

	@Override
	public <V> IDrawable createDrawableIngredient(V ingredient) {
		IIngredientRenderer<V> ingredientRenderer = ingredientRegistry.getIngredientRenderer(ingredient);
		return new DrawableIngredient<>(ingredient, ingredientRenderer);
	}

	@Override
	public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
		return new CraftingGridHelper(craftInputSlot1, craftOutputSlot);
	}

	@Override
	public ITickTimer createTickTimer(int ticksPerCycle, int maxValue, boolean countDown) {
		return new TickTimer(ticksPerCycle, maxValue, countDown);
	}

	private IDrawableStatic createDrawable(TextureInfo textureInfo) {
		return new DrawableSprite(textureInfo);
	}

	private DrawableNineSliceTexture createNineSliceDrawable(TextureInfo textureInfo) {
		return new DrawableNineSliceTexture(textureInfo);
	}

	public IDrawableStatic getTabSelected() {
		return tabSelected;
	}

	public IDrawableStatic getTabUnselected() {
		return tabUnselected;
	}

	public IDrawableStatic getShapelessIcon() {
		return shapelessIcon;
	}

	public IDrawableStatic getArrowPrevious() {
		return arrowPrevious;
	}

	public IDrawableStatic getArrowNext() {
		return arrowNext;
	}

	public IDrawableStatic getRecipeTransfer() {
		return recipeTransfer;
	}

	public IDrawableStatic getConfigButtonIcon() {
		return configButtonIcon;
	}

	public IDrawableStatic getConfigButtonCheatIcon() {
		return configButtonCheatIcon;
	}

	public IDrawableStatic getBookmarkButtonDisabledIcon() {
		return bookmarkButtonDisabledIcon;
	}

	public IDrawableStatic getBookmarkButtonEnabledIcon() {
		return bookmarkButtonEnabledIcon;
	}

	public DrawableNineSliceTexture getButtonDisabled() {
		return buttonDisabled;
	}

	public DrawableNineSliceTexture getButtonEnabled() {
		return buttonEnabled;
	}

	public DrawableNineSliceTexture getButtonHighlight() {
		return buttonHighlight;
	}

	public DrawableNineSliceTexture getButtonForState(int state) {
		if (state == 0) {
			return getButtonDisabled();
		} else if (state == 2) {
			return getButtonHighlight();
		} else {
			return getButtonEnabled();
		}
	}

	public DrawableNineSliceTexture getGuiBackground() {
		return guiBackground;
	}

	public DrawableNineSliceTexture getRecipeBackground() {
		return recipeBackground;
	}

	public DrawableNineSliceTexture getSearchBackground() {
		return searchBackground;
	}

	public IDrawableStatic getInfoIcon() {
		return infoIcon;
	}

	public DrawableNineSliceTexture getCatalystTab() {
		return catalystTab;
	}

	public DrawableNineSliceTexture getNineSliceSlot() {
		return nineSliceSlot;
	}

	public IDrawableStatic getFlameIcon() {
		return flameIcon;
	}
}
