package newconsole;

import arc.scene.event.Touchable;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Log;
import arc.util.Time;
import io.mnemotechnician.autoupdater.*;
import arc.*;
import mindustry.Vars;
import mindustry.game.*;
import mindustry.gen.Icon;
import mindustry.mod.*;
import mindustry.ui.Styles;
import newconsole.io.AutorunManager;
import newconsole.io.ScriptsManager;
import newconsole.ui.CStyles;
import newconsole.ui.FloatingWidget;
import newconsole.ui.dialogs.*;

public class NewConsoleMod extends Mod {
	
	public NewConsoleMod() {
		Vars.loadLogger();

		Events.on(EventType.ClientLoadEvent.class, event -> {
			CStyles.loadSync();
			init();
		});
		NewConsoleMod.class.getClassLoader();

		Events.on(EventType.ClientLoadEvent.class, a -> checkUpdates());
	}

	public static void executeStartup() {
		try {
			var file = Vars.tree.get(ConsoleVars.startup);
			if (!file.exists()) {
				Log.warn("Startup script not found.");
				return;
			}

			Log.info("Executing startup script...");
			Time.mark();
			Vars.mods.getScripts().runConsole(file.readString());
			Log.info("Startup script executed in [blue]" + Time.elapsed() + "[] ms.");
		} catch (Throwable e) {
			Log.err("Failed to execute startup script!", e);
		}
	}

	public void checkUpdates() {
		Updater.checkUpdates(this);
	}

	public void init() {
		ConsoleVars.group = new WidgetGroup();
		ConsoleVars.group.setFillParent(true);
		ConsoleVars.group.touchable = Touchable.childrenOnly;
		ConsoleVars.group.visible(() -> ConsoleVars.consoleEnabled);
		Core.scene.add(ConsoleVars.group);
		ConsoleVars.console = new Console();

		ConsoleVars.saves = new SavesDialog();
		ConsoleVars.copypaste = new CopypasteDialog();
		ConsoleVars.fileBrowser = new FileBrowser();
		ConsoleVars.autorun = new AutorunDialog();

		ConsoleVars.floatingWidget = new FloatingWidget();

		ConsoleVars.floatingWidget.button(Icon.terminal, Styles.defaulti, ConsoleVars.console::show).get()
			.setSize(ConsoleVars.floatingWidget.dragger.getWidth(), ConsoleVars.floatingWidget.dragger.getHeight());

		ConsoleVars.group.addChild(ConsoleVars.floatingWidget);
		Time.run(10, () -> ConsoleVars.floatingWidget.setPosition(ConsoleVars.group.getWidth() / 2, ConsoleVars.group.getHeight() / 1.5f));

		ScriptsManager.init();
		AutorunManager.init();
		executeStartup();
	}
}
