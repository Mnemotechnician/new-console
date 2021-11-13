package newconsole.ui.dialogs;

import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
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
			save.button("[accent]Save", Styles.nodet, () -> {
				String name = saveName.getText();
				if (name.replaceAll("\\w", "").equals("")) {
					//todo: show a popup
					return;
				}
				
				String script = ConsoleVars.console.area.getText();
				if (script.replaceAll("\\w", "").equals("")) {
					//todo: show popup
					return;
				}
				ScriptManager.saveScript(name, script);
			}).width(90).get();
			
			saveName = save.field("", () -> {}).growX().get();
		}).marginBottom(50).row();
		
		cont.add(new BetterPane(table -> {
			scriptsTable = table;
			
			ScriptsManager.eachScript((name, script) -> add(name, script));
		})).grow().row();
		
		cont.button("@newconsole.close", Styles.nodet, () -> hide()).growX();
	}
	
	public void add(String name, String script) {
		scriptsTable.table(entry -> {
			entry.left().setBackground(CStyles.scriptbg);
			entry.add(name).marginRight(40);
					
			entry.button("Run", Styles.nodet, () -> {
				ConsoleVars.console.runConsole(script);
			});
			
			entry.button("Edit", Styles.nodet, () -> {
				ConsoleVars.console.area.setText(script);
			});
			
			entry.button("Delete", Styles.nodet, () -> {
				ScriptsManager.deleteScript(name);
				scriptsTable.removeChild(entry);
			});
		}).pad(10).marginBottom(20).row();
	}
	
}