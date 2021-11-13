package newconsole.ui;

import arc.util.*;
import arc.graphics.*;
import arc.scene.style.*;
import mindustry.gen.*;
import mindustry.graphics.*;

public class CStyles {
	
	public static Drawable scriptbg,
	playIcon, editIcon, deleteIcon;
	
	public static void load() {
		scriptbg = Tex.buttonOver;
		
		playIcon = Icon.play.tint(Color.green);
		editIcon = Icon.edit.tint(Color.yellow);
		deleteIcon = Icon.trash.tint(Color.red);
	}
}