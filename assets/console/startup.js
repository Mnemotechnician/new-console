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

const _interface = classForName("newconsole.js.JSInterface").newInstance();
const _buffer = _interface.getConsole().logBuffer;
const _defaultMethods = Object.keys(new Object());
const _nativeContains = (array, name) => {
	for (i in array) {
		if (i.equals(name)) return true;
	}
	return false;
}

function NCHelp() {
	//todo: create a separate file? these .append()s are killing me
	
	_buffer.append("\n\n[green]")
	.append("####################\n")
	.append("# New Console Help #\n")
	.append("####################[]\n\n")
	.append("[white]You can use [blue]NewConsole[] object to access newconsole stuff.\n")
	.append("Available methods of NewConsole object (self-explanatory):\n")
	for (i in _interface) {
		if (!_nativeContains(_defaultMethods, i)) {
			_buffer.append("NewConsole.[blue]").append(i).append("[];\n");
		}
	}
	_buffer.append("\n\n")
	.append("You can use [blue]prev[] and [blue]next[] buttons to navigate in console history.\n")
	.append("The current input is saved to history whenever you run it or press 'next'/'prev'\n\n")
	.append("You can save, load and edit scripts by opening the scripts dialog.\n")
	.append("In order to do that, press the 'scripts' button in bottom right panel.\n\n")
	.append("\n\n\n")
	.append("[blue]Default functions:[]\n")
	.append("NCHelp() — show this help\n")
	.append("append(Any value) — append value directly to the log.\n")
	.append("println(Any value) — same as append() but adds a newline\n")
	.append("backread() — reads last_log.txt and overrides the output\n");
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