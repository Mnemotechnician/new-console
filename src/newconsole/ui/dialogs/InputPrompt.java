package newconsole.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.ui.*;

/** A text input dialog */
public class InputPrompt extends Dialog {
	
	public Label label;
	public TextField field;
	
	protected Cons<String> onFinish;
	
	public InputPrompt() {
		super("");
		closeOnBack();
		cont.center();
		
		label = new Label("");
		label.setWrap(true);
		cont.add(label).growX().row();
		
		field = cont.field("", (field, letter) -> {
			if (letter == '\n') done();
			return true;
		}, text -> {}).width(200f).get();
		field.removeInputDialog();
		
		cont.button("@newconsole.done", Styles.nodet, this::done).width(80f).row();
		
		cont.button("@newconsole.close", Styles.nodet, this::hide).colspan(2).growX();
	}
	
	/** Shows the dialog, runs the consumer when the done button is pressed */
	public void prompt(String label, String defaultText, Cons<String> cons) {
		this.onFinish = cons;
		
		label.setText(label == null ? "" : label);
		field.setText(defaultText);
		
		show();
	}
	
	public void prompt(String label, Cons<String> cons) {
		prompt(label, "", cons);
	}
	
	protected void done() {
		if (onFinish != null) {
			String text = field.getText();
			if (text.replaceAll("\\s", "").equals("")) {
				Vars.ui.showInfo("@newconsole.empty-field");
			} else {
				onFinish.get(text);
				onFinish = null;
			}
		}
		hide();
	}
	
}