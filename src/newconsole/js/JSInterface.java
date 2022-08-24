package newconsole.js;

import arc.files.Fi;
import arc.func.Cons2;
import arc.struct.StringMap;
import mindustry.Vars;
import newconsole.ConsoleVars;
import newconsole.NewConsoleMod;
import newconsole.io.ScriptsManager;
import newconsole.ui.dialogs.Console;
import newconsole.ui.dialogs.SavesDialog;

/**
 * This class allows js scripts to interact with modded classes.
 */
public class JSInterface {

	public NewConsoleMod getMod() {
		return (NewConsoleMod) Vars.mods.getMod("newconsole").main;
	}

	public Console getConsole() {
		return ConsoleVars.console;
	}

	public SavesDialog getSavesDialog() {
		return ConsoleVars.saves;
	}

	public void loadScripts(Fi file) {
		ScriptsManager.loadSave(file);
	}

	public void saveAll() {
		ScriptsManager.save();
	}

	public StringMap getScriptsMap() {
		return ScriptsManager.scripts;
	}

	public void eachScript(Cons2<String, String> cons) {
		ScriptsManager.eachScript(cons);
	}

	public void saveScript(String name, String script) {
		ScriptsManager.saveScript(name, script);
	}

	public void deleteScript(String name) {
		ScriptsManager.deleteScript(name);
	}

	public void checkUpdates() {
		getMod().checkUpdates();
	}

}
