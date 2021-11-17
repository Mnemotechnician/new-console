package newconsole;

import io.mnemotechnician.autoupdater.*;
import arc.*;
import mindustry.game.*;
import mindustry.mod.*;
import newconsole.*;

public class NewConsoleMod extends Mod {
	
	public NewConsoleMod() {
		ConsoleVars.init();
		
		Events.on(EventType.ClientLoadEvent.class, a -> {
			Updater.checkUpdates(this);
		});
	}
	
}