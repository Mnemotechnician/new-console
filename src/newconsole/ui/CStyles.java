package newconsole.ui;

import arc.*;
import arc.util.*;
import arc.graphics.*;
import arc.scene.style.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.graphics.*;

public class CStyles {
	
	public static Drawable scriptbg,
	playIcon, editIcon, deleteIcon,
	
	filebg;
	
	public static TextureRegion directory, fileAny, fileText, fileJs, fileZip;
	
	public static void load() {
		scriptbg = Tex.buttonOver;
		
		playIcon = Icon.play.tint(Color.green);
		editIcon = Icon.edit.tint(Color.yellow);
		deleteIcon = Icon.trash.tint(Color.red);
		
		filebg = ((ScaledNinePatchDrawable) Styles.flatDown).tint(Color.valueOf("2244ff"));
		
		directory = Core.atlas.find("newconsole-directory");
		fileAny = Core.atlas.find("newconsole-file-unknown");
		fileText = Core.atlas.find("newconsole-file-text");
		fileJs = Core.atlas.find("newconsole-file-js");
		fileZip = Core.atlas.find("newconsole-file-zip");
	}
}