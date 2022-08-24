package newconsole;

import arc.scene.ui.layout.*;

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
	/** Copy / paste dialog, made mostly for mobile devices (TextArea doesn't support that natively) */
	public static CopypasteDialog copypaste;
	/** File browser dialog */
	public static FileBrowser fileBrowser;
	/** Autorun dialog */
	public static AutorunDialog autorun;
	/** Whether the console ui is enabled */
	public static boolean consoleEnabled = true;
	/** Startup js script path, relative to the asset tree */
	public static String startup = "console/startup.js";

}
