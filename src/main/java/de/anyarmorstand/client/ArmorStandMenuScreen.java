package de.anyarmorstand.client;

import de.anyarmorstand.SelectItemPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Menü mit allen Items, die normalerweise nicht in einen Armor Stand passen.
 * Ein Klick auf ein Item schickt die Auswahl an den Server und schließt das Menü.
 */
public class ArmorStandMenuScreen extends Screen {

	private static final int COLUMNS = 9;
	private static final int ROWS = 5;
	private static final int PER_PAGE = COLUMNS * ROWS;
	private static final int CELL = 22;

	private static List<ItemStack> allItems;

	private final int standId;
	private final List<ButtonWidget> cells = new ArrayList<>();
	private List<ItemStack> filtered = List.of();
	private TextFieldWidget search;
	private ButtonWidget prevButton;
	private ButtonWidget nextButton;
	private String query = "";
	private int page = 0;
	private int pages = 1;
	private int gridTop;

	public ArmorStandMenuScreen(int standId) {
		super(Text.literal("Item für den Armor Stand wählen"));
		this.standId = standId;
	}

	private static List<ItemStack> allSelectableItems() {
		if (allItems == null) {
			List<ItemStack> list = new ArrayList<>();
			for (Item item : Registries.ITEM) {
				if (item == Items.AIR) {
					continue;
				}
				ItemStack stack = item.getDefaultStack();
				// Items mit eigenem Ausrüstungs-Slot (Rüstung usw.) passen schon normal.
				if (stack.get(DataComponentTypes.EQUIPPABLE) != null) {
					continue;
				}
				list.add(stack);
			}
			allItems = list;
		}
		return allItems;
	}

	@Override
	protected void init() {
		cells.clear();

		int gridWidth = COLUMNS * CELL;
		int left = (width - gridWidth) / 2;
		gridTop = 56;

		search = new TextFieldWidget(textRenderer, left, 28, gridWidth - 2, 18, Text.literal("Suche"));
		search.setPlaceholder(Text.literal("Item suchen ..."));
		search.setText(query);
		search.setChangedListener(text -> {
			query = text;
			page = 0;
			refresh();
		});
		addDrawableChild(search);
		setInitialFocus(search);

		for (int i = 0; i < PER_PAGE; i++) {
			final int slot = i;
			int col = i % COLUMNS;
			int row = i / COLUMNS;
			ButtonWidget cell = ButtonWidget.builder(Text.empty(), button -> choose(slot))
					.dimensions(left + col * CELL, gridTop + row * CELL, CELL - 2, CELL - 2)
					.build();
			cells.add(cell);
			addDrawableChild(cell);
		}

		int bottom = gridTop + ROWS * CELL + 6;
		prevButton = ButtonWidget.builder(Text.literal("<"), button -> changePage(-1))
				.dimensions(left, bottom, 40, 20).build();
		nextButton = ButtonWidget.builder(Text.literal(">"), button -> changePage(1))
				.dimensions(left + gridWidth - 42, bottom, 40, 20).build();
		addDrawableChild(prevButton);
		addDrawableChild(nextButton);
		addDrawableChild(ButtonWidget.builder(Text.literal("Schließen"), button -> close())
				.dimensions(width / 2 - 40, bottom, 80, 20).build());

		refresh();
	}

	private void changePage(int delta) {
		page = Math.max(0, Math.min(pages - 1, page + delta));
		refresh();
	}

	private void refresh() {
		String needle = query.trim().toLowerCase(Locale.ROOT);
		List<ItemStack> result = new ArrayList<>();
		for (ItemStack stack : allSelectableItems()) {
			if (needle.isEmpty()
					|| stack.getName().getString().toLowerCase(Locale.ROOT).contains(needle)
					|| Registries.ITEM.getId(stack.getItem()).getPath().contains(needle)) {
				result.add(stack);
			}
		}
		filtered = result;
		pages = Math.max(1, (filtered.size() + PER_PAGE - 1) / PER_PAGE);
		page = Math.max(0, Math.min(page, pages - 1));

		for (int i = 0; i < cells.size(); i++) {
			ButtonWidget cell = cells.get(i);
			int index = page * PER_PAGE + i;
			if (index < filtered.size()) {
				cell.visible = true;
				cell.setTooltip(Tooltip.of(filtered.get(index).getName()));
			} else {
				cell.visible = false;
				cell.setTooltip(null);
			}
		}
		prevButton.active = page > 0;
		nextButton.active = page < pages - 1;
	}

	private void choose(int slot) {
		int index = page * PER_PAGE + slot;
		if (index >= filtered.size()) {
			return;
		}
		Item item = filtered.get(index).getItem();
		ClientPlayNetworking.send(new SelectItemPayload(standId, Registries.ITEM.getId(item)));
		close();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);

		for (int i = 0; i < cells.size(); i++) {
			ButtonWidget cell = cells.get(i);
			int index = page * PER_PAGE + i;
			if (cell.visible && index < filtered.size()) {
				context.drawItem(filtered.get(index), cell.getX() + 2, cell.getY() + 2);
			}
		}

		context.drawCenteredTextWithShadow(textRenderer,
				Text.literal((page + 1) + " / " + pages),
				width / 2, gridTop + ROWS * CELL - 6, 0xFFAAAAAA);
	}
}
