package newconsole.ui.dialogs;

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

import newconsole.*;
import newconsole.ui.*;

import static arc.util.Log.*;

public class Console extends BaseDialog {
	
	/** Logs starting with this char aren't retranslated to the console */
	public static final char dontResend = '\u0019';
	protected static final String dontResendStr = String.valueOf(dontResend);
	
	/** Input & output log */
	public static StringBuilder logBuffer = new StringBuilder(15000);
	/** Input history, used to allow the user to redo/undo last inputs. #0 is the current input */
	public static Seq<String> history = Seq.with("", "");
	/** Current command. -1 means that the input is empty */
	public static int historyIndex = -1;
	
	protected static boolean needsInit = true;
	
	public TextArea area;
	public Label logLabel;
	public BetterPane leftPane, rightPane;
	
	protected float lastWidth, lastHeight;
	
	static {
		logBuffer.append("-------- js console output goes here --------\n\n");
		logBuffer.append("[white]Tip: run [blue]NCHelp()[] for a quick overview[]\n\n");
	}
	
	public Console() {
		super("@newconsole.console-header");
		closeOnBack();
		cont.center().margin(0).fill();
		cont.table(main -> {
			main.left().bottom();
			
			main.table(horizontal -> {
				horizontal.left().bottom();
				
				leftPane = new BetterPane(logLabel = new Label(() -> logBuffer));
				horizontal.add(leftPane);
				
				var rightTable = horizontal.table(script -> {
					script.bottom().defaults().bottom().left();
					
					script.table(buttons -> {
						buttons.defaults().width(100).fill();
						
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
							runConsole(code);
						}).width(130).row();
						
						buttons.button("@newconsole.clear", Styles.nodet, () -> {
							logBuffer.setLength(0);
						});
						
						buttons.button("@newconsole.scripts", Styles.nodet, () -> {
							ConsoleVars.saves.show();
						});
					}).row();
					
					rightPane = script.add(new BetterPane(input -> {
						area = input.area("", text -> {
							history.set(0, text);
							historyIndex = 0;
							area.setPrefRows(area.getLines() + 10);
						}).bottom().left().grow().get();
						area.removeInputDialog();
						area.setMessageText("@newconsole.input-script");
					})).grow().get();
				}).bottom().get();
				
				//me when no help
				horizontal.update(() -> {
					float targetWidth = horizontal.getWidth() / 2f;
					float targetHeight = horizontal.getHeight();
					leftPane.setSize(targetWidth, targetHeight);
					rightTable.setSize(targetWidth, targetHeight);
					
					if (targetWidth != lastWidth || targetHeight != lastHeight) {
						rightTable.invalidateHierarchy();
						leftPane.invalidateHierarchy();
						lastWidth = targetWidth;
						lastHeight = targetHeight;
					}
				});
			}).grow().row();
			
			main.button("@newconsole.close", Styles.nodet, () -> {
				hide();
			}).fillX();
		}).grow().row();
		
		init();
	}
		
	public static void init() {
		if (!needsInit) return;
		needsInit = false;
		
		//register a new log handler that retranslates logs to the custom console
		var defaultLogger = logger;
		logger = (level, message) -> {
			if (!message.startsWith(dontResendStr)) {
				logBuffer.append((switch(level) {
					case debug -> "[lightgrey][[[yellow]D[]][]";
					case info -> "[lightgrey][[[blue]I[]][]";
					case warn -> "[lightgrey][[[orange]W[]][]";
					case err -> "[lightgrey][[[red]E[]][]";
					default -> "[lightgrey][[?][]";
				})).append(" [lightgrey]").append(message).append("\n");
			}
			
			if (defaultLogger != null) defaultLogger.log(level, message);
		};
		
		backread();
	}
	
	/** Scroll to the bottom of the log */
	public void scrollDown() {
		leftPane.setScrollY(Float.MAX_VALUE);
	}
	
	/** Tries to read the last log. Overrides the buffer on success. */
	public static void backread() {
		try {
			var log = Vars.dataDirectory.child("last_log.txt");
			if (log.exists()) {
				logBuffer.setLength(0);
				logBuffer.append(log.readString());
				Time.run(4, () -> ConsoleVars.console.scrollDown());
			} else {
				warn("last log file doesn't exist");
			}
		} catch (Throwable e) {
			err("Failed to read last log", e);
		}
	}
	
	public void addLog(String newlog) {
		info(newlog);
		logBuffer.append(newlog);
		Time.run(4, () -> scrollDown());
	}
	
	public void runConsole(String code) {
		//messages starting with \u0019 aren't re-sent
		addLog(dontResend + "[blue]JS $ [grey]" + Strings.stripColors(code) + "\n");
		String log = Vars.mods.getScripts().runConsole(code);
		addLog(dontResend + "[yellow]> [lightgrey]" + Strings.stripColors(log) + "\n");
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
			addHistory(area.getText());
			area.setText("");
			return;
		}
		area.setText(history.get(historyIndex));
	}
	
}