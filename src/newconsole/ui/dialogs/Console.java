package newconsole.ui.dialogs;

import arc.*;
import arc.files.Fi;
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
	public static final String dontResendStr = String.valueOf(dontResend);
	public static Fi historySaveFile;
	
	/** Input & output log */
	public static StringBuilder logBuffer = new StringBuilder(5000);
	/** Input history, used to allow the user to redo/undo last inputs. #0 is the current input */
	public static Seq<String> history = Seq.with("", "");
	/** Current command. -1 means that the input is empty */
	public int historyIndex = -1;
	
	protected static boolean needsInit = true;
	
	public FixedTextArea area;
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
						input.add(area = new FixedTextArea("", CStyles.monoArea)).bottom().left().grow().get();

						area.changed(text -> {
							history.set(0, text);
							historyIndex = 0;
						});
						
						area.setFocusTraversal(false);
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

		historySaveFile = Vars.dataDirectory.child("saves").child("newconsole.history");
		// celete the save if it's a directory
		if (historySaveFile.exists() && historySaveFile.isDirectory()) historySaveFile.deleteDirectory();
		var backup = historySaveFile.sibling(historySaveFile.name() + ".backup");
		if (backup.exists() && backup.isDirectory()) backup.deleteDirectory();


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
		readHistory();
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
		Time.run(4, this::scrollDown);
	}
	
	public void runConsole(String code) {
		//messages starting with \u0019 aren't re-sent
		addLog("[blue]JS $ [grey]" + Strings.stripColors(code) + "\n");
		String log = Vars.mods.getScripts().runConsole(code);
		addLog("[yellow]> [lightgrey]" + Strings.stripColors(log) + "\n");
	}
	
	public static void addHistory(String command) {
		if (history.size < 1) {
			history.add("");
		}
		
		if (command.equals(history.get(1)) || command.replaceAll("\\s", "").equals("")) {
			return; //no need to add the same script twice
		}
		if (history.size >= 100) {
			history.remove(history.size - 1);
		} 
		history.insert(1, command);
		writeHistory();
	}

	/** Writes the script history to historySaveFile. */
	public static void writeHistory() {
		var backup = historySaveFile.sibling(historySaveFile.name() + ".backup");
		if (historySaveFile.exists()) {
			historySaveFile.copyTo(backup);
		}

		try (var writes = historySaveFile.writes()) {
			writes.i(history.size);
			history.each(writes::str);
		} catch (Exception e) {
			Log.err("Failed to save console history", e);

			if (backup.exists()) {
				Log.err("Restoring the old save.");
				backup.copyTo(historySaveFile);
			}
		}
	}

	/** Reads the script history from historySaveFile and overrides the current history with it. */
	public static void readHistory() {
		var backup = historySaveFile.sibling(historySaveFile.name() + ".backup");
		Func<Fi, Boolean> readFrom = (file) -> {
			try (var reads = file.reads()) {
				var count = reads.i();
				if (count < 0 || count > 1000) throw new RuntimeException(file.absolutePath() + " is not a history save file.");

				history.clear();
				for (var i = 0; i < count; i++) {
					history.add(reads.str());
				}
				return true;
			} catch (Exception e) {
				Log.err("Failed to read the script history from " + file.absolutePath(), e);
				return false;
			}
		};

		if (historySaveFile.exists() && readFrom.get(historySaveFile)) return;
		if (backup.exists() && readFrom.get(backup)) return;

		Log.warn("Attempted to read the script history, but the save and backup files either don't exist or couldn't be loaded.");
	}

	/** Shifts the current history index and overrides the current script with the script under the new index. */
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
