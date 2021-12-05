package newconsole.io;

import arc.*;
import arc.util.*;
import arc.func.*;
import arc.files.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.*;

//todo: it's behavior is really simmilar to behavior of ScriptsManager 
public class AutorunManager {
	
	public static final String save = "newconsole.autorun";
	
	public static Fi root;
	/** All default event classes */
	public static Seq<Class> allEvents = new Seq(50);
	/** All current events */
	public static Seq<AutorunEntry> events = new Seq(); 
	
	static {
		findEvents();
	}
	
	public static void init() {
		root = Vars.dataDirectory.child("saves");
		root.mkdirs();
		
		if (!root.exists() || !root.isDirectory()) {
			Log.err("Autorun manager failed to init");
			return;
		}
		
		if (loadSave(root.child(save))) {
			//normal save loaded
		} else if (loadSave(root.child(save + ".backup"))) {
			//normal save corrupt, backup is not
			root.child(save + ".backup").moveTo(root.child(save));
			save();
			Log.warn("Couldn't load autorun save, loaded backup");
		} else {
			Log.warn("Couldn't load autorun save");
		}
	}
	
	/** Loads events from the providen file. Returns whether the load was successful. */
	public static boolean loadSave(Fi file) {
		if (file == null || !file.exists()) return false;
		if (root == null) throw new IllegalStateException("AutorunManager hasn't been initialized yet");
		
		return true;
	}
	
	/** Saves the events into a file and creates a backup of the previous save */
	public static void save() {
		
	}
	
	public static AutorunEntry add(Class event, final String script) {
		var entry = new AutorunEntry(event, script);
		
		Cons<Object> cons = it -> {
			if (entry.enabled) {
				Log.info(Vars.mods.getScripts().runConsole(script));
			}
		};
		
		entry.cons = cons;
		Events.on(event, cons);
		events.add(entry);
		
		return entry;
	}
	
	public static void remove(String script) {
		remove(events.find(e -> e.script.equals(script)));
	}
	
	public static void remove(AutorunEntry entry) {
		Events.remove(entry.event, entry.cons);
		events.remove(entry);
	}
	
	/** Adds all default events to the seq */
	private static void findEvents() {
		var classes = EventType.class.getDeclaredClasses();
		for (var event : classes) {
			if (!event.isEnum()) {
				allEvents.add(event);
			}
		}
	}
	
	public static class AutorunEntry {
		
		public Class event;
		public String script;
		public Cons<Object> cons;
		public boolean enabled = true;
		
		public AutorunEntry(Class<Object> event, String script) {
			this.event = event;
			this.script = script;
		}
		
	}
	
}