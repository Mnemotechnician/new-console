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
	let help = readString("console/startup.js-help");

	let b = new StringBuilder();
	for (method in _interface) {
		if (!_nativeContains(_defaultMethods, method)) {
			b.append("NewConsole.[blue]").append(method).append("[];\n");
		}
		
	}
	
	println(Strings.format(help, b.toString()));
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
