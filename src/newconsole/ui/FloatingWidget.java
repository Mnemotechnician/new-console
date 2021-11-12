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
	
	public static float draggedAlpha = 0.45f;
	
	public ImageButton dragger;
	public boolean isDragging = false;
	
	protected float dragx, dragy;
	
	public FloatingWidget() {
		dragger = new ImageButton(Icon.move, Styles.nodei);
		add(dragger);
		
		dragger.addListener(new InputListener() {
			
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
				dragx = x; dragy = y;
				isDragging = true;
				return true;
			}
			
			@Override
			public void touchDragged(InputEvent event, float x, float y, int pointer) {
				positionParent(x - dragx, y - dragy);
			}
			
			@Override
			public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
				isDragging = false;
			}
			
		});
		
		update(() -> {
			color.a = isDragging ? draggedAlpha : 1f;
			
			Vec2 pos = localToParentCoordinates(Tmp.v1.set(getX(12), getY(12)));
			setPosition(
				Mathf.clamp(pos.x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
				Mathf.clamp(pos.y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
			);
		});
	}
	
	public void positionParent(float x, float y) {
		if (parent == null) return;
		
		Vec2 pos = localToParentCoordinates(Tmp.v1.set(x, y));
		setPosition(
			Mathf.clamp(pos.x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
			Mathf.clamp(pos.y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
		);
	}
	
}