package net.tw9ine.gui.components;

import java.util.function.Consumer;
import com.mojang.blaze3d.platform.InputConstants;
import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.event.events.KeyDownEvent;
import net.tw9ine.event.events.MouseClickEvent;
import net.tw9ine.event.listeners.KeyDownListener;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.UIProperty;
import net.tw9ine.gui.colors.Colors;
import net.tw9ine.gui.types.TextWrapping;
import net.tw9ine.gui.types.Thickness;
import net.tw9ine.gui.types.VerticalAlignment;
import net.tw9ine.utils.input.CursorStyle;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.rendering.shaders.Shader;
import net.tw9ine.utils.types.MouseAction;
import net.tw9ine.utils.types.MouseButton;

public class TextBoxComponent extends Component implements KeyDownListener {
	private boolean listeningForKey;

	private static final Shader CARET_SHADER = Shader.solid(Colors.White);
	private static final Shader PLACEHOLDER_SHADER = Shader.solid(Colors.Gray);
	public static UIProperty<String> HeaderProperty = new UIProperty<String>("Header", "", false, true);
	public static UIProperty<String> TextProperty = new UIProperty<String>("Text", "", false, true,
			TextBoxComponent::onTextPropertyChanged);
	public static UIProperty<String> PlaceholderText = new UIProperty<String>("PlaceholderText", "", false, true,
			TextBoxComponent::onPlaceholderPropertyChanged);
	public static final UIProperty<Float> FontSizeProperty = new UIProperty<>("FontSize", 12f, true, true,
			TextBoxComponent::onFontSizePropertyChanged);

	private boolean isFocused = false;
	private int caretTick = 0;
	private boolean caretVisible = true;
	private boolean isAllSelected = false;
	private int backspaceDelayTimer = 0;

	private final RectangleComponent box;
	private final StringComponent textComponent;

	private Consumer<String> onTextChanged;

	private static void onTextPropertyChanged(UIElement sender, String oldValue, String newValue) {
		if (sender instanceof TextBoxComponent textBoxComponent) {
			textBoxComponent.refreshDisplayedText();
			if (textBoxComponent.onTextChanged != null)
				textBoxComponent.onTextChanged.accept(newValue);
		}
	}

	private static void onPlaceholderPropertyChanged(UIElement sender, String oldValue, String newValue) {
		if (sender instanceof TextBoxComponent textBoxComponent) {
			textBoxComponent.refreshDisplayedText();
		}
	}

	private static void onFontSizePropertyChanged(UIElement sender, Float oldValue, Float newValue) {
		if (sender instanceof TextBoxComponent textBoxComponent) {
			textBoxComponent.textComponent.setProperty(StringComponent.FontSizeProperty, newValue);
		}
	}

	public TextBoxComponent() {
		setProperty(UIElement.CursorProperty, CursorStyle.Type);
		bindProperty(UIElement.BackgroundProperty, GuiManager.componentBackgroundColor);
		bindProperty(UIElement.BorderProperty, GuiManager.componentBorderColor);
		bindProperty(UIElement.CornerRadiusProperty, GuiManager.roundingRadius);

		box = new RectangleComponent();
		box.setProperty(UIElement.PaddingProperty, new Thickness(4f));

		textComponent = new StringComponent(getProperty(TextProperty));
		textComponent.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
		textComponent.setProperty(StringComponent.TextWrappingProperty, TextWrapping.NoWrap);
		textComponent.setProperty(UIElement.IsHitTestVisibleProperty, false);
		box.setContent(textComponent);

		setContent(box);

		refreshDisplayedText();

		setOnClicked(e -> {
			if (e.button == MouseButton.LEFT && e.action == MouseAction.DOWN) {
				if (!listeningForKey) {
					setListeningForKey(true);
				}
				e.cancel();
			}
		});
	}

