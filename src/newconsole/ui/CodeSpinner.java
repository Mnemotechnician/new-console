package newconsole.ui;

import arc.scene.ui.Label;
import arc.util.Strings;

/**
 * Displays arbitrary code in a code block.
 */
public class CodeSpinner extends Spinner {

	public String code;
	public Label codeLabel;

	public CodeSpinner(String code) {
		super("@newconsole.code-spinner", null);

		table.setBackground(CStyles.scriptbg);

		codeLabel = table.add("", CStyles.monoLabel).with(it -> it.setWrap(true)).grow().get();

		this.code = Strings.stripColors(code);
	}

	@Override
	public void show(boolean animate) {
		codeLabel.setText(code);
		super.show(animate);
	}

}
