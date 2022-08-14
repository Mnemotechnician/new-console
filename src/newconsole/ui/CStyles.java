package newconsole.ui;

import arc.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.freetype.*;
import arc.freetype.FreeTypeFontGenerator.*;
import arc.freetype.FreetypeFontLoader.*;
import arc.scene.style.*;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.TextField.TextFieldStyle;
import mindustry.Vars;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.graphics.*;

import static arc.Core.*;

public class CStyles {
	public static Drawable 
		scriptbg, filebg,
		playIcon, editIcon, deleteIcon;
	
	public static TextureRegion 
		directory,
		fileAny,
		fileText, fileJs, fileCode, fileImage,
		fileZip, fileJar;

	public static Font mono;
	public static LabelStyle monoLabel;
	public static TextFieldStyle monoArea;
	
	public static Color accent = Color.valueOf("2244ff");

	public static void loadSync() {
		mono = new FreeTypeFontGenerator(Vars.tree.get("fonts/JetBrainsMono-medium.ttf")).generateFont(new FreeTypeFontParameter() {{
			size = 15;
			incremental = true;
		}});
		mono.getData().markupEnabled = true;

		scriptbg = Tex.button;
		
		playIcon = Icon.play.tint(Color.green);
		editIcon = Icon.edit.tint(Color.yellow);
		deleteIcon = Icon.trash.tint(Color.red);
		
		filebg = ((ScaledNinePatchDrawable) Styles.flatDown).tint(accent);
		
		directory = atlas.find("newconsole-folder");
		fileAny = atlas.find("newconsole-file-unknown");
		fileText = atlas.find("newconsole-file-text");
		fileJs = atlas.find("newconsole-file-js");
		fileZip = atlas.find("newconsole-file-zip");
		fileJar = atlas.find("newconsole-file-jar");
		fileCode = atlas.find("newconsole-file-code");
		fileImage = atlas.find("newconsole-file-image");

		monoLabel = new LabelStyle(Styles.defaultLabel) {{
			font = mono;
		}};
		monoArea = new TextFieldStyle(Styles.defaultField) {{
			font = mono;
			messageFont = mono;
		}};
	}
}
