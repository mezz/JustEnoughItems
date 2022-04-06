package mezz.jei.test.util;

import mezz.jei.core.util.SubString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SubStringTest {
	@Test
	public void testEmptyString() {
		SubString subString = new SubString("");
		Assertions.assertTrue(subString.isEmpty());
	}

	@Test
	public void testEmptyBySubStringOffset() {
		SubString subString = new SubString("a", 1);
		Assertions.assertTrue(subString.isEmpty());
	}

	@Test
	public void testEmptyBySubStringLength() {
		SubString subString = new SubString("a", 0, 0);
		Assertions.assertTrue(subString.isEmpty());
	}

	@Test
	public void testCharAt() {
		String string = "abcdefg";
		SubString subString = new SubString(string);
		for (int i = 0; i < string.length(); i++) {
			Assertions.assertEquals(string.charAt(i), subString.charAt(i));
		}
	}

	@Test
	public void testRegionMatchesSameString() {
		String string = "abcdefg";
		SubString subString = new SubString(string);
		for (int start = 0; start < string.length(); start++) {
			for (int end = string.length() - start; end >= 0; end--) {
				Assertions.assertTrue(subString.regionMatches(start, string, start, end));
			}
		}
	}

	@Test
	public void testRegionMatchesWithZeroLength() {
		String string = "abcdefg";

		// make sure we follow the same rules as regular strings
		Assertions.assertTrue(string.regionMatches(0, "", 0, 0));
		Assertions.assertTrue("".regionMatches(0, string, 0, 0));

		SubString subString = new SubString(string);
		for (int start = 0; start < string.length(); start++) {
			Assertions.assertTrue(subString.regionMatches(start, string, start, 0));
			Assertions.assertTrue(subString.regionMatches(start, "", 0, 0));
		}

		SubString emptySubString = new SubString(string, 0, 0);
		for (int start = 0; start < string.length(); start++) {
			Assertions.assertTrue(emptySubString.regionMatches(start, string, start, 0));
		}
	}

	@Test
	public void testRegionMatchesSameSubString() {
		String string = "abcdefg";
		SubString subString = new SubString(string);
		SubString copy = new SubString(subString);
		for (int end = string.length(); end >= 0; end--) {
			Assertions.assertTrue(subString.regionMatches(copy, end));
		}
	}

	@Test
	public void testRegionMatchesDifferentString() {
		String string = "abcdefg";
		SubString subString = new SubString("123abcdefg456");
		for (int start = 0; start < string.length(); start++) {
			for (int end = string.length() - start; end >= 0; end--) {
				Assertions.assertTrue(subString.regionMatches(start + 3, string, start, end));
			}
		}
	}

	@Test
	public void testRegionMatchesWithOffset() {
		String string = "abcdefg";
		SubString subString = new SubString("123abcdefg", 3);
		for (int start = 0; start < string.length(); start++) {
			for (int end = string.length() - start; end >= 0; end--) {
				Assertions.assertTrue(subString.regionMatches(start, string, start, end));
			}
		}
	}

	@Test
	public void testRegionMatchesFails() {
		String string = "abcdefg";
		SubString subString = new SubString("abcdef3");
		Assertions.assertFalse(subString.regionMatches(0, string, 0, string.length()));
	}

	@Test
	public void testSubstringOffset() {
		String string = "abcdefg";
		SubString subString = new SubString(string);
		for (int offsetAmount = 0; offsetAmount < string.length(); offsetAmount++) {
			SubString offsetString = subString.substring(offsetAmount);
			Assertions.assertEquals(offsetString.length(), subString.length() - offsetAmount);

			for (int i = 0; i < offsetString.length(); i++) {
				Assertions.assertEquals(string.charAt(i + offsetAmount), offsetString.charAt(i));
			}
		}
	}

	@Test
	public void testInvalidSubstringOffset() {
		String string = "abcdefg";
		SubString subString = new SubString(string);
		Assertions.assertThrows(AssertionError.class, () -> subString.substring(string.length() + 1));
	}

	@Test
	public void testAppend() {
		String string = "abcdefg";
		SubString subString = new SubString(string, 0, 0);
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			subString = subString.append(c);
			Assertions.assertEquals(c, subString.charAt(i));
		}
	}

	@Test
	public void testInvalidAppend() {
		SubString subString = new SubString("abc");
		Assertions.assertThrows(AssertionError.class, () -> subString.append('c'));
	}

	@Test
	public void testShorten() {
		String string = "abc";
		SubString subString = new SubString(string);
		Assertions.assertEquals(3, subString.length());
		Assertions.assertTrue(subString.regionMatches(0, string, 0, 3));

		subString = subString.shorten(1);
		Assertions.assertEquals(2, subString.length());
		Assertions.assertTrue(subString.regionMatches(0, string, 0, 2));

		subString = subString.shorten(2);
		Assertions.assertTrue(subString.isEmpty());
	}

	@Test
	public void testIsPrefixAndStartsWith() {
		List<SubString> subStrings = List.of(
			new SubString("abcdefg"),
			new SubString("1abcdefg", 1),
			new SubString("1abcdefg2", 1, 7),
			new SubString("abcdefg12", 0, 7)
		);
		List<SubString> validPrefixes = List.of(
			new SubString("abcd123", 0, 4),
			new SubString("abcd"),
			new SubString("abcdefg"),
			new SubString("1abcd23", 1, 4)
		);
		for (SubString subString : subStrings) {
			for (SubString validPrefix : validPrefixes) {
				Assertions.assertTrue(subString.startsWith(validPrefix), subString + "\n" + validPrefix);
				Assertions.assertTrue(validPrefix.isPrefix(subString), subString + "\n" + validPrefix);
			}
		}
	}

	@Test
	public void testNotIsPrefixAndStartsWith() {
		List<SubString> subStrings = List.of(
			new SubString("abcdefg"),
			new SubString("1abcdefg", 1),
			new SubString("1abcdefg2", 1, 7),
			new SubString("abcdefg12", 0, 7)
		);
		List<SubString> invalidPrefixes = List.of(
			new SubString("abcdefgh"),
			new SubString("abcd123", 0, 5),
			new SubString("abcdf"),
			new SubString("1abcd23", 0, 4)
		);
		for (SubString subString : subStrings) {
			for (SubString invalidPrefix : invalidPrefixes) {
				Assertions.assertFalse(subString.startsWith(invalidPrefix), subString + "\n" + invalidPrefix);
				Assertions.assertFalse(invalidPrefix.isPrefix(subString), subString + "\n" + invalidPrefix);
			}
		}
	}

	@Test
	public void testEmptyPrefix() {
		// make sure we follow the same rules as regular strings
		//noinspection ConstantConditions
		Assertions.assertTrue("abcdefg".startsWith(""));
		//noinspection ConstantConditions,MismatchedStringCase
		Assertions.assertFalse("".startsWith("abcdefg"));

		List<SubString> subStrings = List.of(
			new SubString("abcdefg"),
			new SubString("1abcdefg", 1),
			new SubString("1abcdefg2", 1, 7),
			new SubString("abcdefg12", 0, 7)
		);
		List<SubString> emptyPrefixes = List.of(
			new SubString(""),
			new SubString("abcd", 0, 0),
			new SubString("abcd", 1, 0)
		);
		for (SubString subString : subStrings){
			for (SubString emptyPrefix : emptyPrefixes) {
				Assertions.assertTrue(subString.startsWith(emptyPrefix), subString + "\n" + emptyPrefix);
				Assertions.assertFalse(emptyPrefix.startsWith(subString), subString + "\n" + emptyPrefix);

				Assertions.assertFalse(subString.isPrefix(emptyPrefix), subString + "\n" + emptyPrefix);
				Assertions.assertTrue(emptyPrefix.isPrefix(subString), subString + "\n" + emptyPrefix);
			}
		}
	}
}
