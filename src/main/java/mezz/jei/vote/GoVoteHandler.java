package mezz.jei.vote;

import com.mojang.blaze3d.matrix.MatrixStack;
import mezz.jei.api.constants.ModIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * This class is derived from Botania and released for public use via the Waive Clause of the Botania License.<br />
 * You are encouraged to copy and use it. Keep the marker file path the same so multiple mods don't show the screen
 * at once.<br />
 * If you are uncomfortable with the network access to ip-api, feel free to remove it. The fallback is to examine the
 * computer's current locale.<br />
 * <br />
 * Quick Usage Guide:
 * <li>Copy to your mod</li>
 * <li>Call {@link #init} from {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent}</li>
 * <li>Replace {@link #BRAND} with your mod or group name.</li>
 */
@EventBusSubscriber(modid = ModIds.JEI_ID, value = Dist.CLIENT)
public class GoVoteHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String BRAND = "mezz's mods";
	private static final String MARKER_PATH = ".vote2020_marker";
	private static final LocalDate ELECTION_DAY = LocalDate.of(2020, Month.NOVEMBER, 3);
	public static final String VOTE_ORG_LINK = "https://vote.org/";
	private static final int HEADER_COLOR = 0xFFD0163E; // matches vote.org header
	private static final int BG_COLOR = 0xFF205493; // matches vote.org body
	private static boolean shownThisSession = false;
	private static final List<String> commonUsaLanguages = Arrays.asList(
		Locale.ENGLISH.getLanguage(),
		"es",
		Locale.SIMPLIFIED_CHINESE.getLanguage(),
		"tl",
		"vi",
		Locale.FRENCH.getLanguage()
	);

	private static boolean markerAlreadyExists = false;

	public static void init() {
		if (isAfterElectionDay()) {
			return;
		}

		try {
			Path path = Paths.get(MARKER_PATH);
			Files.createFile(path);
			Files.setAttribute(path, "dos:hidden", true);
		} catch (FileAlreadyExistsException ex) {
			LOGGER.debug("Go vote handler: Marker already exists");
			markerAlreadyExists = true;
		} catch (IOException e) {
			LOGGER.warn("IO exception when trying to create marker", e);
		}
	}

	public static boolean isAfterElectionDay() {
		return LocalDate.now().isAfter(ELECTION_DAY);
	}

	public static boolean isProbablyUsaLocale() {
		Locale locale = Locale.getDefault();
		if (locale == null) {
			return false;
		}

		String usaCountry = Locale.US.getCountry();
		if (!usaCountry.equals(locale.getCountry())) {
			return false;
		}

		return commonUsaLanguages.stream()
			.anyMatch(language -> language.equals(locale.getLanguage()));
	}

	@SubscribeEvent
	public static void clientTick(GuiOpenEvent event) {
		Screen curr = event.getGui();
		if ((curr instanceof WorldSelectionScreen || curr instanceof MultiplayerScreen) && shouldShow()) {
			event.setGui(new GoVoteScreen(curr));
			shownThisSession = true;
		}
	}

	private static boolean shouldShow() {
		return !shownThisSession && !isAfterElectionDay() && !markerAlreadyExists && isProbablyUsaLocale();
	}

	public static void displayOpenLinkScreen(String url, Minecraft minecraft, Screen currentScreen) {
		minecraft.displayGuiScreen(new ConfirmOpenLinkScreen(doIt -> consume(doIt, minecraft, currentScreen, url), url, true));
	}

	private static void consume(boolean doIt, Minecraft minecraft, Screen currentScreen, String url) {
		minecraft.displayGuiScreen(currentScreen);
		if (doIt) {
			Util.getOSType().openURI(url);
		}
	}

	private static class GoVoteScreen extends Screen {
		private static final int TICKS_PER_GROUP = 50;
		private static final int LINE_SPACING = 3;
		private final Screen parent;
		private int ticksElapsed = 0;
		private final List<List<ITextComponent>> message = new ArrayList<>();
		private int ticksForFullMessage = 0;

		protected GoVoteScreen(Screen parent) {
			super(new StringTextComponent(""));
			this.parent = parent;
			addGroup(s("Please read the following message from " + BRAND + "."));
			addGroup(StringTextComponent.EMPTY, s("We are at a unique crossroads in the history of our country."));
			addGroup(StringTextComponent.EMPTY, s("In this time of heightened polarization,"),
				s("breakdown of political decorum, and fear,"));
			addGroup(s("it is tempting to succumb to apathy,"),
				s("to think that nothing you do will matter."));
			addGroup(StringTextComponent.EMPTY, s("But power is still in the hands of We, the People."));
			addGroup(s("The Constitution and its amendments guarantee every citizen the right to vote."));
			addGroup(s("And it is not only our right, but our ")
				.append(s("responsibility").mergeStyle(TextFormatting.ITALIC, TextFormatting.GOLD))
				.appendString(" to do so."));
			addGroup(s("Your vote matters. Always."));
			addGroup(
				StringTextComponent.EMPTY,
				s("If you are eligible to vote, please take 2 minutes and click anywhere"),
				s("to check your registration status and register at ")
					.append(s(VOTE_ORG_LINK).mergeStyle(TextFormatting.GOLD))
			);
			addGroup(
				StringTextComponent.EMPTY,
				s("Press ESC to exit. (This screen will not show up again.)")
			);
		}

		// Each group appears at the same time
		private void addGroup(ITextComponent... lines) {
			message.add(Arrays.asList(lines));
			ticksForFullMessage += TICKS_PER_GROUP;
		}

		private static StringTextComponent s(String txt) {
			return new StringTextComponent(txt);
		}

		@Override
		public void tick() {
			super.tick();
			ticksElapsed++;
		}

		@Override
		public void render(MatrixStack mstack, int mx, int my, float pticks) {
			super.render(mstack, mx, my, pticks);

			final int xMiddle = width / 2;
			final int yMiddle = height / 2;
			final int lineHeight = font.FONT_HEIGHT + LINE_SPACING;
			final int lineCount = message.stream().mapToInt(Collection::size).sum();
			final int totalTextHeight = lineHeight * lineCount;
			int y = yMiddle - (totalTextHeight / 2);

			final int headerHeight = y + lineHeight;

			fill(mstack, 0, 0, width, headerHeight, HEADER_COLOR);
			fill(mstack, 0, headerHeight, width, height, BG_COLOR);

			for (int groupIdx = 0; groupIdx < message.size(); groupIdx++) {
				List<ITextComponent> group = message.get(groupIdx);
				if (ticksElapsed > groupIdx * TICKS_PER_GROUP) {
					for (ITextComponent line : group) {
						//noinspection SuspiciousNameCombination
						drawCenteredString(mstack, font, line, xMiddle, y, 0xFFFFFF);
						y += lineHeight;
					}
				}
			}
		}

		private boolean isMessageComplete() {
			return ticksElapsed >= ticksForFullMessage;
		}

		@Nonnull
		@Override
		public String getNarrationMessage() {
			StringBuilder builder = new StringBuilder();
			for (List<ITextComponent> group : message) {
				for (ITextComponent line : group) {
					builder.append(line.getString());
				}
			}
			return builder.toString();
		}

		@Override
		public boolean keyPressed(int keycode, int scanCode, int modifiers) {
			if (!isMessageComplete()) {
				return false;
			}
			if (keycode == GLFW.GLFW_KEY_ESCAPE && minecraft != null) {
				minecraft.displayGuiScreen(parent);
			}

			return super.keyPressed(keycode, scanCode, modifiers);
		}

		@Override
		public boolean mouseClicked(double x, double y, int modifiers) {
			if (modifiers == 0 && minecraft != null) {
				displayOpenLinkScreen(VOTE_ORG_LINK, minecraft, this);
				return true;
			}

			return super.mouseClicked(x, y, modifiers);
		}

	}

}
