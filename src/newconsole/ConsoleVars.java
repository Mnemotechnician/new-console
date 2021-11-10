package newconsole;

import arc.*;
import arc.util.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.game.*;
import newconsole.ui.fragments.*;

public class ConsoleVars {
	
	/** Main group containing the console ui */
	public static WidgetGroup group;
	/** Main fragment containing the console ui */
	public static ConsoleFragment console;
	/** Whether the console ui is enabled */
	public static boolean consoleEnabled = true;
	
	
	public static void init() {
		Events.on(EventType.ClientLoadEvent.class, a -> {
			Vars.loadLogger();
			
			group = new WidgetGroup();
			group.setFillParent(true);
			group.touchable = Touchable.childrenOnly;
			group.visible(() -> consoleEnabled);
			
			console = new ConsoleFragment();
			Time.run(20, () -> console.build(group));
			
			Core.scene.add(group); //haha, anukus ඞ
		});
	}
	
}