package newconsole.ui;

import arc.*;
import arc.util.*;
import arc.math.*;
import arc.math.geom.*;
import arc.input.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.scene.event.*;
import arc.scene.event.InputEvent.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.fragments.*;

/** Table that can be dragged across a WidgetGroup. */
public class FloatingWidget extends Table {
	
	public static Color dragged = Color.valueOf("#aaaaaa"), normal = Color.white.cpy();
	
	public ImageButton dragger;
	public boolean isDragging = false;
	
	public FloatingWidget() {
		dragger = new ImageButton(Icon.move, Styles.nodei);
		add(dragger).size(40f);
		
		dragger.addListener(new InputListener() {
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
				isDragging = true;
				return true;
			}
			
			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				positionParent(x, y);
			}
			
			@Override
			public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
				isDragging = false;
			}
			
		});
		
		update(() -> {
			color.set(isDragging ? dragged : normal);
		});
	}
	
	public void positionParent(float x, float y) {
		Vec2 pos = localToParentCoordinates(Tmp.v1.set(x, y));
		setPosition(pos.x, pos.y);
	}
	
}