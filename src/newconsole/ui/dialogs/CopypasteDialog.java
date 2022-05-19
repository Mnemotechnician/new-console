package newconsole.ui.dialogs;

import arc.*;
import arc.util.*;
import arc.scene.ui.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import newconsole.*;

/** Allows the user to copy text from a text field/area, or paste into it, overriding the original text */
public class CopypasteDialog extends BaseDialog {
	
	TextField target;
	
	public CopypasteDialog() {
		super("@newconsole.copypaste-header");
		closeOnBack();
		
		cont.center();
		cont.table(main -> {
			main.defaults().height(50f);
			main.button("@newconsole.copy", Styles.logict, () -> {
				if (target == null) {
					Log.warn("No target text area specified for CopypasteDialog");
					return;
				}
				
				String script = target.getText();
				Core.app.setClipboardText(script);
				hide();
			}).width(150);
			
			main.button("@newconsole.paste", Styles.logict, () -> {
				if (target == null) {
					Log.warn("No target text area specified for CopypasteDialog");
					return;
				}
				
				String script = Core.app.getClipboardText();
				if (script != null && !script.equals("")) {
					Vars.ui.showConfirm("@newconsole.warn-override", () -> target.setText(script));
				} else {
					Vars.ui.showInfo("@newconsole.clipboard-empty");
				}
				hide();
			}).width(150).row();
			
			main.button("@newconsole.close", Styles.logict, this::hide).colspan(2).growX();
		});
	}
	
	public CopypasteDialog setTarget(TextField target) {
		this.target = target;
		return this;
	}
	
}
