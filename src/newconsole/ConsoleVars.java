package newconsole;

import arc.*;
import arc.util.*;
import arc.scene.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.ui.*;

import newconsole.io.*;
import newconsole.ui.*;
import newconsole.ui.dialogs.*;

public class ConsoleVars {
	
	/** Main group containing the console ui */
	public static WidgetGroup group;
	/** The floating window that allows the user to open the console ui */
	public static FloatingWidget floatingWidget;
	/** Custom console handler */
	public static Console console;
	/** Dialog that allows the user to save & load scripts */
	public static SavesDialog saves;
	/** Whether the console ui is enabled */
	public static boolean consoleEnabled = true;
	
	
	public static void init() {
		Vars.loadLogger();
		
		Events.on(EventType.ClientLoadEvent.class, a -> {
			group = new WidgetGroup();
			group.setFillParent(true);
			group.touchable = Touchable.childrenOnly;
			group.visible(() -> consoleEnabled);
			Core.scene.add(group); //haha, anukus ඞ
			console = new Console();
			
			floatingWidget = new FloatingWidget();
			floatingWidget.button(Icon.terminal, Styles.nodei, () -> console.show());
			group.addChild(floatingWidget);
			Time.run(10, () -> floatingWidget.setPosition(group.getWidth() / 2, group.getHeight() / 1.5f));
			
			CStyles.load(); //for some reason mindustry.gen.Tex fields are null during mod loading
			
			ScriptsManager.init();
		});
	}
	
}