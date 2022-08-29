package newconsole.ui;

import arc.func.Cons;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.Drawable;
import arc.scene.ui.TextArea;
import arc.struct.Seq;
import arc.util.*;
import mindustry.graphics.Pal;
import newconsole.game.ConsoleSettings;
import java.util.regex.*;

import static newconsole.ui.JsCodeArea.SymbolKind.*;

/*
 * A text area somewhat assisting in writing JS scripts.
 */
public class JsCodeArea extends TextArea {
	protected FontCache cache;
	protected String[] lines;

	public boolean syntaxHighlighting = true;

	protected int tabSize = 4;
	protected String tab = "    ";
	protected int lastLines = 0;
	protected int cacheLength = 0;
	protected String oldText;

	public static Seq<String>
		keywords = Seq.with("function", "class", "const", "let", "var", "delete", "in", "of"),
		statements = Seq.with("if", "else", "do", "while", "for", "switch", "return", "break", "continue"),
		literals = Seq.with("this", "this$super", "super", "true", "false");

	public static Color
		baseColor = Color.valueOf("f8f8f2"), // darcula base
		literalColor = Color.valueOf("bd93f9"), // darcula purple
		keywordColor = Color.valueOf("ff79c6"), // darcula pink
		specialColor = Color.valueOf("ccfcff"),
		classColor = Color.valueOf("8be9fd"), // darcula cyan
		stringColor = Color.valueOf("f1fa8c"), // darcula yellow
		commentColor = Color.valueOf("6272a4"); // darcula comment
	
	public JsCodeArea(String text) {
		super(text);
	}

	public JsCodeArea(String text, TextFieldStyle style) {
		super(text, style);
	}

	public void insertAfterCursor(CharSequence newText) {
		insertAt(cursor, newText);
		cursor -= newText.length();
	}

	public void insertAtCursor(CharSequence newText) {
		insertAt(cursor, newText);
	}

	public void insertAt(int pos, CharSequence newText) {
		text = text.substring(0, pos) + newText + text.substring(pos);
		if (pos <= cursor) cursor += newText.length();
		updateDisplayText();
	}

	public void changed(Cons<String> listener) {
		changed(() -> listener.get(getText()));
	}

	@Override
	protected InputListener createInputListener() {
		return new AssistingInputListener();
	}

	@Override
	public void paste(String content, boolean fireChangeEvent) {
		super.paste(content.replace("\t", tab == null ? "    " : tab), fireChangeEvent);
	}

	@Override
	public void act(float delta) {
		super.act(delta);

		// if the cursor is outside the visible area, scroll the area. 
		// todo: useless since i discovered that i just needed to invalidate hierarchy to update the height
		if (getLines() > linesShowing) {
			if (cursorLine < firstLineShowing) {
				moveCursorLine(Math.max(cursorLine - 1, 0));
			} else if (cursorLine > firstLineShowing + linesShowing) {
				try {
					moveCursorLine(Math.min(cursorLine + 1, getLines() - 2));
				} catch (Exception e) {}
			}
		}
		
		// invalidate hierarchy if the count of lines has changed.
		if (getLines() != lastLines) {
			invalidateHierarchy();
			lastLines = getLines();
		}
	}

	@Override
	protected void calculateOffsets() {
		try {
			super.calculateOffsets();

			if (text != oldText) {
				cacheLength = 0;

				lines = new String[linesBreak.size / 2];
				for (var l = 0; l < linesBreak.size; l += 2) {
					var lastLine = l + 2 >= linesBreak.size; // last char must be included.

					var begin = linesBreak.items[l];
					var end = lastLine ? text.length() : Math.min(linesBreak.items[l + 1] + 1, text.length());
					var line = text.substring(begin, end);
					lines[l / 2] = line;

					cacheLength += line.length() - (line.endsWith("\n") ? 1 : 0);
				}
				if (!text.endsWith("\n")) cacheLength += 1; // i don't know.
				oldText = text;

				updateSyntaxHighlighting();
			}
		} catch (Exception e) {
//			Log.err("failed to calculate offsets", e);
		}
	}

