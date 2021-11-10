package newconsole.ui.fragments;

import arc.*;
import arc.util.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import newconsole.ui.*;

public class ConsoleFragment {
	
	/** Input & output log */
	public static StringBuffer logBuffer = new StringBuffer(); //haha jaba
	/** Input history, used to allow the user to redo/undo last inputs */
	public static Seq<String> history = Seq.with("", "");
	/** Current command. -1 means that the input is empty */
	public static int historyIndex = -1;
	
	public float margin = 50f;
	//public boolean shown = false;
	public FloatingWidget floatingWidget;
	public TextArea area;
	public BaseDialog dialog;
	
	public void build(Group parent) {
		floatingWidget = new FloatingWidget();
		floatingWidget.button(Icon.terminal, Styles.nodei, () -> dialog.show());
		parent.addChild(floatingWidget);
		floatingWidget.setPosition(parent.getWidth(), parent.getHeight() / 1.5f);
		
		dialog = new BaseDialog("console");
		dialog.closeOnBack();
		var root = dialog.cont;
		root.center();
		root.table(main -> {
			main.center().top();
			
			main.add("@newconsole.console-header");
			
			main.table(horizontal -> {
				horizontal.label(() -> logBuffer).width(200).height(300);
				
				horizontal.table(script -> {
					area = script.area("", text -> {
						history.set(0, text);
						historyIndex = 0;
					}).marginLeft(margin).marginRight(margin).width(200).height(300f).get();
					area.removeInputDialog();
					area.setMessageText("input your js script here");
					
					script.row();
					script.table(buttons -> {
						buttons.button("@newconsole.prev", Styles.nodet, () -> {
							area.setText(historyPrev());
						});
						
						buttons.button("@newconsole.next", Styles.nodet, () -> {
							area.setText(historyNext());
						});
						
						buttons.button("@newconsole.run", Styles.nodet, () -> {
							String code = area.getText();
							
							historyIndex = 0;
							addHistory(code);
							
							addLog("[blue]JS $ [grey]" + code + "\n");
							String log = Vars.mods.getScripts().runConsole(code);
							addLog("[yellow]> [white]" + log + "\n");
						}).growX();
					});
				});
			});
		}).row();
		
		root.button("@newconsole.close", Styles.nodet, () -> {
			dialog.hide();
		}).growX();
	}
	
	public void addLog(String newlog) {
		Log.info(newlog);
		logBuffer.append(newlog);
	}
	
	public void addHistory(String command) {
		if (history.size >= 10) {
			history.remove(history.size - 1);
		} else if (history.size < 1) {
			history.add("");
		}
		history.insert(1, command);
	}
	
	public String historyPrev() {
		historyIndex = Mathf.clamp(historyIndex + 1, -1, history.size - 1);
		if (historyIndex < 0) return "";
		return history.get(historyIndex);
	}
	
	public String historyNext() {
		historyIndex = Mathf.clamp(historyIndex - 1, -1, history.size - 1);
		if (historyIndex < 0) return "";
		return history.get(historyIndex);
	}
	
}