package mezz.jei.common.gui.elements;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IButtonWidget;
import mezz.jei.common.Internal;
import mezz.jei.common.input.IInputTarget;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.input.UserInput;
import mezz.jei.common.input.interaction.IInputInteraction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Objects;

public abstract class AbstractRecipeButtonWidget<THIS extends IButtonWidget<THIS>> extends AbstractRecipeWidgetBuilder<THIS> implements IButtonWidget<THIS>, IJeiInputHandler, IInputTarget {
	private final int width;
	private final int height;
	private RecipeButtonContents contents = new RecipeButtonContents.Text(() -> CommonComponents.EMPTY);
	private boolean active = true;
	private boolean visible = true;
	private @Nullable ButtonPress pendingClick;

	protected AbstractRecipeButtonWidget(int width, int height) {
		super(0, 0);
		if (width <= 0 || height <= 0) {
			throw new IllegalArgumentException("Button width and height must be positive");
		}
		this.width = width;
		this.height = height;
	}

	protected final void setContents(RecipeButtonContents contents) {
		this.contents = Objects.requireNonNull(contents);
	}

	protected abstract void onPress(IJeiUserInput input);

	@Override
	public final THIS setMessage(Component message) {
		Objects.requireNonNull(message);
		setContents(new RecipeButtonContents.Text(() -> message));
		return getThis();
	}

	@Override
	public final THIS setIcon(IDrawable icon) {
		Objects.requireNonNull(icon);
		setContents(new RecipeButtonContents.Icon(() -> icon));
		return getThis();
	}

	@Override
	public final int getWidth() {
		return width;
	}

	@Override
	public final int getHeight() {
		return height;
	}

	@Override
	public final ScreenRectangle getArea() {
		return getScreenRectangle();
	}

	@Override
	public final THIS setActive(boolean active) {
		this.active = active;
		if (!active) {
			resetInput();
		}
		return getThis();
	}

	@Override
	public final THIS setVisible(boolean visible) {
		this.visible = visible;
		if (!visible) {
			resetInput();
		}
		return getThis();
	}

	@Override
	public final void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
		if (!visible) {
			return;
		}
		boolean hovered = isMouseOver(mouseX, mouseY);
		boolean pressed = pendingClick != null && hovered;
		Internal.getTextures().getButtonBackground(active, hovered, pressed).draw(graphics, 0, 0, width, height);
		var pose = graphics.pose();
		pose.pushMatrix();
		try {
			if (pressed) {
				pose.translate(0.5f, 0.5f);
			}
			contents.draw(graphics, width, height, active, hovered);
		} finally {
			pose.popMatrix();
		}
	}

	@Override
	public final void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
		if (!visible || !isMouseOver(mouseX, mouseY)) {
			return;
		}
		contents.addTooltip(tooltip, width);
		addConfiguredTooltip(tooltip);
	}

	private boolean acceptsInput(double mouseX, double mouseY, IJeiUserInput input) {
		if (!active || !visible || !isMouseOver(mouseX, mouseY)) {
			return false;
		}
		return input.getKey().getType() == InputConstants.Type.MOUSE &&
			input.getKey().getValue() == InputConstants.MOUSE_BUTTON_LEFT;
	}

	private void activate(IJeiUserInput input) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		onPress(input);
	}

	@Override
	public final boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
		if (!acceptsInput(mouseX, mouseY, input)) {
			return false;
		}
		if (!input.isSimulate()) {
			activate(input);
		}
		return true;
	}

	@Override
	public final Optional<IInputInteraction> beginInput(Screen screen, UserInput input, IInternalKeyMappings keys) {
		if (!acceptsInput(input.getMouseX(), input.getMouseY(), input)) {
			return Optional.empty();
		}
		ButtonPress click = new ButtonPress();
		pendingClick = click;
		return Optional.of(click);
	}

	@Override
	public final void resetInput() {
		pendingClick = null;
	}

	private final class ButtonPress implements IInputInteraction {
		@Override
		public void complete(UserInput input) {
			if (pendingClick != this) {
				return;
			}
			pendingClick = null;
			if (acceptsInput(input.getMouseX(), input.getMouseY(), input)) {
				activate(input);
			}
		}

		@Override
		public void cancel() {
			if (pendingClick == this) {
				pendingClick = null;
			}
		}
	}
}
