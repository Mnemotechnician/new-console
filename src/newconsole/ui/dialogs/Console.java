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
	public static StringBuilder logBuffer = new StringBuilder(5000);
	/** Input history, used to allow the user to redo/undo last inputs. #0 is the current input */
	public static Seq<String> history = Seq.with("", "");
	/** Current command. -1 means that the input is empty */
	public static int historyIndex = -1;
	
	protected static boolean needsInit = true;
	
	public TextArea area;
	public Label logLabel;
	public BetterPane leftPane, rightPane;
	
	protected float lastWidth, lastHeight;
	
	public Console() {
		super("@newconsole.console-header");
		closeOnBack();

		cont.center().margin(0).fill();
		cont.table(main -> {
			main.left().bottom();
			
			main.table(horizontal -> {
				horizontal.left().bottom();

				horizontal.table(left -> {
					logLabel = new Label(logBuffer, CStyles.monoLabel) {{
						update(() -> setText(logBuffer));
					}};
					left.add(leftPane = new BetterPane(logLabel)).grow();
				}).grow().uniformX();
				
				var rightTable = horizontal.table(script -> {
					script.bottom().defaults().bottom().left();
					
					script.table(buttons -> {
						buttons.left().table(twoRows -> {
							twoRows.table(history -> {
								history.defaults().height(40).width(100);
								
								history.button("@newconsole.prev", Styles.defaultt, () -> {
									historyShift(1);
								});
								
								history.button("@newconsole.next", Styles.defaultt, () -> {
									historyShift(-1);
								}).row();
								
								history.button("@newconsole.clear", Styles.defaultt, () -> {
									logBuffer.setLength(0);
								});
								
								history.button("@newconsole.clipboard", Styles.defaultt, () -> {
									ConsoleVars.copypaste.setTarget(area).show();
								});
							});
							
							twoRows.button("@newconsole.run", Styles.defaultt, () -> {
								String code = area.getText();
								
								historyIndex = 0;
								addHistory(code);
								runConsole(code);
							}).width(100).growY();
						}).row();
						
						buttons.table(lower -> {
							lower.defaults().width(100).height(40);
							lower.left();
							
							lower.button("@newconsole.scripts", Styles.defaultt, () -> {
								ConsoleVars.saves.show();
							});
							
							lower.button("@newconsole.files", Styles.defaultt, () -> {
								ConsoleVars.fileBrowser.show();
							});
							
							lower.button("@newconsole.autorun", Styles.defaultt, () -> {
								ConsoleVars.autorun.show();
							});
						});
					}).growX().row();
					
					script.add(new BetterPane(input -> {
						area = input.area("", CStyles.monoArea, text -> {
							history.set(0, text);
							historyIndex = 0;
							
							area.setPrefRows(area.getLines());
							rightPane.layout();
						}).bottom().left().grow().padRight(5f).get();
						
						area.removeInputDialog();
						area.setMessageText("@newconsole.input-script");
					})).grow().with(it -> {
						it.setForceScroll(false, true);
						it.setScrollingDisabledX(true);
						rightPane = it;
					});
				}).bottom().grow().uniformX().get();
			}).grow().row();
			
			main.button("@newconsole.close", Styles.defaultt, () -> {
				hide();
			}).fillX();
		}).grow().row();
		
		init();
	}
		
	public static void init() {
		if (!needsInit) return;
		needsInit = false;
		
		// register a new log handler that retranslates logs to the custom console
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

	@Override
	public void act(float delta) {
		super.act(delta);
		// long buffer causes the console to lag. so we just trim it.
		if (logBuffer.length() > 20000) {
			var start = logBuffer.length() - 20000;
			logBuffer.delete(0, start);
		}
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
				Time.run(4, () -> {
					if (ConsoleVars.console != null) {
						ConsoleVars.console.scrollDown();
					}
				});
			} else {
				warn("last log file doesn't exist");
			}
		} catch (Throwable e) {
			err("Failed to read last log", e);
		}
	}
	
	public void addLog(String newlog) {
		info(dontResendStr + newlog);
		logBuffer.append(newlog);
		Time.run(4, () -> scrollDown());
	}
	
	public void runConsole(String code) {
		//messages starting with \u0019 aren't re-sent
		addLog("[blue]JS $ [grey]" + Strings.stripColors(code) + "\n");
		String log = Vars.mods.getScripts().runConsole(code);
		addLog("[yellow]> [lightgrey]" + Strings.stripColors(log) + "\n");
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
	
	public void setCode(String code) {
		area.setText(code);
	}
}
