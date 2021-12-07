package newconsole.ui;

import arc.func.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;

/** Anuke, what the fucking fuck?
 * the whole point of a scroll pane is to fit bigger widgets in a smaller space, not to reduce their visual space */
public class BetterPane extends ScrollPane {
	
	public BetterPane(Element element) {
		super(element);
	}
	
	public BetterPane(Cons<Table> build) {
		super(new Table());
		build.get((Table) getWidget());
	}
	
	@Override
	public float getPrefWidth() {
		return width;
	}
	
	@Override
	public float getPrefHeight() {
		return height;
	}
	
	/** Returns the original preferred width of a pane */
	public float getAbstractWidth() {
		return super.getPrefWidth();
	}
	
	/** Returns the original preferred height of a pane */
	public float getAbstractHeight() {
		return super.getPrefHeight();
	}
	
}