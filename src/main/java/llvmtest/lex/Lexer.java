package magnum.lex;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Lexer {

	private static final Escaper STRING_ESCAPER = Escapers.builder()
			.addEscape('\n', "\\n")
			.addEscape('\r', "\\r").build();

	public static List<LexToken> lex(String input) {
		var tokens = new ArrayList<LexToken>();
		if (input.length() < 2) {
			return tokens;
		}
		var chars = input.toCharArray();

		Optional<LexToken> token;
		int base = 0;
		while (true) {
			token = next(chars, base);
			if (token.isEmpty()) {
				return tokens;
			}
			var t = token.get();
			tokens.add(t);
			base += t.string.length();
		}
	}

	public static Optional<LexToken> next(final char[] input, final int base) {
		if (base >= input.length) {
			return Optional.empty();
		}

		var type = LexTokenType.NONE;
		var firstchar = input[base];
		System.out.println("lex.next type " + type.name() + " char '" + STRING_ESCAPER.escape(String.valueOf(firstchar)) + "'\t\t(code " + ((int) firstchar) + ")");
		if (firstchar == '\n') {
			return Optional.of(new LexToken(LexTokenType.NEWLINE, "\n"));
		}
		if (firstchar == '\r') {
			if (input[base + 1] != '\n') {
				throw new RuntimeException("\\r not followed by \\n");
			}
			return Optional.of(new LexToken(LexTokenType.NEWLINE, "\r\n"));
		}
		if (isSpace(firstchar)) {
			return Optional.of(new LexToken(LexTokenType.SPACE, " "));
		}
		if (firstchar == '\t') {
			return Optional.of(new LexToken(LexTokenType.SPACE, "\t"));
		}
		if (isSymbol(firstchar)) {
			return Optional.of(new LexToken(LexTokenType.SYMBOL, Character.toString(firstchar)));
		}
		if (isAlphabetical(firstchar)) {
			type = LexTokenType.WORD;
		}

		int lookahead = 1;
		for (;;) {
			if (base + lookahead >= input.length) {
				return Optional.of(new LexToken(type, mkstr(input, base, lookahead)));
			}

			char c = input[base + lookahead];
			if (isDelimeter(type, c)) {
				//System.out.println("encountered delimeter '" + c + "' for " + type.name());
				return Optional.of(new LexToken(type, mkstr(input, base, lookahead)));
			}
			lookahead++;
		}
	}

	private static String mkstr(final char[] chars, final int idx, final int len) {
		var strbuf = new StringBuilder();
		for (int i = idx; i < idx + len; i++) {
			strbuf.append(chars[i]);
		}
		return strbuf.toString();
	}

	public static char[] SYMBOLS = new char[] { '(', ')', '{', '}' };
	public static boolean isSymbol(char c) {
		for (int i = 0; i < SYMBOLS.length; i++) {
			if (c == SYMBOLS[i]) {
				return true;
			}
		}
		return false;
	}

	public static boolean isDelimeter(LexTokenType ctx, char c) {
		if (ctx == LexTokenType.WORD) {
			return !isAlphabetical(c);
		}
		throw new RuntimeException("unhandled delimeter check for type " + ctx.name());
	}

	public static boolean isSpace(char c) {
		return c == ' ';
	}

	public static boolean isAlphabetical(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	public static boolean expected(LexTokenType token, char c) {
		if (token == LexTokenType.NONE) {
			return true;
		}
		if (token == LexTokenType.SPACE) {
			return true;
		}
		if (token == LexTokenType.WORD) {
			return isAlphabetical(c);
		}
		return false;
	}

}
