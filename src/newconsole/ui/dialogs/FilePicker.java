package newconsole.ui.dialogs;

import arc.*;
import arc.util.*;
import arc.struct.*;
import arc.func.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout;
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
		
		mainPane = new BetterPane(t -> {
			filesTable = t;
		});
		cont.add(mainPane).growX();
		
		rebuild();
	}
	
	public void rebuild() {
		if (file == null || !file.exists()) {
			file = Vars.dataDirectory;
		}
		
		//special case: button that allows to go to the parent directory
		filesTable.add(new FileEntry(placeholderUp, it -> openDirectory(it.parent()))).growX();
		
		for (Fi file : currentDirectory) {
			filesTable.add(new FileEntry(file, it -> {
				if (it.isDirectory()) {
					openFile(file);
				} else {
					Vars.ui.showInfo("Not implemented");
				}
			}));
		}
	}
	
	public void openDirectory(Fi file) {
		if (file == null || !file.exists()) {
			Log.warn("Attempt to open an inexistent directory. Ignored.");
			return;
		}
		
		currentDirectory = file;
		rebuild();
	}
	
	public static class FileEntry extends Table {
		
		public FileEntry(Fi file, Cons<File> onclick) {
			setBackground(CStyles.filebg);
			marginBottom(3f);
			
			left();
			defaults().pad(7f).height(50f);
			
			image(pickIcon(file)).size(50f).marginRight(10f);
			add(file.name());
			
			table(right -> {
				right();
				add("placeholder"); //todo: actions
			}).growX();
			
			clicked(() -> {
				onClick.get(file);
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
			}
		}
		
	}
	
}