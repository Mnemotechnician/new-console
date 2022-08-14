package newconsole.ui;

import arc.Core;
import arc.struct.IntSeq;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.func.*;
import arc.input.KeyCode;
import arc.math.*;
import arc.scene.Scene;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.Drawable;
import arc.scene.ui.*;
import arc.util.*;
import arc.util.pooling.Pools;

/*
 * todo remove if my PR geta merged.
 *
 * or, alternatively, turn into a code-assisting text area.
 */
public class FixedTextArea extends TextArea {
    public FixedTextArea(String text) {
        super(text);
    }

    public FixedTextArea(String text, TextFieldStyle style) {
        super(text, style);
    }

    public void changed(Cons<String> listener) {
        changed(() -> listener.get(getText()));
    };

    @Override
    protected void updateDisplayText() {
        super.updateDisplayText();

        glyphPositions.clear();
        layout.setText(style.font, displayText.toString().replace('\n', ' ').replace('\r', ' '));
        var runs = layout.runs;

        if (runs.size > 0) {
            var xAdvances = runs.first().xAdvances;
            float x = 0;
            for(int j = 1; j < xAdvances.size; j++){
                glyphPositions.add(x);
                x += xAdvances.get(j);
            }
            glyphPositions.add(x);
        }
    }

    @Override
    protected void drawSelection(Drawable selection, Font font, float x, float y){
        int i = firstLineShowing * 2;
        float offsetY = 0;
        int minIndex = Math.min(cursor, selectionStart);
        int maxIndex = Math.max(cursor, selectionStart);
        while(i + 1 < linesBreak.size && i < (firstLineShowing + linesShowing) * 2){

            int lineStart = linesBreak.get(i);
            int lineEnd = linesBreak.get(i + 1);

            if(!((minIndex < lineStart && minIndex < lineEnd && maxIndex < lineStart && maxIndex < lineEnd)
            || (minIndex > lineStart && minIndex > lineEnd && maxIndex > lineStart && maxIndex > lineEnd))){

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
}

