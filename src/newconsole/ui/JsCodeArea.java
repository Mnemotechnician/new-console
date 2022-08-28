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

	public static Seq<String>
		keywords = Seq.with("function", "class", "const", "let", "var", "delete"),
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
		// if the cursor is outside the visible area, scroll the pane.
		if (getLines() > linesShowing) {
			if (cursorLine < firstLineShowing) {
				moveCursorLine(Math.max(cursorLine - 1, 0));
			} else if (cursorLine > firstLineShowing + linesShowing) {
				try {
					moveCursorLine(Math.min(cursorLine + 1, getLines() - 2));
				} catch (Exception e) {}
			}
		}
	}

	@Override
	protected void calculateOffsets() {
		try {
			super.calculateOffsets();

			// TODO this gets called once per frame
			// not an issue in my case, but it's better to find a way not to.
			lines = new String[linesBreak.size / 2];
			for (var l = 0; l < linesBreak.size - 1; l += 2) {
				var begin = linesBreak.items[l];
				var end = Math.min(linesBreak.items[l + 1] + 1, text.length());
				lines[l / 2] = text.substring(begin, end);
			}
		} catch (Exception e) {
			Log.err("failed to calculate offsets", e);
		}
	}

	@Override
	protected void updateDisplayText() {
		super.updateDisplayText();

		layout.setText(style.font, displayText);
		if (cache == null) cache = new FontCache(style.font);
		cache.setText(layout, 0, 0);

		layout.setText(style.font, displayText.toString().replace('\n', ' ').replace('\r', ' '));
		glyphPositions.clear();
		float x = 0;
		if(layout.runs.size > 0){
			var run = layout.runs.first();
			var xAdvances = run.xAdvances;
			fontOffset = xAdvances.first();
			for(int i = 1, n = xAdvances.size; i < n; i++){
				glyphPositions.add(x);
				x += xAdvances.get(i);
			}
			glyphPositions.add(x);
		}

		// update syntax highlighting
		if (syntaxHighlighting) try {
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
					if (c == '\n' || c == '\r') posOffset -= 1; // i don't know why, i don't know anything. but it breaks elsewise
					continue;
				} else if (c == '"' || c == '\'' || c == '`') {
					// STRING
					kind = STRING;
					var beginChar = c;
					while (++pos < text.length() && (c = text.charAt(pos)) != beginChar && c != '\n' && c != '\r') {
						if (c == beginChar) break;
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
						symbolb.append(prevChar = c);
					}
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
				highlightSymbol(symbolb.toString(), kind, begin + posOffset, Math.min(pos, text.length()) + posOffset); // jit should optimise this heavily
			}
		} catch (Exception e) {
			Log.err("failed to update syntax highlighting", e);
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
			case '"', '\'' -> c;
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
	protected void drawText(Font font, float x, float y) {
		try {
			var data = style.font.getData();
			var space = data.getGlyph(' ');
			var maxWidthLine = this.getWidth() 
				- (style.background != null ? style.background.getLeftWidth() + style.background.getRightWidth() : 0);
				
			Draw.color(Pal.lightishGray);
			Lines.stroke(1f);

			var pos = 0; 
			var textOffX = 0f;
			var textOffY = 0f;
			var l = 0;
			var lastGuide = 0;

			for (var line : lines) {
				var lineLength = line.endsWith("\n") ? line.length() - 1 : line.length();
				
				if (l >= firstLineShowing || l <= firstLineShowing + linesShowing) {
					// render indentation guides
					var c = -1;
					while (++c < line.length() && line.charAt(c) == ' ') {};

					if (c >= line.length()) {
						// nothing here, inherit the previous indentation guide level, as that looks better
						c = lastGuide;
					} else {
						lastGuide = c;
					}

					for (var i = 1; i <= c; i++) {
						var offX = x + ((i - 1) / 4) * space.width * 4;
						var offY = y - (l - firstLineShowing) * style.font.getLineHeight() + style.font.getAscent();
					
						if (i % 4 == 0) Lines.line(offX, offY, offX, offY - style.font.getLineHeight());
					}

					// render the line
					cache.setPosition(x + textOffX, y + firstLineShowing * style.font.getLineHeight() + textOffY);
					cache.draw(Math.min(pos, text.length()), Math.min(pos + lineLength, text.length()));

					if (!line.endsWith("\n")) {
						// if there's no linefeed at the end, the line was wrapped - this needs to be handled manually
						textOffX = -glyphPositions.items[Math.min(pos + lineLength - 1, glyphPositions.size - 1)];
						textOffY -= style.font.getLineHeight();
					} else {
						textOffX = 0;
					}
				}
				pos += lineLength;
				l++;
			}
		} catch (Exception e) {
			Log.err("failed to render the code area", e);
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
			if (character == '\t') {
				insertAtCursor(tab);
				return true;
			} else if (character == '\n') {
				var oldText = text;
				var oldLine = cursorLine;

				var left = getNotSpace(cursor - 1, true);
				var right = getNotSpace(cursor, false);

				if (super.keyTyped(event, character)) {
					// determine how many spaces the previous line has had
					var i = linesBreak.get(oldLine * 2);
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
							leadingSpace.deleteCharAt(0);
						}
					}

					// insert the same amount of spaces
					insertAtCursor(leadingSpace);
				}
				return true;
			} else if (isPairedCharacter(character) && (cursor >= text.length() || Character.isWhitespace(text.charAt(cursor))) && super.keyTyped(event, character)) {
				insertAfterCursor(String.valueOf(getPairedCharacter(character)));
				return true;
			} else if (cursor < text.length() && isRightBracket(character) && character == text.charAt(cursor)) {
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

