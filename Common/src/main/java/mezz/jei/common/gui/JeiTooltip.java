package mezz.jei.common.gui;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.common.Internal;
import mezz.jei.common.platform.IPlatformRenderHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JeiTooltip {
	private final List<Either<FormattedText, TooltipComponent>> list = new ArrayList<>();

	public void add(FormattedText formattedText) {
		list.add(Either.left(formattedText));
	}

	public void add(TooltipComponent component) {
		list.add(Either.right(component));
	}

	public void addKeyUsageComponent(String translationKey, IJeiKeyMapping keyMapping) {
		MutableComponent translatedKeyMessage = keyMapping.getTranslatedKeyMessage().copy();
		addKeyUsageComponent(translationKey, translatedKeyMessage);
	}

	public void addKeyUsageComponent(String translationKey, MutableComponent keyMapping) {
		Component boldKeyMapping = keyMapping.withStyle(ChatFormatting.BOLD);
		MutableComponent component = Component.translatable(translationKey, boldKeyMapping)
			.withStyle(ChatFormatting.ITALIC)
			.withStyle(ChatFormatting.GRAY);

		add(component);
	}

	public void addAll(List<Component> components) {
		for (Component component : components) {
			add(component);
		}
	}

	public void addAll(JeiTooltip tooltip) {
		list.addAll(tooltip.list);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public List<Either<FormattedText, TooltipComponent>> build() {
		return list;
	}

	@Override
	public String toString() {
		return list.stream()
			.map(e -> e.map(
				FormattedText::getString,
				Object::toString
			))
			.collect(Collectors.joining("\n", "[\n", "\n]"));
	}

	public void draw(GuiGraphics guiGraphics, int x, int y) {
		if (isEmpty()) {
			return;
		}
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
		try {
			renderHelper.renderTooltip(guiGraphics, list, x, y, font, ItemStack.EMPTY);
		} catch (RuntimeException e) {
			throw new RuntimeException("Crashed when rendering tooltip:\n" + this);
		}
	}

	public <T> void draw(GuiGraphics guiGraphics, int x, int y, ITypedIngredient<T> typedIngredient){
		IIngredientType<T> ingredientType = typedIngredient.getType();
		IIngredientManager ingredientManager = Internal.getJeiRuntime().getIngredientManager();
		IIngredientRenderer<T> ingredientRenderer = ingredientManager.getIngredientRenderer(ingredientType);
		draw(guiGraphics, x, y, typedIngredient, ingredientRenderer, ingredientManager);
	}

	public <T> void draw(
		GuiGraphics guiGraphics,
		int x,
		int y,
		ITypedIngredient<T> typedIngredient,
		IIngredientRenderer<T> ingredientRenderer,
		IIngredientManager ingredientManager
	) {
		Minecraft minecraft = Minecraft.getInstance();
		T ingredient = typedIngredient.getIngredient();
		Font font = ingredientRenderer.getFontRenderer(minecraft, ingredient);
		ItemStack itemStack = typedIngredient.getItemStack().orElse(ItemStack.EMPTY);

		itemStack.getTooltipImage()
			.ifPresent((c) -> {
				list.add(1, Either.right(c));
			});

		if (isEmpty()) {
			return;
		}
		try {
			IPlatformRenderHelper renderHelper = Services.PLATFORM.getRenderHelper();
			renderHelper.renderTooltip(guiGraphics, list, x, y, font, itemStack);
		} catch (RuntimeException e) {
			CrashReport crashReport = ErrorUtil.createIngredientCrashReport(e, "Rendering ingredient tooltip", ingredientManager, typedIngredient);
			crashReport.addCategory("tooltip")
				.setDetail("value", this);
			throw new ReportedException(crashReport);
		}
	}
}
