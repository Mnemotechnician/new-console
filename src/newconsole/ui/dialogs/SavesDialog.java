package newconsole.ui.dialogs;

import arc.scene.Action;
import arc.scene.Scene;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newconsole.ConsoleVars;
import newconsole.io.ScriptsManager;
import newconsole.ui.BetterPane;
import newconsole.ui.CStyles;
import newconsole.ui.CodeSpinner;

import java.util.Comparator;

public class SavesDialog extends BaseDialog {

	public Table scriptsTable;
	public TextField saveName;

	public SavesDialog() {
		super("@newconsole.scripts-header");
		closeOnBack();

		cont.table(save -> {
			save.button("@newconsole.save", Styles.defaultt, () -> {
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

			saveName = save.field("", input -> {
			}).growX().get();
			saveName.setMessageText("@newconsole.input-name");
		}).growX().marginBottom(40).row();

		cont.add(new BetterPane(table -> {
			scriptsTable = table;
		})).grow().row();

		cont.button("@newconsole.close", Styles.defaultt, () -> hide()).growX();
	}

	public void rebuild() {
		scriptsTable.clearChildren();

		// copy to a seq, then sort by name - fuck java.
		Seq<Pair<String, String>> seq = new Seq<>(ScriptsManager.scripts.size);
		ScriptsManager.eachScript((name, script) -> {
			seq.add(new Pair(name, script));
		});

		seq.sort(new EntryComparator());

		seq.each(it -> add(it.first, it.second));
	}

	@Override
	public Dialog show(Scene stage, Action action) {
		rebuild();
		return super.show(stage, action);
	}

	public void add(String name, String script) {
		scriptsTable.table(entry -> {
			entry.center().left().setBackground(CStyles.scriptbg);
			entry.labelWrap(name).width(250).marginLeft(20);

			entry.add(new CodeSpinner(script)).growX();

			entry.table(actions -> {
				actions.center().right().defaults().center().size(50);

				actions.button(CStyles.playIcon, Styles.defaulti, () -> {
					ConsoleVars.console.runConsole(script);
				});

				actions.button(CStyles.editIcon, Styles.defaulti, () -> {
					ConsoleVars.console.setCode(script);
					hide();
				});

				actions.button(CStyles.deleteIcon, Styles.defaulti, () -> {
					Vars.ui.showConfirm("@newconsole.delete-confirm", () -> {
						ScriptsManager.deleteScript(name);
						scriptsTable.removeChild(entry);
					});
				});
			});
		}).growX().pad(2f).marginBottom(20).row();
	}

	/**
	 * Same as kotlin.Pair.
	 */
	public static class Pair<A, B> {
		final A first;
		final B second;

		public Pair(A first, B second) {
			this.first = first;
			this.second = second;
		}
	}

	public class EntryComparator implements Comparator<Pair<String, String>> {
		@Override
		public int compare(Pair<String, String> o1, Pair<String, String> o2) {
			if (o1 == o2) return 0;

			var left = Strings.stripColors(o1.first);
			var right = Strings.stripColors(o2.first);

			for (int i = 0; i < Math.min(left.length(), right.length()); i++) {
				var diff = Character.toLowerCase(left.charAt(i)) - Character.toLowerCase(right.charAt(i));
				if (diff != 0) return diff;
			}

			if (left.length() > right.length()) return -1;
			return 1;
		}

		@Override
		public boolean equals(Object o) {
			return o == this;
		}
	}

}
