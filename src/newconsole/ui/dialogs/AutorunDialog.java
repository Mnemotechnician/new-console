package newconsole.ui.dialogs;

import arc.scene.Action;
import arc.scene.Scene;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import newconsole.ConsoleVars;
import newconsole.io.AutorunManager;
import newconsole.ui.BetterPane;
import newconsole.ui.CStyles;
import newconsole.ui.CodeSpinner;
import newconsole.ui.Spinner;

/**
 * Allows the user to run specific scripts upon specific game events.
 */
public class AutorunDialog extends BaseDialog {

	public Class lastEvent = EventType.ClientLoadEvent.class;

	public Table list;
	public Spinner eventsSpinner;

	public AutorunDialog() {
		super("@newconsole.autorun-header");
		closeOnBack();

		cont.table(bar -> {
			bar.left();

			bar.button(Icon.exit, Styles.defaulti, this::hide).size(50f);
		}).growX().row();

		cont.stack(
			new Table(listRoot -> {
				listRoot.top().left().setFillParent(true);

				listRoot.add(new BetterPane(list -> {
					list.setBackground(CStyles.scriptbg);

					this.list = list;
				})).grow();
			}),

			new Table(addAutorun -> {
				addAutorun.top().left().setFillParent(true);

				addAutorun.add(new Spinner("@newconsole.add-event", false, panel -> {
					panel.setBackground(CStyles.scriptbg);

					panel.label(() -> lastEvent.getSimpleName()).growX().get().setColor(Pal.accent);
					panel.row();

					eventsSpinner = new Spinner("@newconsole.select-event", false, events -> {
						for (final var event : AutorunManager.allEvents) {
							var button = events.button(event.getSimpleName(), Styles.defaultt, () -> {
								lastEvent = event;

								eventsSpinner.hide(false);
							}).growX().get();

							events.row();
						}
					});
					panel.add(eventsSpinner).growX().marginBottom(10f).row();

					panel.button("@newconsole.save", Styles.defaultt, () -> {
						String code = ConsoleVars.console.area.getText();

						if (!code.isEmpty()) {
							AutorunManager.add(lastEvent, code);
							AutorunManager.save();
							rebuild();
						} else {
							Vars.ui.showInfo("@newconsole.empty-script");
						}
					}).growX();
				})).margin(4f).width(350f).with(it -> {
					it.setStyle(Styles.defaultt);
					it.unique = false;
				}).row();
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

			table.add("[accent]#" + list.getChildren().size).width(40f).padRight(10f);

			table.labelWrap("[darkgrey]" + entry.event.getSimpleName() + "->").width(200f);

			table.add(new CodeSpinner(entry.script)).growX();

			table.table(actions -> {
				actions.defaults().size(50f);

				TextButton toggle = new TextButton(entry.enabled ? "@newconsole.enabled" : "@newconsole.disabled", Styles.defaultt);
				actions.add(toggle).width(80f);
				//fuck java lambdas
				toggle.clicked(() -> {
					entry.enabled = !entry.enabled;

					toggle.setText(entry.enabled ? "@newconsole.enabled" : "@newconsole.disabled");
				});

				actions.button(CStyles.editIcon, Styles.defaulti, () -> {
					ConsoleVars.console.setCode(entry.script);
					hide();
				});

				actions.button(CStyles.deleteIcon, Styles.defaulti, () -> {
					Vars.ui.showConfirm("@newconsole.delete-confirm", () -> {
						AutorunManager.remove(entry);
						AutorunManager.save();
						rebuild();
					});
				});
			}).padLeft(20f);
		}).growX().pad(4f).marginBottom(5f).row();
	}

}
