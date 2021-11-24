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

import newconsole.*;
import newconsole.ui.*;

public class FilePicker extends Dialog {
	
	/** File types that can be readen as text */
	public static Seq<String> readableExtensions = Seq.with("txt", "md", "properties");
	/** Files containing raw code */
	public static Seq<String> codeExtensions = Seq.with("js", "java", "kt", "json", "hjson", "gradle", "frag", "vert");
	/** Files that can be opened as images */
	public static Seq<String> imageExtensions = Seq.with("png", "jpg", "jpeg", "bmp" /*?*/);
	
	public InputPrompt inputPrompt;
	public ImageDialog imageDialog;
	
	public BetterPane mainPane;
	public Table filesTable;
	
	protected Fi currentDirectory;
	/** The last opened zip file. Used to return from zip file trees */
	protected Fi zipEntryPoint;
	
	public FilePicker() {
		super("");
		closeOnBack();
		setFillParent(true);
		
		inputPrompt = new InputPrompt();
		imageDialog = new ImageDialog();
		
		//todo: replace this with clickable buttons?
		cont.label(() -> {
			if (currentDirectory instanceof ZipFi) {
				return "[darkgrey]" + (zipEntryPoint != null ? zipEntryPoint.name() : "") + ": ZIP FILE ROOT[]" + currentDirectory.absolutePath();
			} else {
				return currentDirectory.absolutePath();
			}
		}).growX().get().setWrap(true);
		cont.row();
		
		cont.table(bar -> {
			bar.left().defaults().height(50f);
			
			bar.button(Icon.exit, Styles.nodei, this::hide).size(50f);
			
			bar.button("@newconsole.files.save-script", Styles.nodet, () -> {
				ifNotZip(() -> {
					inputPrompt.prompt("@newconsole.file-name", name -> {
						if (name.indexOf(".") == -1) {
							name = name + ".js"; //no extension - set to .js
						}
						
						String script = ConsoleVars.console.area.getText();
						var file = currentDirectory.child(name);
						if (!file.exists()) {
							file.mkdirs();
							file.writeString(script, false);
						} else {
							Vars.ui.showConfirm("@newconsole.file-override", () -> {
								file.writeString(script, false);
							});
						}
					});
				}
			}).width(250);
			
			bar.button("@newconsole.files.new-folder", Styles.nodet, () -> {
				ifNotZip(() -> {
					inputPrompt.prompt("@newconsole.folder-name", name -> {
						var dir = currentDirectory.child(name);
						if (dir.exists()) {
							Vars.ui.showInfo("@newconsole.already-exists");
						} else {
							dir.child("").mkdirs();
						}
					});
				})
			}).width(150);
		}).growX().row();
		
		//special entry that allows to go to the parent directory
		cont.table(entry -> {
			entry.setBackground(CStyles.filebg);
			entry.center().left().marginBottom(3f).defaults().pad(7f).height(50f);
			entry.touchable = Touchable.enabled;
			
			entry.image(CStyles.directory).size(50f).marginRight(10f).get().setColor(Color.gray);
			entry.add("@newconsole.files-up");
			
			entry.clicked(() -> {
				//special case for zip files
				if (currentDirectory.parent() == null && currentDirectory instanceof ZipFi && zipEntryPoint != null) {
					currentDirectory = zipEntryPoint; //return from the zip file
					zipEntryPoint = null;
				}
				
				//root & shared storage (android) directories may be unaccessible. This isn't a failproof way to check but whatsoever.
				if (currentDirectory.parent().list().length > 0) {
					openDirectory(currentDirectory.parent());
				} else { 
					Log.warn("Cannot access superdirectory " + currentDirectory.parent() + " (no permission?)");
					Vars.ui.showInfo("@newconsole-no-permission");
				}
			});
		}).growX().row();
		
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
	
	/** Returns whether the current directory is inside a zip file */
	public boolean isZipTree() {
		return zipEntryPoint != null && currentDirectory instanceof ZipFi;
	}
	
	/** Runs the runnable if the current directory is not in a zip tree, shows an info popup otherwise */
	protected void ifNotZip(Runnable run) {
		if (isZipTree()) {
			Vars.ui.showInfo("@newconsole.zip-not-permitted");
		} else {
			run.run()
		}
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
				openDirectory(it);
			} else {
				String ext = it.extension();
				if (ext.equals("zip") || ext.equals("jar")) {
					if (zipEntryPoint == null) {
						zipEntryPoint = file; //if it's not null, a zip file has been opened inside of another zip file
					}
					openDirectory(it);
				} else {
					if (readableExtensions.contains(ext) || codeExtensions.contains(ext)) {
						if (ConsoleVars.console != null) { //todo: is this check required?
							Vars.ui.showConfirm("newconsole.open-readable", () -> {
								ConsoleVars.console.area.setText(it.readString());
								hide();
							});
						}
					} else if (imageExtensions.contains(ext)) {
						imageDialog.showFor(it);
					} else {
						Vars.ui.showInfo("@newconsole.unknown-format");
					}
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
	
	/** An element representing a file or a directory */
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
					spinner.setBackground(CStyles.filebg);
					
					spinner.add("placeholder").row(); //todo: actions
					spinner.button("@newconsole.files-delete", Styles.nodet, () -> {
						Vars.ui.showInfo("not implemented");
					});
				})).width(100f);
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
				case "js": return CStyles.fileJs; //this is a js console, after all
				case "zip": return CStyles.fileZip;
				case "jar": return CStyles.fileJar;
				default: break;
			}
			if (readableExtensions.contains(ext)) return CStyles.fileText;
			if (codeExtensions.contains(ext)) return CStyles.fileCode;
			if (imageExtensions.contains(ext)) return CStyles.fileImage;
			return CStyles.fileAny;
		}
		
	}
	
	/** A dialog with a single image */
	public static class ImageDialog extends Dialog {
		
		public Label label;
		public Image image;
		
		public ImageDialog() {
			super("@newconsole.image-preview");
			closeOnBack();
			
			label = new Label("");
			image = new Image();
			
			cont.add(label).row();
			cont.add(image).row();
			cont.button("@newconsole.close", Styles.nodet, this::hide).fillX();
		}
		
		public void showFor(Fi file) {
			try {
				label.setText(file.name());
				image.setDrawable(new TextureRegion(new Texture(file)));
				show();
			} catch (ArcRuntimeException e) {
				Vars.ui.showException("@newconsole.image-corrupt", e);
			}
		}
		
	}
	
}