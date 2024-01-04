importPackage(Packages.java.lang);
importPackage(Packages.arc.files);
const modClassLoader = Vars.mods.mainLoader();

function classForName(name) {
	try {
		return Class.forName(name, true, modClassLoader);
	} catch (e) {
		Log.err(e);
		return null;
	}
}

function importCls(name) {
	return importClass(new Packages.rhino.NativeJavaClass(Vars.mods.scripts.scope, Class.forName(name, true, Vars.mods.mainLoader())))
}

const _interface = classForName("newconsole.js.JSInterface").newInstance();
const _buffer = _interface.getConsole().logBuffer;
const _defaultMethods = new java.lang.Object();
const _nativeContains = (array, name) => {
	for (i in array) {
		if (i.equals(name)) return true;
	}
	return false;
}

function NCHelp() {
	println("[green]#  hello and welcome to new console one of the first consoles that can be publicly used  #\n#  everything will be explaned here  #\n\n    [white]lets start with what you see now\non this side this is the [yellow]logs [white]every log is printed here\n[red][[Warning]the log scroll back is limted tp 30k characters for performance reasons\n\n    [white] on the right side is the console input\nlets start with what it can do with the buttons:\n\nthe run button well runs the current code\n\nthe prev button changes the current code to the previously ran code in the logs\nthe next button well is the same as the prev button just of course the next in the logs if theres nothing next it will clear the code input\n\nthe scripts button well lets you well view/run saves scripts or save scripts\n\nthe files button opens the ingame file browser [red]WARNING DANGEROUS\n\n[white]the events button opens the script autoren menu [[unfinished?]\n\n    if you want to access anything from this mod just use the [blue]NewConsole [white]object\n\n    [yellow]here are the default functions:\n[white]NCHelp() prints this message\nappend(value) add value to the logs [red]doesnt print to ingame logs\n[white]println(value) append just with \n at the start\nbackread() reads the logs and override the output?\n\nimportCls(name) import any (even modded) vlass by its fully-qualified name?")
}

const append = text => {
	_buffer.append(text);
	return null;
};
const println = text => {
	_buffer.append(text).append("\n");
	return null;
};
const backread = () => _interface.getConsole().backread();

const NewConsole = _interface;

Log.info("Tip: you can access newconsole stuff via the NewConsole object!");
