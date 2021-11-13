package newconsole.ui;

import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*!

public class CStyles {
	
	public static Drawable scriptbg;
	
	public static void load() {
		var whiteui = (TextureRegionDrawable) Tex.whiteui;
		
		Tmp.c1.set(Pal.accent).a = 0.2;
		scriptbg = whiteui.tint(Tmp.c1);
	}
}