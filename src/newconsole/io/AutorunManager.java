package newconsole.io;

import arc.*;
import arc.util.*;
import arc.func.*;
import arc.files.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.*;

public class AutorunManager {
	
	/** All default event classes */
	public static Seq<Class> allEvents = new Seq(50);
	/** All current events */
	public static Seq<AutorunEntry> events = new Seq(); 
	
	static {
		findEvents();
	}
	
	public static void init() {
		//load();
	}
	
	public static boolean load(Fi file) {
		return false;
	}
	
	public static void save() {
		
	}
	
	public static AutorunEntry add(Class event, final String script) {
		Cons<Object> cons = it -> {
			Log.info(Vars.mods.getScripts().runConsole(script));
		};
		
		var entry = new AutorunEntry(event, script, cons);
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
		
		var triggers = EventType.Trigger.class.getEnumConstants();
		for (var trigger : triggers) {
			allEvents.add(trigger.getClass());
		}
	}
	
	public static class AutorunEntry {
		
		public Class event;
		public String script;
		public Cons<Object> cons;
		
		public AutorunEntry(Class<Object> event, String script, Cons<Object> cons) {
			this.event = event;
			this.script = script;
			this.cons = cons;
		}
		
	}
	
}