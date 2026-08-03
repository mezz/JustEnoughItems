package mezz.jei.common.chat;

import mezz.jei.common.chat.JeiChatItemLinks.IngredientLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.font.ActiveArea;
import net.minecraft.client.gui.font.EmptyArea;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JeiChatItemLinkHover {
	private JeiChatItemLinkHover() {
	}

	public record HoveredText(Style style, Rect2i area) {
	}

	public static Optional<Style> getHoveredStyle(Screen screen, double mouseX, double mouseY) {
		if (!(screen instanceof ChatScreen chatScreen)) {
			return Optional.empty();
		}

		ActiveTextCollector.ClickableStyleFinder finder = new ActiveTextCollector.ClickableStyleFinder(
			chatScreen.getFont(),
			(int) mouseX,
			(int) mouseY
		);
		captureClickableText(finder);

		Style style = finder.result();
		return Optional.ofNullable(style);
	}

	public static Optional<HoveredText> getHoveredText(Screen screen, double mouseX, double mouseY) {
		if (!(screen instanceof ChatScreen chatScreen)) {
			return Optional.empty();
		}
		HoveredTextFinder finder = new HoveredTextFinder(
			chatScreen.getFont(),
			(int) mouseX,
			(int) mouseY
		);
		captureClickableText(finder);
		return finder.result();
	}

	public static Optional<IngredientLink> getIngredientLink(@Nullable Style style) {
		if (style == null) {
			return Optional.empty();
		}
		ClickEvent clickEvent = style.getClickEvent();
		if (clickEvent instanceof ClickEvent.RunCommand(String command)) {
			return JeiChatItemLinks.parseShowRecipeCommand(command);
		}
		return Optional.empty();
	}

	private static void captureClickableText(ActiveTextCollector activeTextCollector) {
		Minecraft minecraft = Minecraft.getInstance();
		ChatComponent chatComponent = minecraft.gui.getChat();
		int screenHeight = minecraft.getWindow().getGuiScaledHeight();
		int ticks = minecraft.gui.getGuiTicks();
		chatComponent.captureClickableText(activeTextCollector, screenHeight, ticks, true);
	}

	private static final class HoveredTextFinder implements ActiveTextCollector {
		private static final ActiveTextCollector.Parameters INITIAL = new ActiveTextCollector.Parameters(new Matrix3x2f());

		private final Font font;
		private final int mouseX;
		private final int mouseY;
		private ActiveTextCollector.Parameters defaultParameters = INITIAL;

		@Nullable
		private HoveredText result;

		public HoveredTextFinder(Font font, int mouseX, int mouseY) {
			this.font = font;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

		@Override
		public ActiveTextCollector.Parameters defaultParameters() {
			return this.defaultParameters;
		}

		@Override
		public void defaultParameters(ActiveTextCollector.Parameters newParameters) {
			this.defaultParameters = newParameters;
		}

		@Override
		public void accept(TextAlignment alignment, int anchorX, int y, ActiveTextCollector.Parameters parameters, FormattedCharSequence text) {
			int leftX = alignment.calculateLeft(anchorX, this.font, text);
			GuiTextRenderState renderState = new GuiTextRenderState(
				this.font,
				text,
				parameters.pose(),
				leftX,
				y,
				ARGB.white(parameters.opacity()),
				0,
				true,
				true,
				parameters.scissor()
			);
			HoveredText hoveredText = findHoveredText(renderState);
			if (hoveredText != null) {
				this.result = hoveredText;
			}
		}

		@Override
		public void acceptScrolling(
			Component message,
			int centerX,
			int left,
			int right,
			int top,
			int bottom,
			ActiveTextCollector.Parameters parameters
		) {
			int lineWidth = this.font.width(message);
			int lineHeight = 9;
			this.defaultScrollingHelper(message, centerX, left, right, top, bottom, lineWidth, lineHeight, parameters);
		}

		public Optional<HoveredText> result() {
			return Optional.ofNullable(this.result);
		}

		private @Nullable HoveredText findHoveredText(GuiTextRenderState renderState) {
			ScreenRectangle bounds = renderState.bounds();
			if (bounds == null || !bounds.containsPoint(this.mouseX, this.mouseY)) {
				return null;
			}

			Matrix3x2f localPose = renderState.pose.invert(new Matrix3x2f());
			Vector2f localMousePos = localPose.transformPosition(new Vector2f(this.mouseX, this.mouseY));
			float localMouseX = localMousePos.x();
			float localMouseY = localMousePos.y();
			List<ActiveArea> activeAreas = getActiveAreas(renderState);
			int hoveredIndex = getHoveredIndex(activeAreas, localMouseX, localMouseY);
			if (hoveredIndex < 0) {
				return null;
			}

			ActiveArea hoveredArea = activeAreas.get(hoveredIndex);
			Style style = hoveredArea.style();
			if (!isLinkStyle(style)) {
				return null;
			}

			Rect2i area = getArea(activeAreas, hoveredIndex, renderState.pose, renderState.scissor);
			return new HoveredText(style, area);
		}

		private static List<ActiveArea> getActiveAreas(GuiTextRenderState renderState) {
			List<ActiveArea> activeAreas = new ArrayList<>();
			Font.PreparedText preparedText = renderState.ensurePrepared();
			preparedText.visit(new Font.GlyphVisitor() {
				@Override
				public void acceptGlyph(TextRenderable.Styled glyph) {
					activeAreas.add(glyph);
				}

				@Override
				public void acceptEmptyArea(EmptyArea empty) {
					activeAreas.add(empty);
				}
			});
			return activeAreas;
		}

		private static int getHoveredIndex(List<ActiveArea> activeAreas, float mouseX, float mouseY) {
			for (int i = 0; i < activeAreas.size(); i++) {
				ActiveArea activeArea = activeAreas.get(i);
				if (ActiveTextCollector.isPointInRectangle(
					mouseX,
					mouseY,
					activeArea.activeLeft(),
					activeArea.activeTop(),
					activeArea.activeRight(),
					activeArea.activeBottom()
				)) {
					return i;
				}
			}
			return -1;
		}

		private static boolean isLinkStyle(Style style) {
			return style.getClickEvent() != null ||
				style.getHoverEvent() != null;
		}

		private static Rect2i getArea(
			List<ActiveArea> activeAreas,
			int hoveredIndex,
			Matrix3x2fc pose,
			@Nullable ScreenRectangle scissor
		) {
			Style style = activeAreas.get(hoveredIndex).style();
			float left = Float.MAX_VALUE;
			float top = Float.MAX_VALUE;
			float right = -Float.MAX_VALUE;
			float bottom = -Float.MAX_VALUE;
			for (int i = hoveredIndex; i >= 0 && activeAreas.get(i).style().equals(style); i--) {
				ActiveArea activeArea = activeAreas.get(i);
				left = Math.min(left, activeArea.activeLeft());
				top = Math.min(top, activeArea.activeTop());
				right = Math.max(right, activeArea.activeRight());
				bottom = Math.max(bottom, activeArea.activeBottom());
			}
			for (int i = hoveredIndex + 1; i < activeAreas.size() && activeAreas.get(i).style().equals(style); i++) {
				ActiveArea activeArea = activeAreas.get(i);
				left = Math.min(left, activeArea.activeLeft());
				top = Math.min(top, activeArea.activeTop());
				right = Math.max(right, activeArea.activeRight());
				bottom = Math.max(bottom, activeArea.activeBottom());
			}

			ScreenRectangle area = toScreenRectangle(left, top, right, bottom)
				.transformMaxBounds(pose);
			if (scissor != null) {
				ScreenRectangle intersection = scissor.intersection(area);
				if (intersection == null) {
					area = ScreenRectangle.empty();
				} else {
					area = intersection;
				}
			}
			return new Rect2i(area.left(), area.top(), area.width(), area.height());
		}

		private static ScreenRectangle toScreenRectangle(float left, float top, float right, float bottom) {
			int x = Mth.floor(left);
			int y = Mth.floor(top);
			int width = Mth.ceil(right - left);
			int height = Mth.ceil(bottom - top);
			return new ScreenRectangle(x, y, width, height);
		}
	}
}
