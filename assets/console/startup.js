importPackage(Packages.java.lang);
importPackage(Packages.arc.files);
const modClassLoader = Vars.mods.mainLoader();

function classForName(name) {
	return Class.forName(name, true, modClassLoader);
}

let rootMod = classForName("newconsole.NewConsoleMod");
let vars = classFotName("newconsole.ConsoleVars");
let scriptsManager = classForName("newconsole.io.ScriptsManager");

const NewConsole = {
	Mod: rootMod,
	Vars: vars,
	getConsole: () => NewConsole.Vars.console,
	ScriptsIO: scriptsManager
};

Log.info("Tip: you can access newconsole stuff via the NewConsole object!");