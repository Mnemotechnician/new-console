package newconsole;

import mindustry.mod.*;
import newconsole.*;
import newconsole.ui.*;

public class NewConsoleMod extends Mod {
	
	public NewConsoleMod() {
		ConsoleVars.init();
	}
	
	@Override
	public void loadContent() {
		//CStyles.load();
	}
	
}