	@Override
	protected void updateDisplayText() {
		super.updateDisplayText();

		layout.setText(style.font, displayText);
		if (cache == null) cache = new FontCache(style.font);
		cache.setText(layout, 0, 0);

		// GlyphLayout ignores empty lines, so we replace them with zero-width spaces.
		// Their positions will be invalid, but we only need glyph positions for line wraps, so it doesn't matter...
		// Man, I want to laugh as a mad vilian while writing this
		layout.setText(style.font, displayText.toString().replaceAll("(\n(?=\n)|^\n|\n$)", "\u200b"));	

		glyphPositions.clear();
		float x = 0f;
		float lastAdv = style.font.getData().getGlyph(' ').width;

		for (var r = 0; r < layout.runs.size; r++) {
			var run = layout.runs.get(r);
			if (run.xAdvances.size < 1) return;

			x = run.xAdvances.first();
			for (int i = 1; i < run.xAdvances.size - 1; i++) {
				glyphPositions.add(x);
				x += (lastAdv = run.xAdvances.get(i));
			}
			glyphPositions.add(x); // add the skipped one
			glyphPositions.add(x += lastAdv); // add the last one by duplicating the last advance
		};
		glyphPositions.add(x + lastAdv); // h
	}

	public void updateSyntaxHighlighting() {
		if (syntaxHighlighting && ConsoleSettings.syntaxHighlighting()) try {
			var symbolb = new StringBuilder();
			var pos = 0;
			var posOffset = 0;
			while (pos < text.length()) {
				symbolb.setLength(0);

				var c = text.charAt(pos);
				var begin = pos;
				SymbolKind kind = null;

				symbolb.append(c);
				if (Character.isJavaIdentifierStart(c)) {
					// IDENTIFIER
					kind = IDENTIFIER;
					while (++pos < text.length() && Character.isJavaIdentifierPart(c = text.charAt(pos))) {
						symbolb.append(c);
					}
				} else if (Character.isDigit(c)) {
					// NUMBER
					kind = NUMBER;
					var hadDot = false;
					while (++pos < text.length() && (Character.isDigit(c = text.charAt(pos)) || (c == '.' && !hadDot && (hadDot = true)))) {
						symbolb.append(c);
					}
				} else if (Character.isWhitespace(c)) {
					pos++; // can't highlight
					if (c == '\n' || c == '\r') posOffset -= 1; // fucking font cache doesn't count line breaks as glyphs.
					continue;
				} else if (c == '"' || c == '\'' || c == '`') {
					// STRING
					kind = STRING;
					var beginChar = c;
					var prevChar = beginChar;
					while (++pos < text.length() && ((c = text.charAt(pos)) != beginChar || prevChar == '\\')) {
						if (c == '\n' || c == '\r') {
							pos--;
							break;
						}
						prevChar = prevChar == '\\' ? beginChar : c; // double backward slash is a literal backward slash, not double escape
						symbolb.append(c);
					}
					pos++; // skip the closing one
				} else if (pos < text.length() - 1 && c == '/' && text.charAt(pos + 1) == '/') {
					// COMMENT
					kind = COMMENT;
					while (++pos < text.length() && (c = text.charAt(pos)) != '\n' && c != '\r') {
						symbolb.append(c);
					}
				} else if (pos < text.length() - 1 && c == '/' && text.charAt(pos + 1) == '*') {
					// MULTILINE_COMMENT
					kind = COMMENT;
					var prevChar = '/';
					while (++pos < text.length() && ((c = text.charAt(pos)) != '/' || prevChar != '*')) {
						if (c == '\n' || c == '\r') {
							posOffset--;
							begin++; // workaround - decreasing posOffset also decreases begin
						}
						symbolb.append(prevChar = c);
					}
					pos++;
				} else if (isLeftBracket(c) || isRightBracket(c)) {
					// BRACKET
					kind = BRACKET;
					pos++;
				} else if (isOperator(c)) {
					// OPERATOR
					kind = OPERATOR;
					while (++pos < text.length() && isOperator(c = text.charAt(pos))) {
						symbolb.append(c);
					}
				} else {
					pos++; // need to increment the position to avoid an infinite loop
				}
				if (kind == null) kind = OTHER;
				highlightSymbol(symbolb.toString(), kind, begin + posOffset, Math.min(pos + posOffset, cacheLength)); // jit should optimise this heavily
			}
		} catch (Exception e) {
//			Log.err("failed to update syntax highlighting", e);
		}
	}

	/** Highlights the providen symbol in the text cache. */
	protected void highlightSymbol(String symbol, SymbolKind kind, int start, int end) {
		if (symbol.length() == 0) return;

		cache.setColors(switch (kind) {
			case IDENTIFIER -> {
				if (keywords.contains(symbol)) yield keywordColor;
				if (statements.contains(symbol)) yield specialColor;
				if (literals.contains(symbol)) yield literalColor;
				if (Character.isUpperCase(symbol.charAt(0))) yield classColor;
				yield baseColor;
			}
			case NUMBER -> literalColor;
			case COMMENT -> commentColor;
			case STRING -> stringColor;
			case BRACKET, OPERATOR -> specialColor;
			default -> baseColor;
		}, start, end);
	}

