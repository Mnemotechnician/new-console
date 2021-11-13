package newconsole.ui;

import arc.func.*;
import arc.struct.*;
import arc.files.*;
import mindustry.*;

public class ScriptsManager {
	
	public static final String save = "newconsole.save", def = "assets/console/default.save";
	public static final byte startScript = 3, endScript = 4, splitter = 17;
	
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
			//loaded backup save
		} else if (loadSave(Vars.tree.get(def))) {
			Log.info("loaded default scripts");
		} else {
			Log.err("Couldn't load saved nor default scripts!");
		}
	}
	
	public static boolean loadSave(Fi save) {
		if (!save.exists()) return false;
		if (root == null) throw new IllegalStateException("ScriptsManager hasn't been initialized yet");
		
		try {
			var stream = file.read();
			//read scripts
			int b;
			while ((b = stream.read()) != -1) {
				if (b == startScript) {
					//read name
					build.setLength(0);
					while ((b = stream.read()) != splitter && b != -1) {
						if (b == endScript) continue;
						build.append((char) b);
					}
					String name = build.toString();
					
					//read script
					build.setLength(0);
					while ((b = stream.read()) != endScript && b != -1) {
						build.append((char) b);
					}
					String script = build.toString();
					
					scripts.put(name, script);
				}
			}
			return true;
		} catch (Exception e) {
			Log.warn(e.toString());
			return false;
		}
	}
	
	/** Save scripts & create a backup */
	public static void save() {
		Fi savef = root.child(save);
		//backup
		if (savef.exists()) {
			savef.moveTo(root.child(save + ".backup"));
		}
		//save
		var writes = savef.writes();
		scripts.each((name, script) -> {
			writes.b(startScript);
			writes.str(name);
			writes.b(splitter);
			writes.str(script);
			writes.b(endScript);
		});
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