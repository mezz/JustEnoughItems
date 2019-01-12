package mezz.jei.gui;

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
import mezz.jei.gui.textures.JeiTextureMap;
import mezz.jei.gui.textures.Textures;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

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
	private final IDrawableStatic infoIcon;
	private final IDrawableStatic flameIcon;

	public GuiHelper(IIngredientRegistry ingredientRegistry, Textures textures) {
		this.ingredientRegistry = ingredientRegistry;
		JeiTextureMap textureMap = textures.getTextureMap();
		this.slotDrawable = createDrawable(textureMap, textures.slot);

		this.tabSelected = createDrawable(textureMap, textures.tabSelected);
		this.tabUnselected = createDrawable(textureMap, textures.tabUnselected);

		this.buttonDisabled = createNineSliceDrawable(textureMap, textures.buttonDisabled, 2, 2, 2, 2);
		this.buttonEnabled = createNineSliceDrawable(textureMap, textures.buttonEnabled, 2, 2, 2, 2);
		this.buttonHighlight = createNineSliceDrawable(textureMap, textures.buttonHighlight, 2, 2, 2, 2);
		this.guiBackground = createNineSliceDrawable(textureMap, textures.guiBackground, 4, 4, 4, 4);
		this.recipeBackground = createNineSliceDrawable(textureMap, textures.recipeBackground, 4, 4, 4, 4);
		this.searchBackground = createNineSliceDrawable(textureMap, textures.searchBackground, 4, 4, 4, 4);
		this.catalystTab = createNineSliceDrawable(textureMap, textures.catalystTab, 6, 7, 6, 6);

		this.shapelessIcon = drawableBuilder(textureMap, textures.shapelessIcon)
			.trim(1, 2, 1, 1)
			.build();
		this.arrowPrevious = drawableBuilder(textureMap, textures.arrowPrevious)
			.trim(0, 0, 1, 1)
			.build();
		this.arrowNext = drawableBuilder(textureMap, textures.arrowNext)
			.trim(0, 0, 1, 1)
			.build();
		this.recipeTransfer = createDrawable(textureMap, textures.recipeTransfer);

		this.configButtonIcon = createDrawable(textureMap, textures.configButtonIcon);
		this.configButtonCheatIcon = createDrawable(textureMap, textures.configButtonCheatIcon);
		this.bookmarkButtonDisabledIcon = createDrawable(textureMap, textures.bookmarkButtonDisabledIcon);
		this.bookmarkButtonEnabledIcon = createDrawable(textureMap, textures.bookmarkButtonEnabledIcon);

		this.infoIcon = createDrawable(textureMap, textures.infoIcon);
		this.flameIcon = createDrawable(textureMap, textures.flameIcon);
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

	private IDrawableStatic createDrawable(JeiTextureMap textureMap, TextureAtlasSprite sprite) {
		return drawableBuilder(textureMap, sprite).build();
	}

	private DrawableNineSliceTexture createNineSliceDrawable(JeiTextureMap textureMap, TextureAtlasSprite sprite, int leftWidth, int rightWidth, int topHeight, int bottomHeight) {
		return new DrawableNineSliceTexture(textureMap.getLocation(), sprite.getOriginX(), sprite.getOriginY(), sprite.getIconWidth(), sprite.getIconHeight(), leftWidth, rightWidth, topHeight, bottomHeight, textureMap.getWidth(), textureMap.getHeight());
	}

	private IDrawableBuilder drawableBuilder(JeiTextureMap textureMap, TextureAtlasSprite sprite) {
		return drawableBuilder(textureMap.getLocation(), sprite.getOriginX(), sprite.getOriginY(), sprite.getIconWidth(), sprite.getIconHeight())
			.setTextureSize(textureMap.getWidth(), textureMap.getHeight());
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

	public IDrawableStatic getFlameIcon() {
		return flameIcon;
	}
}
