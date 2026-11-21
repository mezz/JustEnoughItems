package mezz.jei.test;

import mezz.jei.common.chat.JeiChatItemLinks;
import mezz.jei.common.chat.JeiChatItemLinks.IngredientLink;
import net.minecraft.network.chat.ClickEvent;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JeiChatItemLinksTest {
	@ParameterizedTest
	@ValueSource(strings = {"plain", "bracket]", "%5D", "plus+space ", "semi;colon", "雪🌿", "[JEI:v1:nested;marker]"})
	void escapedLinksSurviveChatFramingAndClicks(String uid) {
		IngredientLink link = new IngredientLink("test:ingredient", uid);
		String argument = JeiChatItemLinks.createCommandArgument(link);
		assertEquals(link, JeiChatItemLinks.parseCommandArgument(argument).orElseThrow());

		String marker = "[JEI:" + argument + "]";
		var parsed = JeiChatItemLinks.parse("before " + marker + " after", candidate -> {
			assertEquals(link, candidate);
			return Optional.of("Shown");
		});
		assertEquals("before [Shown] after", parsed.getString());
		var command = assertInstanceOf(ClickEvent.RunCommand.class, parsed.getSiblings().get(1).getStyle().getClickEvent());
		assertEquals(link, JeiChatItemLinks.parseShowRecipeCommand(command.command()).orElseThrow());
	}

	@ParameterizedTest
	@ValueSource(strings = {"plain", "%5D", "+", "literal%20space", "雪", "(nested,value)"})
	void legacyLinksKeepLiteralUidText(String uid) {
		IngredientLink link = new IngredientLink("test:ingredient", uid);
		String argument = "v1:test:ingredient;" + uid;
		assertEquals(link, JeiChatItemLinks.parseCommandArgument(argument).orElseThrow());
		var parsed = JeiChatItemLinks.parse("[JEI:" + argument + "]", candidate -> {
			assertEquals(link, candidate);
			return Optional.of("Legacy");
		});
		assertEquals("[Legacy]", parsed.getString());
	}

	@ParameterizedTest
	@ValueSource(strings = {"v2:test%ZZ;uid", "v2:test;%", "v2:test;%5", "v2:bad+type;uid", "v2:;uid", "v2:test;", "v3:test;uid"})
	void rejectsMalformedOrUnsupportedArguments(String argument) {
		assertTrue(JeiChatItemLinks.parseCommandArgument(argument).isEmpty());
	}
}
