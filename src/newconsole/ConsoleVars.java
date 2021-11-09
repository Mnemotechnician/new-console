package newconsole;

import arc.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.game.*;
import newconsole.ui.fragments*;

public class ConsoleVars {
	
	WidgetGroup group;
	
	public static void init() {
		Events.on(EventType.ClientLoadEvent.class, a -> {
			Vars.loadLogger();
			
			group = new WidgetGroup();
			group.setFillParent(true);
			group.touchable = Touchable.childrenOnly;
			group.visible(true);
			
			ConsoleFragment.build(group);
			
			Core.scene.add(group); //haha, anukus
		});
	}
	
}