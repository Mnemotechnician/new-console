package newconsole.ui.dialogs;

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
				ScriptsManager.saveScript(name, script);
				rebuild();
			}).width(90).get();
			
			saveName = save.field("", input -> {}).growX().get();
			saveName.setMessage("@newconsole.input-name");
		}).marginBottom(50).row();
		
		cont.add(new BetterPane(table -> {
			scriptsTable = table;
		})).grow().row();
		rebuild();
		
		cont.button("@newconsole.close", Styles.nodet, () -> hide()).growX();
	}
	
	public void rebuild() {
		scriptsTable.clearChildren();
		ScriptsManager.eachScript((name, script) -> add(name, script));
	}
	
	public void add(String name, String script) {
		scriptsTable.table(entry -> {
			entry.center().left().setBackground(CStyles.scriptbg);
			entry.add(name).marginRight(40);
			
			entry.table(actions -> {
				actions.right();
				
				actions.button("Run", Styles.nodet, () -> {
					ConsoleVars.console.runConsole(script);
				});
				
				entry.button("Edit", Styles.nodet, () -> {
					ConsoleVars.console.area.setText(script);
					hide();
				});
				
				entry.button("Delete", Styles.nodet, () -> {
					ScriptsManager.deleteScript(name);
					scriptsTable.removeChild(entry);
				});
			}).growX();
		}).growX().pad(20).marginBottom(20).row();
	}
	
}