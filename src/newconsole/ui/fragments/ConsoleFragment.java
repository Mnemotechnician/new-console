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

import static arc.util.Log.*;

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
	
	public ConsoleFragment(Group parent) {
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
			
			main.add("@newconsole.console-header").row();
			
			main.table(horizontal -> {
				horizontal.label(() -> logBuffer).grow();
				
				horizontal.table(script -> {
					area = script.area("", text -> {
						history.set(0, text);
						historyIndex = 0;
					}).marginLeft(margin).marginRight(margin).grow().get();
					area.removeInputDialog();
					area.setMessageText("insert your js script here");
					
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
							
							//messages starting with \u0019 aren't re-sent
							addLog("\u0019[blue]JS $ [grey]" + code + "\n");
							String log = Vars.mods.getScripts().runConsole(code);
							addLog("\u0019[yellow]> [white]" + log + "\n");
						}).growX();
					}).growX();
				});
			});
		}).row();
		
		root.button("@newconsole.close", Styles.nodet, () -> {
			dialog.hide();
		}).growX();
		
		//register a new log handler that retranslates logs to the custom console
		var defaultLogger = logger;
		logger = (level, message) -> {
			if (!message.startsWith('\u0019')) {
				logBuffer.append((switch (level) {
					case debug -> "[white][[[yellow]D[]][]";
					case info -> "[white][[[blue]I[]][]";
					case warn -> "[white][[[orange]W[]][]";
					case err -> "[white][[[red]E[]][]";
					default -> "[white][[?][]";
				}) + " " + message + "\n");
			}
			
			if (defaultLogger != null) defaultLogger.log(level, message);
		};
	}
	
	public void addLog(String newlog) {
		info(newlog);
		logBuffer.append(newlog);
	}
	
	public void addHistory(String command) {
		if (history.size < 1) {
			history.add("");
		}
		String check = command.replaceAll("\\s", "");
		if (check.equals(history.get(1)) || check.equals("")) {
			return; //no need to add the same script twice
		}
		if (history.size >= 50) {
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