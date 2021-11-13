package newconsole.ui.fragments;

import arc.*;
import arc.func.*;
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
	public static StringBuffer logBuffer = new StringBuffer("-------- js console output goes here --------\n"); //haha jaba
	/** Input history, used to allow the user to redo/undo last inputs. #0 is the current input */
	public static Seq<String> history = Seq.with("", "");
	/** Current command. -1 means that the input is empty */
	public static int historyIndex = -1;
	
	public FloatingWidget floatingWidget;
	public TextArea area;
	public BaseDialog dialog;
	public Label logLabel;
	
	protected float lastWidth, lastHeight;
	
	public ConsoleFragment(Group parent) {
		floatingWidget = new FloatingWidget();
		floatingWidget.button(Icon.terminal, Styles.nodei, () -> dialog.show());
		parent.addChild(floatingWidget);
		floatingWidget.setPosition(parent.getWidth() / 2, parent.getHeight() / 1.5f);
		
		dialog = new BaseDialog("console");
		dialog.closeOnBack();
		var root = dialog.cont;
		root.center().margin(0);
		root.table(main -> {
			main.left().top();
			
			main.add("@newconsole.console-header").row();
			
			main.table(horizontal -> {
				horizontal.left();
				
				var left = new BetterPane(logLabel = new Label(() -> logBuffer));
				horizontal.add(left);
				
				var right = horizontal.table(script -> {
					script.defaults().left();
					
					script.table(buttons -> {
						buttons.defaults().width(90).fill();
						
						buttons.button("@newconsole.prev", Styles.nodet, () -> {
							historyShift(1);
						});
						
						buttons.button("@newconsole.next", Styles.nodet, () -> {
							historyShift(-1);
						});
						
						buttons.button("@newconsole.run", Styles.nodet, () -> {
							String code = area.getText();
							
							historyIndex = 0;
							addHistory(code);
							
							//messages starting with \u0019 aren't re-sent
							addLog("\u0019[blue]JS $ [grey]" + code.replaceAll("\\[", "[[") + "\n");
							String log = Vars.mods.getScripts().runConsole(code);
							addLog("\u0019[yellow]> [lightgrey]" + log + "\n");
						}).row();
						
						buttons.button("@newconsole.clear", Styles.nodet, () -> {
							logBuffer.setLength(0);
						});
					}).left().row();
					
					script.add(new BetterPane(input -> {
						area = input.area("", text -> {
							history.set(0, text);
							historyIndex = 0;
							area.setPrefRows(area.getLines() * 2);
						}).left().grow().get();
						area.removeInputDialog();
						area.setMessageText("insert your js script here");
					})).grow().minHeight(200);
				}).get();
				
				//me when no help
				horizontal.update(() -> {
					float targetWidth = root.getWidth() / 2f;
					float targetHeight =  root.getHeight() / 2f;
					left.setSize(targetWidth, targetHeight);
					right.setSize(targetWidth, targetHeight);
					
					if (targetWidth != lastWidth || targetHeight != lastHeight) {
						right.invalidateHierarchy();
						left.invalidateHierarchy();
						lastWidth = targetWidth;
						lastHeight = targetHeight;
					}
				});
			}).grow().row();
			
			main.button("@newconsole.close", Styles.nodet, () -> {
				dialog.hide();
			}).fillX();
		}).grow().row();
		
		//register a new log handler that retranslates logs to the custom console
		var defaultLogger = logger;
		logger = (level, message) -> {
			if (!message.startsWith("\u0019")) {
				logBuffer.append((switch(level) {
					case debug -> "[lightgrey][[[yellow]D[]][]";
					case info -> "[lightgrey][[[blue]I[]][]";
					case warn -> "[lightgrey][[[orange]W[]][]";
					case err -> "[lightgrey][[[red]E[]][]";
					default -> "[lightgrey][[?][]";
				}) + " [lightgrey]" + message + "\n");
				
				logLabel.invalidateHierarchy(); //it doesn't seem to invalidate automatically upon such an event
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
		
		if (command.equals(history.get(1)) || command.replaceAll("\\s", "").equals("")) {
			return; //no need to add the same script twice
		}
		if (history.size >= 50) {
			history.remove(history.size - 1);
		} 
		history.insert(1, command);
	}
	
	public void historyShift(int shift) {
		historyIndex = Mathf.clamp(historyIndex + shift, -1, history.size - 1);
		if (historyIndex < 0) {
			area.setText("");
			return;
		}
		area.setText(history.get(historyIndex));
	}
	
	/** Anuke, what the fucking fuck?
	 * the whole point of a scroll pane is to fit bigger widgets in a smaller space, not to reduce their visual space */
	public static class BetterPane extends ScrollPane {
		
		public BetterPane(Element element) {
			super(element);
		}
		
		public BetterPane(Cons<Table> build) {
			super(new Table());
			build.get((Table) getWidget());
		}
		
		@Override
		public float getPrefWidth() {
			return width;
		}
		
		@Override
		public float getPrefHeight() {
			return height;
		}
		
	}
	
}