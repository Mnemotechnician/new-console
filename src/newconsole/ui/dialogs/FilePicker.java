package newconsole.ui.dialogs;

import arc.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.func.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.files.*;
import mindustry.*;
import mindustry.graphics.*;

import newconsole.ui.*;

public class FilePicker extends Dialog {
	
	static Fi placeholderUp = new Fi(".. go up");
	
	public BetterPane mainPane;
	public Table filesTable;
	
	Fi currentDirectory;
	
	public FilePicker() {
		super("");
		closeOnBack();
		setFillParent(true);
		
		//special case: button that allows to go to the parent directory
		cont.add(new FileEntry(placeholderUp, it -> {
			var lastDirectory = currentDirectory;
			
			//root directories may be unaccessible. This isn't a failproof way to check but whatsoever.
			if (currentDirectory.parent().list().length > 0) {
				openDirectory(currentDirectory.parent());
			} else { 
				Log.warn("Cannot access superdirectory " + currentDirectory.parent());
			}
		})).growX().row();
		
		mainPane = new BetterPane(t -> {
			filesTable = t;
		});
		cont.add(mainPane).grow();
	}
	
	@Override
	public Dialog show(Scene stage, Action action) {
		rebuild();
		return super.show(stage, action);
	}
	
	public void rebuild() {
		if (currentDirectory == null || !currentDirectory.exists()) {
			currentDirectory = Vars.dataDirectory;
		}
		
		filesTable.clear();
		var list = currentDirectory.list();
		//first run: add subdirectories
		for (Fi file : list) {
			if (file.isDirectory()) buildFile(file);
		}
		//second: add files
		for (Fi file : list) {
			if (!file.isDirectory()) buildFile(file);
		}
	}
	
	public void buildFile(Fi file) {
		filesTable.row();
		filesTable.add(new FileEntry(file, it -> {
			if (it.isDirectory()) {
				openDirectory(file);
			} else {
				Vars.ui.showInfo("Not implemented");
			}
		})).growX();
	}
	
	public void openDirectory(Fi file) {
		if (file == null || !file.exists()) {
			Log.warn("Attempt to open an inexistent directory. Ignored.");
			return;
		}
		
		if (file.extension().equals("zip"))
		currentDirectory = file;
		rebuild();
	}
	
	public static class FileEntry extends Table {
		
		public Fi file;
		
		public FileEntry(Fi file, Cons<Fi> onclick) {
			this.file = file;
			
			setBackground(CStyles.filebg);
			setColor(CStyles.accent);
			left().marginBottom(3f).defaults().pad(7f).height(50f);
			
			image(pickIcon(file)).size(50f).marginRight(10f);
			add(file.name());
			
			table(right -> {
				right();
				add("placeholder"); //todo: actions
			}).growX();
			
			clicked(() -> {
				onclick.get(file);
			});
		}
		
		public static TextureRegion pickIcon(Fi file) {
			if (file == null || file.isDirectory()) {
				return CStyles.directory;
			}
			
			return switch (file.extension()) {
				case "txt" -> CStyles.fileText;
				case "js" -> CStyles.fileJs;
				case "zip" -> CStyles.fileZip;
				default -> CStyles.fileAny;
			};
		}
		
	}
	
}