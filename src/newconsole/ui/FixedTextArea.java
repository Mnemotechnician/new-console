package newconsole.ui;

import arc.func.Cons;
import arc.graphics.g2d.Font;
import arc.scene.event.InputEvent;
import arc.scene.style.Drawable;
import arc.scene.ui.TextArea;
import arc.scene.ui.TextField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
 * Was created to fix an arc-level bug that didn't allow to use custom fonts.
 * Will be made into a code-assisting text area.
 */
public class FixedTextArea extends TextArea {
	private static Method insertMethod;

	static {
		try {
			insertMethod = TextField.class.getDeclaredMethod("insert", Integer.TYPE, CharSequence.class, String.class);
			insertMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("java fucking sucks. use kotlin or groovy, folks!", e);
		}
	}

	public FixedTextArea(String text) {
		super(text);
	}


	public FixedTextArea(String text, TextFieldStyle style) {
		super(text, style);
	}

	public void insertAtCursor(CharSequence newText) {
		try {
			insertMethod.invoke(this, cursor, newText, text);
			cursor += newText.length();
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("FUCK YOU", e);
		}
	}

	public void changed(Cons<String> listener) {
		changed(() -> listener.get(getText()));
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
				updateDisplayText();
				return true;
			} else if (character == '\n') {
				var oldText = text;
				var oldLine = cursorLine;

				if (super.keyTyped(event, character) && cursorLine > 0) {
					// determine how many spaces the previous line has had
					var i = linesBreak.get(oldLine * 2);
					var leadingSpace = 0;
					while (i < oldText.length() && oldText.charAt(i++) == ' ') leadingSpace++;
					// insert the same amount of spaces
					insertAtCursor(" ".repeat(leadingSpace));
					updateDisplayText();
				}
				return true;
			}

			return super.keyTyped(event, character);
		}
	}
}

