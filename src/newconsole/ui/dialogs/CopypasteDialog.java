package newconsole.ui.dialogs;

import arc.*;
import arc.scene.ui.*;
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
			main.button("@newconsole.copy", Styles.nodet, () -> {
				if (target == null) {
					Log.warn("No target text area specified for CopypasteDialog");
					return;
				}
				
				String script = target.getText();
				Core.app.setClipboardText(script);
			}).width(100);
			
			main.button("@newconsole.paste", Styles.nodet, () -> {
				if (target == null) {
					Log.warn("No target text area specified for CopypasteDialog");
					return;
				}
				
				String script = Core.app.getClipboardText();
				target.setText(script);
			}).width(100).row();
			
			main.button("@newconsole.close", Styles.nodet, this::hide).colspan(2).growX();
		});
	}
	
	public CopypasteDialog setTarget(TextArea target) {
		this.target = target;
		return this;
	}
	
}