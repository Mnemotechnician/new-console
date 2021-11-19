package newconsole.js;

import arc.files.*;
import arc.struct.*;
import newconsole.*;
import newconsole.io.*;
import newconsole.ui.dialogs.*;

/** This class allows js scripts to interact with modded classes. */
public class JSInterface {

	private ConsoleVars varsInstance;
	
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
		ScriptsManager.loadFile(file);
	}
	
	public void saveAll() {
		ScriptsManager.save();
	}
	
	public ObjectMap<String, String> getScriptMap() {
		
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
	
}