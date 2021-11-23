package newconsole.ui.dialogs;

import arc.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import arc.func.*;
import arc.scene.*;
import arc.scene.style.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.files.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import newconsole.ui.*;

public class FilePicker extends Dialog {
	
	protected static Fi placeholderUp = new Fi(".. <go up>");
	/** File types that can be readen as text */
	public static Seq<String> readableExtensions = Seq.with("txt", "md");
	/** Files containing raw code */
	public static Seq<String> codeExtensions = Seq.with("js", "java", "kt", "json", "hjson", "gradle");
	/** Files that can be opened as images */
	public static Seq<String> imageExtensions = Seq.with("png", "jpg", "jpeg", "bmp" /*?*/);
	
	public BetterPane mainPane;
	public Table filesTable;
	
	protected Fi currentDirectory;
	/** The last opened zip file. Used to return from zip file trees */
	protected Fi zipEntryPoint;
	
	public FilePicker() {
		super("");
		closeOnBack();
		setFillParent(true);
		
		//todo: replace this with clickable buttons?
		cont.label(() -> {
			if (currentDirectory instanceof ZipFi) {
				(zipEntryPoint != null ? zipEntryPoint.name() : "") + ": ZIP FILE ROOT" + currentDirectory.absolutePath();
			} else {
				currentDirectory.absolutePath();
			}
		}).row();
		
		cont.table(bar -> {
			bar.left();
			bar.button(Icon.exit, Styles.nodei, this::hide).size(50f).row();
			bar.button("@newconsole.files.save-script", Styles.nodet, () -> {
				
			});
		}).growX();
		
		//special entry: button that allows to go to the parent directory
		cont.add(new FileEntry(placeholderUp, it -> {
			//special case for zip files
			if (currentDirectory.parent() == null && currentDirectory instanceof ZipFi && zipEntryPoint != null) {
				currentDirectory = zipEntryPoint; //return from the zip file
				zipEntryPoint = null;
			}
			
			//root & shared storage (android) directories may be unaccessible. This isn't a failproof way to check but whatsoever.
			if (currentDirectory.parent().list().length > 0) {
				openDirectory(currentDirectory.parent());
			} else { 
				Log.warn("Cannot access superdirectory " + currentDirectory.parent());
				Vars.ui.showInfo("@newconsole-no-permission");
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
				String ext = it.extension();
				if (ext.equals("zip") || ext.equals("jar")) {
					if (zipEntryPoint == null) {
						zipEntryPoint = file; //if it's not null, a zip file has been opened inside of another zip file
					}
					openDirectory(it);
				} else {
					Vars.ui.showInfo("not implemented");
				}
			}
		})).growX();
	}
	
	public void openDirectory(Fi file) {
		if (file == null || !file.exists()) {
			Log.warn("Attempt to open an inexistent directory. Ignored.");
			return;
		}
		
		if (!file.isDirectory()) {
			currentDirectory = new ZipFi(file);
		} else {
			currentDirectory = file;
		}
		rebuild();
	}
	
	public static class FileEntry extends Table {
		
		public Fi file;
		
		public FileEntry(Fi file, Cons<Fi> onclick) {
			this.file = file;
			
			setBackground(CStyles.filebg);
			center().left().marginBottom(3f).defaults().pad(7f).height(50f);
			touchable = Touchable.enabled;
			
			var image = image(pickIcon(file)).size(50f).marginRight(10f).get();
			image.setColor(file.name().startsWith(".") ? Color.gray : file.isDirectory() ? CStyles.accent : Color.white);
			
			add(file.name());
			
			table(right -> {
				right.right();
				right.add(new Spinner("@newconsole.actions", spinner -> {
					spinner.add("placeholder"); //todo: actions
					spinner.button("@newconsole.files-delete", Styles.nodet, () -> {
						Vars.ui.showInfo("not implemented");
					});
				});
			}).growX();
			
			clicked(() -> {
				onclick.get(file);
			});
		}
		
		public static TextureRegion pickIcon(Fi file) {
			if (file == null || file.isDirectory()) {
				return CStyles.directory;
			}
			String ext = file.extension();
			switch (ext) {
				case "js" -> return CStyles.fileJs;
				case "zip" -> return CStyles.fileZip;
				case "jar" -> return CStyles.fileJar;
				default -> break;
			}
			if (readableExtensions.contains(ext)) return CStyles.fileText;
			if (codeExtensions.contains(ext)) return CStyles.fileCode;
			if (imageExtensions.contains(ext)) return CStyles.fileImage;
		}
		
	}
	
}