	protected static boolean isOperator(char c) {
		return c == '=' || c == '<' || c == '>' // comparison
			|| c == '+' || c == '-' || c == '*' || c == '/' || c == '%' // arithmetics
			|| c == '&' || c == '|' || c == '^' || c == '!' // boolean logic
			|| c == '.'; // dot-qualified access
	}

	protected static boolean isLeftBracket(char c) {
		return c == '(' || c == '[' || c == '{';
	}

	protected static boolean isRightBracket(char c) {
		return c == ')' || c == ']' || c == '}';
	}
	
	/** Returns 0 if there's none. */
	protected static char getPairedCharacter(char c) {
		return switch (c) {
			case '(' -> ')';
			case '[' -> ']';
			case '{' -> '}';
			case '"', '\'', '`' -> c;
			default -> 0;
		};
	}
	
	protected static boolean isPairedCharacter(char c) {
		return getPairedCharacter(c) != 0;
	}

	/** Gets the first non-space character in the text before or after the specified position, or 0 if it doesn't exist. */
	public char getNotSpace(int position, boolean before) {
		var inc = before ? -1 : 1;
		for (; position >= 0 && position < text.length(); position += inc) {
			if (text.charAt(position) != ' ') return text.charAt(position);
		}

		return 0;
	}

	public void setTabSize(int spaces) {
		tabSize = spaces;

		var sb = new StringBuilder();
		for (var i = 0; i < spaces; i++) sb.append(' ');
		tab = sb.toString();
	}

	@Override
	public void setText(String str) {
		super.setText(str.replace("\t", tab == null ? "    " : tab));
	}

	@Override
	public float getPrefHeight() {
		return textHeight * getLines();
	}

	@Override
	public void layout() {
		oldText = null;
		super.layout();
	}

	@Override
	protected void drawText(Font font, float x, float y) {
		try {
			var data = style.font.getData();
			var space = data.getGlyph(' ');
			var indentGuides = ConsoleSettings.indentationGuides();
				
			Draw.color(Pal.lightishGray);
			Lines.stroke(1f);

			var pos = 0; // position in TextCache - doesn't include line breaks
			var charPos = 0; // position in text - includes line breaks
			// line wrap offsets
			var textOffX = 0f;
			var textOffY = 0f;
			var l = 0; // line index, including wrapped lines
			var wrapl = 0; // if the current line is wrapped, stores which wrap the current line represents
			var lastGuide = 0; // last indentation guide level

			// rare case - code starts with exactly 1 newline, the glyph layout and the fixing regex ignore it, everything goes wrong.
			if (text.startsWith("\n") && !text.startsWith("\n\n")) charPos--;

			for (var line : lines) {
				var lineLength = line.length() - 1;
				var wrappedLine = !line.endsWith("\n");
				
				if (l >= firstLineShowing || l <= firstLineShowing + linesShowing) {
					if (indentGuides && wrapl == 0) {
						// render indentation guides
						var c = -1;
						while (++c < line.length() && line.charAt(c) == ' ') {}

						if (c >= line.length() - (wrappedLine ? 0 : 1)) {
							// nothing here, inherit the previous indentation guide level, as that looks better
							c = lastGuide;
						} else {
							lastGuide = c;
						}

						for (var i = 1; i <= c; i++) {
							if (i % 4 != 0) continue;

							var offX = x + ((i - 1 + tabSize - 1) / tabSize) * space.width * tabSize;
							var offY = y - (l - firstLineShowing) * style.font.getLineHeight() + style.font.getAscent();

							Lines.line(offX, offY, offX, offY - style.font.getLineHeight());
						}
					}

					// render the line
					var begin = Math.min(pos, cacheLength);
					var end = Math.min(pos + lineLength, cacheLength)
						+ (l == lines.length - 1 && !line.endsWith("\n") ? 1 : 0); // I just don't care anymore, if it works, I'm fine

					if (end > begin) {
						cache.setPosition(x + textOffX, y + firstLineShowing * style.font.getLineHeight() + textOffY);
						cache.draw(begin, end);
					}

					if (wrappedLine) {
						// if there's no linefeed at the end, the line was wrapped - this needs to be handled manually
						textOffX = -glyphPositions.items[Math.min(charPos + lineLength, glyphPositions.size - 1)];
						textOffY -= style.font.getLineHeight();
						wrapl++;
					} else {
						wrapl = 0;
						textOffX = 0;
					}
				}
				pos += lineLength;
				charPos += lineLength + (wrappedLine ? 0 : 1);
				l++;
			}
		} catch (Exception e) {
//			Log.err("failed to render the code area", e); // this has got called thousands of times during development. seriously.
		}
	}

