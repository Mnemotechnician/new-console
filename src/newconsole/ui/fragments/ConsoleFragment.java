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

import newconsole.ui.*;

public class ConsoleFragment {
	
	/** Input & output log */
	public static StringBuffer logBuffer = new StringBuffer(); //haha jaba
	/** Input history, used to allow the user to redo/undo last inputs */
	public static Seq<String> history = new Seq(10);
	/** Current command. -1 means that the input is empty */
	public static int historyIndex = -1;
	
	public float margin = 50f;
	public boolean shown = false;
	public FloatingWidget floatingWidget;
	public TextArea area;
	
	public void build(Group parent) {
		floatingWidget = new FloatingWidget();
		floatingWidget.button(Icon.terminal, Styles.nodei, this::toggle);
		parent.addActor(floatingWidget);
		floatingWidget.setPosition(200, 200);
		
		parent.fill(root -> {
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
						script.button("@newconsole.prev", Styles.nodet, () -> {
							area.setText(historyPrev());
						});
						
						script.button("@newconsole.next", Styles.nodet, () -> {
							area.setText(historyNext());
						});
						
						script.button("@newconsole.run", Styles.nodet, () -> {
							String code = area.getText();
							
							historyIndex = 0;
							addHistory(script);
							
							addLog("[blue]JS $ [grey]" + code + "\n");
							String log = Vars.mods.getScripts().runConsole(code);
							addLog("[yellow]> [white]" + log + "\n");
						}).growX();
					});
				});
			});
			
			root.button("@newconsole.close", Styles.nodet, () -> {
				toggle();
			}).growX();
			
			update(() -> {
				root.visible = shown;
			});
		});
	}
	
	public void toggle() {
		shown = !shown;
	}
	
	public void addLog(String newlog) {
		Log.info(newlog);
		logBuffer.append(newlog);
	}
	
	public void addHistory(String command) {
		if (history.size >= 10) {
			history.remove(history.size - 1);
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