	@Override
	public void update() {
		super.update();
		if (isFocused) {
			caretTick++;
			if (caretTick >= 10) {
				caretVisible = !caretVisible;
				caretTick = 0;
			}

			// Handle continuous backspace deletion when held down
			if (InputConstants.isKeyDown(InputConstants.KEY_BACKSPACE)) {
				backspaceDelayTimer++;
				// Initial delay threshold of 15 ticks, then repeats every 2 ticks
				if (backspaceDelayTimer > 15 && (backspaceDelayTimer - 15) % 2 == 0) {
					String currentText = getProperty(TextProperty);
					if (currentText == null) currentText = "";

					if (isAllSelected) {
						setProperty(TextProperty, "");
						isAllSelected = false;
					} else if (!currentText.isEmpty()) {
						setProperty(TextProperty, currentText.substring(0, currentText.length() - 1));
					}
				}
			} else {
				backspaceDelayTimer = 0;
			}
		} else {
			caretVisible = false;
			caretTick = 0;
			backspaceDelayTimer = 0;
			isAllSelected = false;
		}
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		super.draw(renderer, partialTicks);

		if (isFocused && caretVisible) {
			float textX = textComponent.getActualSize().x();
			float textY = textComponent.getActualSize().y();
			float textWidth = textComponent.getPreferredSize().width();
			float textHeight = textComponent.getPreferredSize().height();

			if (isAllSelected) {
				renderer.drawBox(textX, textY + textHeight - 2, textWidth, 2, CARET_SHADER);
			} else {
				float caretX = textX + textWidth;
				renderer.drawBox(caretX, textY, 2, textHeight, CARET_SHADER);
			}
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);

		if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
			boolean hovered = getProperty(UIElement.IsHoveredProperty);
			if (!hovered && listeningForKey) {
				setListeningForKey(false);
			}
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (listeningForKey) {
			caretVisible = true;
			caretTick = 0;

			int key = event.GetKey();
			boolean ctrlDown = InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
					|| InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);

			// Handle CTRL Modifiers
			if (ctrlDown) {
				if (key == InputConstants.KEY_A) {
					isAllSelected = true;
					event.cancel();
					return;
				} else if (key == InputConstants.KEY_C) {
					String currentText = getProperty(TextProperty);
					if (currentText != null && !currentText.isEmpty()) {
						Tw9ineClient.MC.keyboardHandler.setClipboard(currentText);
					}
					event.cancel();
					return;
				} else if (key == InputConstants.KEY_V) {
					String clipboard = Tw9ineClient.MC.keyboardHandler.getClipboard();
					if (clipboard != null) {
						if (isAllSelected) {
							setProperty(TextProperty, clipboard);
							isAllSelected = false;
						} else {
							String currentText = getProperty(TextProperty);
							setProperty(TextProperty, (currentText == null ? "" : currentText) + clipboard);
						}
					}
					event.cancel();
					return;
				}
			}

			String currentText = getProperty(TextProperty);
			if (currentText == null) currentText = "";

			if (key == InputConstants.KEY_RETURN || key == InputConstants.KEY_ESCAPE) {
				setListeningForKey(false);
			} else if (key == InputConstants.KEY_BACKSPACE) {
				if (isAllSelected) {
					setProperty(TextProperty, "");
					isAllSelected = false;
				} else if (!currentText.isEmpty()) {
					setProperty(TextProperty, currentText.substring(0, currentText.length() - 1));
				}
			} else if (key == InputConstants.KEY_SPACE) {
				if (isAllSelected) {
					setProperty(TextProperty, " ");
					isAllSelected = false;
				} else {
					setProperty(TextProperty, currentText + ' ');
				}
			} else if (keyIsValid(event.GetKeyCode())) {
				char keyCode = (char) event.GetKeyCode();

				boolean shiftDown = InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
						|| InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
				if (shiftDown)
					keyCode = Character.toUpperCase(keyCode);
				else
					keyCode = Character.toLowerCase(keyCode);

				if (isAllSelected) {
					setProperty(TextProperty, String.valueOf(keyCode));
					isAllSelected = false;
				} else {
					setProperty(TextProperty, currentText + keyCode);
				}
			}

			event.cancel();
		}
	}

	private void refreshDisplayedText() {
		String text = getProperty(TextProperty);
		boolean empty = text == null || text.isEmpty();
		if (empty && !isFocused) {
			String placeholder = getProperty(PlaceholderText);
			textComponent.setProperty(StringComponent.TextProperty, placeholder == null ? "" : placeholder);
			textComponent.setProperty(UIElement.ForegroundProperty, PLACEHOLDER_SHADER);
		} else {
			textComponent.setProperty(StringComponent.TextProperty, text == null ? "" : text);
			textComponent.clearProperty(UIElement.ForegroundProperty);
		}
	}

	private boolean keyIsValid(int key) {
		return key == 45 || (key >= 48 && key <= 57) || (key >= 65 && key <= 90) || (key >= 97 && key <= 122);
	}

	private void setListeningForKey(boolean state) {
		if (listeningForKey == state)
			return;

		listeningForKey = state;
		isFocused = state;
		if (listeningForKey) {
			GuiManager.requestFocus(this);
			caretVisible = true;
			caretTick = 0;
			Tw9ine.getInstance().eventManager.AddListener(KeyDownListener.class, this);
		} else {
			isAllSelected = false;
			GuiManager.clearFocus(this);
			Tw9ine.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
		}
		refreshDisplayedText();
	}

	@Override
	protected void onLostFocus() {
		if (listeningForKey) {
			listeningForKey = false;
			isFocused = false;
			isAllSelected = false;
			Tw9ine.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
			refreshDisplayedText();
		}
	}

	public void setOnTextChanged(Consumer<String> onTextChanged) {
		this.onTextChanged = onTextChanged;
	}

	@Override
	protected void onVisibilityChanged(Boolean oldValue, Boolean newValue) {
		super.onVisibilityChanged(oldValue, newValue);
		if (!newValue)
			setListeningForKey(false);
	}
}