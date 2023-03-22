package newconsole;

import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.*;
import com.github.mnemotechnician.autoupdater.Updater;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.Styles;
import newconsole.game.ConsoleSettings;
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
			initConsole();
		});

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

	public void initConsole() {
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

		ConsoleVars.floatingWidget.button(Icon.terminal, Styles.defaulti, ConsoleVars.console::show)
			.uniformX().uniformY().fill();

		ConsoleVars.group.addChild(ConsoleVars.floatingWidget);
		Time.run(10, () -> {
			// try to restore the position of the button
			var oldPosition = ConsoleSettings.getLastButtonPosition();

			ConsoleVars.floatingWidget.setPosition(
				oldPosition.x != -1 ? oldPosition.x : ConsoleVars.group.getWidth() / 2f,
				oldPosition.y != -1 ? oldPosition.y : ConsoleVars.group.getHeight() / 1.5f
			);
		});
		Timer.schedule(() -> {
			// save the position of the floating button every 7 seconds
			ConsoleSettings.setLastButtonPosition(Tmp.v1.set(
				ConsoleVars.floatingWidget.x,
				ConsoleVars.floatingWidget.y
			));
		}, 2f, 10f);

		ScriptsManager.init();
		AutorunManager.init();
		ConsoleSettings.init();
		executeStartup();
	}
}
