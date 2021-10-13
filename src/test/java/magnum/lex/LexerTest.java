package magnum.lex;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class LexerTest {

	@Test
	public void testLex() {
		var input1 = "function name() {}";
		var output1 = Lexer.lex(input1);
		var expected1 = Lists.newArrayList(
				new LexToken(LexTokenType.WORD, "function"),
				new LexToken(LexTokenType.SPACE, " "),
				new LexToken(LexTokenType.WORD, "name"),
				new LexToken(LexTokenType.SYMBOL, "("),
				new LexToken(LexTokenType.SYMBOL, ")"),
				new LexToken(LexTokenType.SPACE, " "),
				new LexToken(LexTokenType.SYMBOL, "{"),
				new LexToken(LexTokenType.SYMBOL, "}")
		);
		assertLex(expected1, output1);
	}
	private void assertLex(List<LexToken> expected, List<LexToken> actual) {
		Assert.assertEquals(expected.toArray(new Object[0]), actual.toArray(new Object[0]));
	}

	@Test
	public void testNext() {
		// case: no input
		var input1 = new char[] { };
		var output1 = Lexer.next(input1, 0);
		Assert.assertTrue(output1.isEmpty());

		// case: space
		var input2 = new char[] { ' ' };
		var output2 = Lexer.next(input2, 0).get();
		assertToken(output2, LexTokenType.SPACE, " ");

		// case: space + offset
		var input3 = new char[] { ' ', ' ' };
		var output3 = Lexer.next(input3, 1).get();
		assertToken(output3, LexTokenType.SPACE, " ");

		// case: word
		var input4 = new char[] { 'a', 'b', 'c', 'd' };
		var output4 = Lexer.next(input4, 0).get();
		assertToken(output4, LexTokenType.WORD, "abcd");

		// case: word + space delimeter
		var input5 = new char[] { 'a', 'b', ' ', 'c', 'd' };
		var output5 = Lexer.next(input5, 0).get();
		assertToken(output5, LexTokenType.WORD, "ab");
	}

	private void assertToken(LexToken token, LexTokenType type, String str) {
		Assert.assertEquals(type, token.type);
		Assert.assertEquals(str, token.string);
	}

	@Test
	public void testIsDelimeter() {
		// case: word -> space
		Assert.assertTrue(Lexer.isDelimeter(LexTokenType.WORD, ' '));

		// case: word -> any symbol
		for (char symbol : Lexer.SYMBOLS) {
			Assert.assertTrue(Lexer.isDelimeter(LexTokenType.WORD, symbol));
		}
	}
}
