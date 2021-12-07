package newconsole.ui;

import arc.util.*;
import arc.scene.ui.*;
import mindustry.ui.*;
import newconsole.ui.*;

/** Displays arbitrary code in a code block. Provides some optimisations. */
public class CodeSpinner extends Spinner {
	
	public String code;
	public Label codeLabel;
	
	public CodeSpinner(String code) {
		super("@newconsole.code-spinner", Styles.togglet, code -> {
			code.setBackground(CStyles.scriptbg);
			
			code = code.add("").get();
		});
		
		this.code = Strings.stripColors(code);
	}
	
	@Override
	public void show(boolean animate) {
		codeLabel.setText(code);
	}
	
}