	@Override
	protected void drawSelection(Drawable selection, Font font, float x, float y) { 
		int i = firstLineShowing * 2;
		float offsetY = 0;
		int minIndex = Math.min(cursor, selectionStart);
		int maxIndex = Math.max(cursor, selectionStart);
		while (i + 1 < linesBreak.size && i < (firstLineShowing + linesShowing) * 2) {

			int lineStart = linesBreak.items[i];
			int lineEnd = linesBreak.items[i + 1];

			if (!((minIndex < lineStart && minIndex < lineEnd && maxIndex < lineStart && maxIndex < lineEnd)
				|| (minIndex > lineStart && minIndex > lineEnd && maxIndex > lineStart && maxIndex > lineEnd))) {

				int start = Math.min(Math.max(linesBreak.get(i), minIndex), glyphPositions.size - 1);
				int end = Math.min(Math.min(linesBreak.get(i + 1), maxIndex), glyphPositions.size - 1);

				float selectionX = glyphPositions.get(start) - glyphPositions.get(Math.min(linesBreak.get(i), glyphPositions.size));
				float selectionWidth = glyphPositions.get(end) - glyphPositions.get(start);

				selection.draw(x + selectionX + fontOffset, y - textHeight - font.getDescent() - offsetY, selectionWidth,
					font.getLineHeight());
			}

			offsetY += font.getLineHeight();
			i += 2;
		}
	}

	class AssistingInputListener extends TextAreaListener {
		@Override
		public boolean keyTyped(InputEvent event, char character) {
			var indentAssistance = ConsoleSettings.indentationAssistance();
			var pairedChars = ConsoleSettings.characterPairs();

			if (character == '\t') {
				insertAtCursor(tab);
				return true;
			} else if (indentAssistance && character == '\n') {
				var oldText = text;
				var oldCursor = cursor;

				var left = getNotSpace(cursor - 1, true);
				var right = getNotSpace(cursor, false);

				if (super.keyTyped(event, character)) {
					// determine where the old line begins - lineBreak is invalid at tnis point
					var i = Math.max(oldCursor - 1, 0);
					while (i > 0 && (i >= oldText.length() || oldText.charAt(i) != '\n')) {
						i--;
					}
					if (i < oldText.length() && oldText.charAt(i) == '\n') i++;
					// determine how many spaces the previous line has had
					var leadingSpace = new StringBuilder();
					while (i < oldText.length() && oldText.charAt(i++) == ' ')
						leadingSpace.append(" ");

					// if the cursor was surrounded by two matching brackets, add another line after the cursor and increment indentation
					if (isLeftBracket(left) && getPairedCharacter(left) == right) {
						insertAfterCursor(leadingSpace);
						insertAfterCursor("\n");
						leadingSpace.append(tab);
					}

					// if the last char was a closing bracket, decrement indentation
					if (cursor > 0 && isRightBracket(left)) {
						for (var j = 0; j < tabSize && leadingSpace.length() > 0; j++) {
							leadingSpace.deleteCharAt(leadingSpace.length() - 1);
						}
					}

					// insert the same amount of spaces
					insertAtCursor(leadingSpace);
				}
				return true;
			} else if (
				pairedChars && isPairedCharacter(character)
				&& (cursor >= text.length() || Character.isWhitespace(text.charAt(cursor)) || isRightBracket(text.charAt(cursor)))
				&& super.keyTyped(event, character
			)) {
				insertAfterCursor(String.valueOf(getPairedCharacter(character)));
				return true;
			} else if (pairedChars && cursor < text.length() && isPairedCharacter(character) && !isLeftBracket(character) && character == text.charAt(cursor)) {
				cursor++;
				return true;
			}

			return super.keyTyped(event, character);
		}
	}

	public enum SymbolKind {
		IDENTIFIER,
		NUMBER,
		STRING,
		COMMENT,
		BRACKET,
		OPERATOR,
		OTHER
	}
}

