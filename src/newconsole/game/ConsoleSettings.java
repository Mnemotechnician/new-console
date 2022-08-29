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

			root.checkPref("newconsole.syntax-highlighting", true);

			root.checkPref("newconsole.indentation-assistance", true);

			root.checkPref("newconsole.indentation-guides", true);

			root.checkPref("newconsole.insert-paired-chars", true);
		});
	}

	public static int fontSize() {
		return Core.settings.getInt("newconsole.font-size", 15);
	}

	public static int tabSize() {
		return Core.settings.getInt("newconsole.tab-size", 4);
	}

	public static boolean indentationAssistance() {
		return Core.settings.getBool("newconsole.indentation-assistance", true);
	}

	public static boolean indentationGuides() {
		return Core.settings.getBool("newconsole.indentation-guides", true);
	}

	public static boolean characterPairs() {
		return Core.settings.getBool("newconsole.insert-paired-chars", true);
	}

	public static boolean syntaxHighlighting() {
		return Core.settings.getBool("newconsole.syntax-highlighting", true);
	}
}
