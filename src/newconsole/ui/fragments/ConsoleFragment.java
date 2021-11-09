package newconsole.ui.fragments;

import arc.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.ui.fragments.*;

public class ConsoleFragment extends Fragment {
	
	public boolean shown = false;
	
	@Override
	public void build(Group parent) {
		parent.fill(root -> {
			//todo
		}).visible(() -> shown);
	}
	
}