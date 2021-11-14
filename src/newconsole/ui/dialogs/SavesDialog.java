package newconsole.ui.dialogs;

import arc.graphics.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import newconsole.*;
import newconsole.io.*;
import newconsole.ui.*;

public class SavesDialog extends BaseDialog {
	
	public Table scriptsTable;
	public TextField saveName;
	
	public SavesDialog() {
		super("@newconsole.scripts-header");
		closeOnBack();
		
		cont.table(save -> {
			save.button("@newconsole.save", Styles.nodet, () -> {
				String name = saveName.getText();
				if (name.replaceAll("\\s", "").equals("")) {
					Vars.ui.showInfo("@newconsole.empty-name");
					return;
				}
				
				String script = ConsoleVars.console.area.getText();
				if (script.replaceAll("\\s", "").equals("")) {
					Vars.ui.showInfo("@newconsole.empty-script");
					return;
				}
				
				if (ScriptsManager.scripts.containsKey(name)) {
					//Overwrite, ask the player to confirm
					Vars.ui.showConfirm("@newconsole.overwrite-confirm", () -> {
						ScriptsManager.saveScript(name, script);
						rebuild();
					});
				} else {
					ScriptsManager.saveScript(name, script);
					rebuild();
				}
			}).width(90).get();
			
			saveName = save.field("", input -> {}).growX().get();
			saveName.setMessageText("@newconsole.input-name");
		}).growX().marginBottom(50).row();
		
		cont.add(new BetterPane(table -> {
			scriptsTable = table;
		})).grow().row();
		
		cont.button("@newconsole.close", Styles.nodet, () -> hide()).growX();
	}
	
	public void rebuild() {
		scriptsTable.clearChildren();
		ScriptsManager.eachScript((name, script) -> add(name, script));
	}
	
	@Override
	public Dialog show(Scene stage, Action action) {
		rebuild();
		return super.show(stage, action);
	}
	
	public void add(String name, String script) {
		scriptsTable.table(entry -> {
			entry.center().left().setBackground(CStyles.scriptbg);
			entry.add(name).width(250).marginLeft(20).get().setWrap(true);
			
			entry.table(actions -> {
				actions.center().right().defaults().center().size(80);
				
				actions.button(CStyles.playIcon, Styles.nodei, () -> {
					ConsoleVars.console.runConsole(script);
				});
				
				actions.button(CStyles.editIcon, Styles.nodei, () -> {
					ConsoleVars.console.area.setText(script);
					hide();
				});
				
				actions.button(CStyles.deleteIcon, Styles.nodei, () -> {
					Vars.ui.showConfirm("@newconsole.delete-confirm", () -> {
						ScriptsManager.deleteScript(name);
						scriptsTable.removeChild(entry);
					});
				});
			}).center().growX();
		}).growX().pad(10f).marginBottom(20).row();
	}
	
}