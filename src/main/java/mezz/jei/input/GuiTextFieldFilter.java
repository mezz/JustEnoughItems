package mezz.jei.input;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.LinkedList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.Rectangle2d;

import mezz.jei.Internal;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.HoverChecker;
import mezz.jei.gui.elements.DrawableNineSliceTexture;
import mezz.jei.gui.ingredients.IIngredientListElement;
import mezz.jei.gui.overlay.IIngredientGridSource;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

public class GuiTextFieldFilter extends TextFieldWidget {
	private static final int MAX_HISTORY = 100;
	private static final int maxSearchLength = 128;
	private static final List<String> history = new LinkedList<>();

	private final HoverChecker hoverChecker;
	private final IIngredientGridSource ingredientSource;
	private final IWorldConfig worldConfig;
	private boolean previousKeyboardRepeatEnabled;

	private final DrawableNineSliceTexture background;

	public GuiTextFieldFilter(IIngredientGridSource ingredientSource, IWorldConfig worldConfig) {
		// TODO narrator string
		super(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, StringTextComponent.field_240750_d_);
		this.worldConfig = worldConfig;

		setMaxStringLength(maxSearchLength);
		this.hoverChecker = new HoverChecker();
		this.ingredientSource = ingredientSource;

		this.background = Internal.getTextures().getSearchBackground();
	}

	public void updateBounds(Rectangle2d area) {
		this.field_230690_l_ = area.getX();
		this.field_230691_m_ = area.getY();
		this.field_230688_j_ = area.getWidth();
		this.field_230689_k_ = area.getHeight();
		this.hoverChecker.updateBounds(area.getY(), area.getY() + area.getHeight(), area.getX(), area.getX() + area.getWidth());
		setSelectionPos(getCursorPosition());
	}

	public void update() {
		String filterText = worldConfig.getFilterText();
		if (!filterText.equals(getText())) {
			setText(filterText);
		}
		List<IIngredientListElement<?>> ingredientList = ingredientSource.getIngredientList(filterText);
		if (ingredientList.size() == 0) {
			setTextColor(0xFFFF0000);
		} else {
			setTextColor(0xFFFFFFFF);
		}
	}

	@Override
	public boolean func_231046_a_(int keyCode, int scanCode, int modifiers) {
		boolean handled = super.func_231046_a_(keyCode, scanCode, modifiers);
		if (!handled && !history.isEmpty()) {
			if (keyCode == GLFW.GLFW_KEY_UP) {
				String currentText = getText();
				int historyIndex = history.indexOf(currentText);
				if (historyIndex < 0) {
					if (saveHistory()) {
						historyIndex = history.size() - 1;
					} else {
						historyIndex = history.size();
					}
				}
				if (historyIndex > 0) {
					String historyString = history.get(historyIndex - 1);
					setText(historyString);
					handled = true;
				}
			} else if (keyCode == GLFW.GLFW_KEY_DOWN) {
				String currentText = getText();
				int historyIndex = history.indexOf(currentText);
				if (historyIndex >= 0) {
					String historyString;
					if (historyIndex + 1 < history.size()) {
						historyString = history.get(historyIndex + 1);
					} else {
						historyString = "";
					}
					setText(historyString);
					handled = true;
				}
			} else if (KeyBindings.isEnterKey(keyCode)) {
				saveHistory();
			}
		}
		return handled;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return hoverChecker.checkHover(mouseX, mouseY);
	}

	public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (mouseButton == 1) {
			setText("");
			return worldConfig.setFilterText("");
		} else {
			super.func_231044_a_(mouseX, mouseY, mouseButton);
		}
		return false;
	}

	@Override
	public void func_230996_d_(boolean keyboardFocus) {
		final boolean previousFocus = func_230999_j_();
		super.func_230996_d_(keyboardFocus);

		if (previousFocus != keyboardFocus) {
			Minecraft minecraft = Minecraft.getInstance();
			if (keyboardFocus) {
				previousKeyboardRepeatEnabled = minecraft.keyboardListener.repeatEventsEnabled;
				minecraft.keyboardListener.enableRepeatEvents(true);
			} else {
				minecraft.keyboardListener.enableRepeatEvents(previousKeyboardRepeatEnabled);
			}

			saveHistory();
		}
	}

	private boolean saveHistory() {
		String text = getText();
		if (text.length() > 0) {
			history.remove(text);
			history.add(text);
			if (history.size() > MAX_HISTORY) {
				history.remove(0);
			}
			return true;
		}
		return false;
	}

	// begin hack to draw our own background texture instead of the default one
	private boolean isDrawing = false;

	@Override
	protected boolean getEnableBackgroundDrawing() {
		if (this.isDrawing) {
			return false;
		}
		return super.getEnableBackgroundDrawing();
	}

	@Override
	public void func_230431_b_(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.isDrawing = true;
		if (this.getVisible()) {
			RenderSystem.color4f(1, 1, 1, 1);
			background.draw(matrixStack, this.field_230690_l_, this.field_230691_m_, this.field_230688_j_, this.field_230689_k_);
		}
		super.func_230431_b_(matrixStack, mouseX, mouseY, partialTicks);
		this.isDrawing = false;
	}
	// end background hack
}
