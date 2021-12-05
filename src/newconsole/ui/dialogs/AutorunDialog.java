package newconsole.ui.dialogs;

import arc.struct.*;
import arc.util.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import newconsole.*;
import newconsole.ui.*;
import newconsole.io.*;

/** Allows the user to run specific scripts upon specific game events. */
public class AutorunDialog extends BaseDialog {
	
	public Class<Object> lastEvent = EventType.ClientLoadEvent.class;
	
	public Table list;
	
	public AutorunDialog() {
		super("@newconsole.autorun-header");
		closeOnBack();
		
		cont.table(bar -> {
			bar.button(Icon.exit, Styles.nodei, this::hide).size(50f);
		}).growX().row();
		
		cont.stack(
			new Table(addAutorun -> {
				addAutorun.top().left().setFillParent(true);
				
				addAutorun.add(new Spinner("@newconsole.add-event", false, panel -> {
					addAutorun.label(() -> lastEvent.getName()).growX().row();
					
					addAutorun.add(new Spinner("@newconsole.select-event", events -> {
						for (final var event : AutorunManager.allEvents) {
							var button = events.button(event.getName(), Styles.nodet, () -> {
								lastEvent = event;
							}).growX().get();
							
							button.setColor(event instanceof Enum ? Color.red : Color.blue);
							
							events.row();
						}
					})).growX().marginBottom(10f).row();
				})).row().width(300f);
				
				addAutorun.labelWrap("@newconsole.warn-trigger").visible(() -> lastEvent instanceof EventTypes.Trigger).growX().row();
				
				addAutorun.button("@newconsole.save", Styles.nodet, () -> {
					AutorunManager.add(lastEvent, ConsoleVars.console.area.getText());
					rebuild();
				});
			}),
			
			new Table(listRoot -> {
				listRoot.top().left().setFillParent(true);
				
				listRoot.add(new BetterPane(list -> {
					this.list = list;
				}));
			})
		).grow();
	}
	
	@Override
	public Dialog show(Scene stage, Action action) {
		rebuild();
		return super.show(stage, action);
	}
	
	public void rebuild() {
		list.clearChildren();
		for (var entry : AutorunManager.events) addEntry(entry);
	}
	
	public void addEntry(AutorunManager.AutorunEntry entry) {
		list.table(table -> {	
			table.center().left().setBackground(CStyles.scriptbg);
			
			table.add(String.valueOf(list.getChildren().size)).marginRight(10f);
			
			table.add(new Spinner("@newconsole.code-spinner", code -> {
				code.add(entry.script);
			})).growX().marginRight(20f);
			
			table.table(actions -> {
				actions.defaults().size(40f);
				
				actions.button(CStyles.deleteIcon, Styles.nodei, () -> {
					Vars.ui.showConfirm("@newconsole.delete-confirm", () -> {
						AutorunManager.remove(entry);
						rebuild();
					});
				});
			});
		}).growX().pad(4f).marginBottom(5f).row();
	}
	
}