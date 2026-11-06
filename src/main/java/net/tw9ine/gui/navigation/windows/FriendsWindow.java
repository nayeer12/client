/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.windows;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.tw9ine.Tw9ine;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.components.ButtonComponent;
import net.tw9ine.gui.components.ComboBoxComponent;
import net.tw9ine.gui.components.GridComponent;
import net.tw9ine.gui.components.ItemsComponent;
import net.tw9ine.gui.components.PanelComponent;
import net.tw9ine.gui.components.ScrollComponent;
import net.tw9ine.gui.components.SeparatorComponent;
import net.tw9ine.gui.components.StackPanelComponent;
import net.tw9ine.gui.components.StringComponent;
import net.tw9ine.gui.font.FontManager;
import net.tw9ine.gui.navigation.Window;
import net.tw9ine.gui.types.GridDefinition;
import net.tw9ine.gui.types.TextAlign;
import net.tw9ine.gui.types.Thickness;
import net.tw9ine.gui.types.VerticalAlignment;
import net.tw9ine.gui.types.GridDefinition.RelativeUnit;
import net.tw9ine.settings.friends.Friend;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

public class FriendsWindow extends Window  {
	private final ComboBoxComponent playerComboBox;

	public FriendsWindow() {
		super("Friends", 360, 825);
		setProperty(UIElement.MinWidthProperty, 300f);
		setProperty(UIElement.MinHeightProperty, 300f);
		StackPanelComponent stackPanel = new StackPanelComponent();
		stackPanel.setSpacing(8f);

		StringComponent headerText = new StringComponent("Friends");
		headerText.setProperty(UIElement.FontWeightProperty, FontManager.WEIGHT_BOLD);
		headerText.bindProperty(ForegroundProperty, GuiManager.foregroundHeaderColor);

		stackPanel.addChild(headerText);
		stackPanel.addChild(new SeparatorComponent());
		stackPanel.setLastChildFill(true);

		StringComponent label = new StringComponent("Friends will be excluded from combat-related modules.");
		label.bindProperty(ForegroundProperty, GuiManager.foregroundAccentColor);
		stackPanel.addChild(label);

		stackPanel.addChild(new SeparatorComponent());

		// Add friend combo box
		StringComponent addFriendLabel = new StringComponent("Add Friends");
		addFriendLabel.bindProperty(ForegroundProperty, GuiManager.foregroundAccentColor);
		stackPanel.addChild(addFriendLabel);

		playerComboBox = new ComboBoxComponent();
		playerComboBox.setProperty(ComboBoxComponent.PlaceholderTextProperty, "Select a player...");
		playerComboBox.setOnItemChanged(value -> {
			if (value == null)
				return;

			if(value instanceof String playerName) {
				ClientPacketListener connection = Minecraft.getInstance().getConnection();
				if (connection != null) {
					PlayerInfo playerInfo = connection.getPlayerInfo(playerName);
					if (playerInfo != null) {
						Tw9ine.getInstance().friendsList.addFriend(playerName, playerInfo.getProfile().id());
					}
				}
			}
			
			playerComboBox.setProperty(ComboBoxComponent.SelectedItemProperty, null);
		});
		stackPanel.addChild(playerComboBox);

		stackPanel.addChild(new SeparatorComponent());

		StringComponent friendsLabel = new StringComponent("Friends");
		friendsLabel.bindProperty(ForegroundProperty, GuiManager.foregroundAccentColor);
		stackPanel.addChild(friendsLabel);

		Function<Friend, UIElement> friendItemFactory = (friend -> {

			GridComponent grid = new GridComponent();
			grid.addColumnDefinition(new GridDefinition(1, RelativeUnit.Relative));
			grid.addColumnDefinition(new GridDefinition(1, RelativeUnit.Auto));
			grid.setProperty(GridComponent.HorizontalSpacingProperty, 8f);
			grid.setProperty(UIElement.MarginProperty, new Thickness(4f));

			StringComponent text = new StringComponent(friend.getUsername());
			text.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
			text.setProperty(UIElement.IsHitTestVisibleProperty, true);
			grid.addChild(text);

			// Delete Button
			ButtonComponent removeButton = new ButtonComponent(() -> {
				TW9INE.friendsList.removeFriend(friend);
			});

			StringComponent removeString = new StringComponent("Unfriend");
			removeString.setProperty(StringComponent.TextAlignmentProperty, TextAlign.Center);
			removeString.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
			removeButton.setContent(removeString);
			grid.addChild(removeButton);
			return grid;
		});

		Supplier<PanelComponent> friendListParentSupplier = () -> new StackPanelComponent();

		ItemsComponent<Friend> friendsList = new ItemsComponent<Friend>(Tw9ine.getInstance().friendsList.getFriends(), friendListParentSupplier, friendItemFactory);

		ScrollComponent friendScroll = new ScrollComponent();
		friendScroll.setProperty(UIElement.MarginProperty, new Thickness(4f));
		friendScroll.setContent(friendsList);
		stackPanel.addChild(friendScroll);

		setContent(stackPanel);
	}

	private int lastPlayerCount = -1;

	@Override
	public void update() {
		super.update();

		ClientPacketListener connection = Minecraft.getInstance().getConnection();
		if (connection == null)
			return;

		int currentCount = connection.getOnlinePlayers().size();
		if (currentCount != lastPlayerCount) {
			lastPlayerCount = currentCount;
			refreshPlayerList(connection);
		}
	}

	private void refreshPlayerList(ClientPacketListener connection) {
		String localName = Minecraft.getInstance().getUser().getName();
		List<String> playerNames = new ArrayList<>();
		for (PlayerInfo info : connection.getOnlinePlayers()) {
			String name = info.getProfile().name();
			if (!name.equals(localName) && !Tw9ine.getInstance().friendsList.contains(info.getProfile().id()))
				playerNames.add(name);
		}

		playerComboBox.setProperty(ComboBoxComponent.ItemsSourceProperty, playerNames);
	}
}
