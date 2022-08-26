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
	protected String realText;
	protected String[] lines;

	public boolean syntaxHighlighting = true;

	static Seq<String> 
		keywords = Seq.with("function", "class", "const", "let", "var", "delete"),
		statements = Seq.with("if", "else", "do", "while", "for", "switch", "return", "break", "continue"),
		literals = Seq.with("this", "this$super", "super", "true", "false");

	static Color
		baseColor = Color.valueOf("f8f8f2"), // darcula base
		literalColor = Color.valueOf("bd93f9"), // darcula purple
		keywordColor = Color.valueOf("ff79c6"), // darcula pink
		specialColor = Color.valueOf("aaeeff"),
		classColor = Color.valueOf("8be9fd"), // darcula cyan
		stringColor = Color.valueOf("f1fa8c"), // darcula yellow
		commentColor = Color.valueOf("6272a4"); // darcula comment
		

	public JsCodeArea(String text) {
		super(text);
	}

	public JsCodeArea(String text, TextFieldStyle style) {
		super(text, style);
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
		super.paste(content.replace("\t", "    "), fireChangeEvent);
	}

	@Override
	protected void updateDisplayText() {
		super.updateDisplayText();

		glyphPositions.clear();
		layout.setText(style.font, displayText.toString().replace('\n', ' ').replace('\r', ' '));
		var runs = layout.runs;

		if (runs.size > 0) {
			var xAdvances = runs.first().xAdvances;
			float x = 0;
			for (int j = 1; j < xAdvances.size; j++) {
				glyphPositions.add(x);
				x += xAdvances.get(j);
			}
			glyphPositions.add(x);
		}
		
		lines = text.split("\n");

		if (cache == null) cache = new FontCache(style.font);
		cache.setText(text, 0, 0, width, Align.left, false);

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
				} else if (c == '(' || c == ')' || c == '{' || c == '}' || c == '[' || c == ']') {
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
			case BRACKET -> specialColor;
			case OPERATOR -> specialColor;
			default -> baseColor;
		}, start, end);
	}

	protected boolean isOperator(char c) {
		return c == '=' || c == '<' || c == '>' // comparison
			|| c == '+' || c == '-' || c == '*' || c == '/' || c == '%' // math
			|| c == '&' || c == '|' || c == '^' || c == '!'; // boolean logic
	}

	@Override
	public void setText(String str) {
		super.setText(str.replace("\t", "    "));
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
				
			cache.setPosition(x, y + firstLineShowing * style.font.getLineHeight());
			Draw.color(Pal.lightishGray);
			Lines.stroke(1f);

			var pos = 0; 
			var l = 0;
			for (var line : lines) {
				if (l >= firstLineShowing || l <= firstLineShowing + linesShowing) {
					// render indentation guides
					var c = 0;
					while (c < line.length() && line.charAt(c++) == ' ') {
						var offX = x + (c / 4 - 1) * space.width * 4;
						var offY = y - l * style.font.getLineHeight() + style.font.getAscent();

						if (c % 4 == 0) Lines.line(offX, offY, offX, offY - style.font.getLineHeight());
					}

					// render the line
					cache.draw(pos, Math.min(pos + line.length(), text.length()));
				}
				pos += line.length();
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

			int lineStart = linesBreak.get(i);
			int lineEnd = linesBreak.get(i + 1);

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
				insertAtCursor("    ");
				return true;
			} else if (character == '\n') {
				var oldText = text;
				var oldLine = cursorLine;

				if (super.keyTyped(event, character) && cursorLine > 0 && oldLine * 2 < linesBreak.size) {
					// determine how many spaces the previous line has had
					var i = linesBreak.get(oldLine * 2);
					var leadingSpace = new StringBuilder();
					while (i < oldText.length() && oldText.charAt(i++) == ' ')
						leadingSpace.append(" ");
					// insert the same amount of spaces
					insertAtCursor(leadingSpace);
				}
				return true;
			}

			return super.keyTyped(event, character);
		}
	}

	enum SymbolKind {
		IDENTIFIER,
		NUMBER,
		STRING,
		COMMENT,
		BRACKET,
		OPERATOR,
		OTHER
	}
}

