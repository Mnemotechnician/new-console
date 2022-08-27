package newconsole.game;

import arc.Core;
import mindustry.Vars;
import newconsole.ConsoleVars;

public class ConsoleSettings {
	public static void init() {
		Vars.ui.settings.addCategory("NewConsole", root -> {
			root.sliderPref("newconsole.font-size", 15, 8, 48, value -> {
				return value + " pt.";
			});

			root.sliderPref("newconsole.tab-size", 4, 2, 8, value -> {
				ConsoleVars.console.area.setTabSize(value);
				return value + "x";
			});
		});
	}

	public static int fontSize() {
		return Core.settings.getInt("newconsole.font-size", 15);
	}

	public static int tabSize() {
		return Core.settings.getInt("newconsole.tab-size", 4);
	}
}
