package newconsole.io;

import java.io.*;
import arc.func.*;
import arc.struct.*;
import arc.files.*;
import arc.util.*;
import mindustry.*;

public class ScriptsManager {
	
	public static final String save = "newconsole.save", def = "assets/console/default.save";
	public static final byte startScript = 3, endScript = 4, splitter = 17, eof = 127;
	
	public static StringMap scripts = new StringMap();
	
	protected static StringBuilder build = new StringBuilder();
	protected static Fi root;
	
	public static void init() {
		root = Vars.dataDirectory.child("saves");
		root.mkdirs();
		
		if (!root.exists() || !root.isDirectory()) {
			Log.err("Scripts manager failed to init");
		return;
		}
		
		if (loadSave(root.child(save))) {
			//loaded normal save
		} else if (loadSave(root.child(save + ".backup"))) {
			//original file corrupt, all scripts that could be recognized were loaded, so we just delete it and replace with backup
			root.child(save).delete();
			save();
			Log.info("loaded backup");
		} else if (loadSave(Vars.tree.get(def))) {
			Log.info("loaded default scripts");
		} else {
			Log.err("Couldn't load saved nor default scripts!");
		}
	}
	
	public static boolean loadSave(Fi save) {
		if (!save.exists()) return false;
		if (root == null) throw new IllegalStateException("ScriptsManager hasn't been initialized yet");
		
		//yeah, i did all the funny code just in case of unexpected modifications performed by the user
		var reads = save.reads();
		try {
			byte b;
			scripts:
			do {
				b = reads.b();
				if (b == startScript) {
					String name = reads.str();
					//find splitter
					while ((b = reads.b()) != splitter) {
						if (b == endScript) {
							Log.warn("illegal EOS, skipping");
							continue scripts;
						} else if (b == eof) {
							Log.warn("Illegal end of file: splitter and script body expected");
							reads.close();
							return false;
						}
					}
					
					String script = reads.str();
					scripts.put(name, script);
					
					//find end of script, just in case
					while ((b = reads.b()) != endScript) {
						if (b == eof) {
							Log.warn("EOS expected, found EOF. Ignoring.");
							break scripts;
						}
					}
				}
			} while (b != eof);
		} catch (Exception e) {
			Log.warn("Failed to read existing save file. Illegal modification?");
			reads.close();
			return false;
		}
		reads.close();
		return true;
	}
	
	/** Save scripts & create a backup */
	public static void save() {
		Fi savef = root.child(save);
		//backup
		if (savef.exists()) {
			savef.moveTo(root.child(save + ".backup"));
		}
		//save
		var writes = root.child(save).writes();
		scripts.each((name, script) -> {
			writes.b(startScript);
			writes.str(name);
			writes.b(splitter);
			writes.str(script);
			writes.b(endScript);
		});
		writes.b(eof);
		writes.close();
	}
	
	public static void eachScript(Cons2<String, String> cons) {
		scripts.each(cons);
	}
	
	public static void saveScript(String name, String script) {
		scripts.put(name, script);
		save();
	}
	
	public static void deleteScript(String name) {
		scripts.remove(name);
		save();
	}
	
}