/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.components;

import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.MouseClickEvent;
import net.tw9ine.event.events.MouseMoveEvent;
import net.tw9ine.event.events.MouseScrollEvent;
import net.tw9ine.event.listeners.MouseScrollListener;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.UIProperty;
import net.tw9ine.gui.types.Rectangle;
import net.tw9ine.gui.types.Size;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.utils.types.MouseAction;

public class ScrollComponent extends Component implements MouseScrollListener {
	public static final UIProperty<Float> ScrollOffsetProperty = new UIProperty<>(
			"ScrollOffset", 0f, false, true, null, ScrollComponent::coerceScrollOffset);

	private static Float coerceScrollOffset(UIElement sender, Float value) {
		if (sender instanceof ScrollComponent sc && value != null) {
			float viewport = sc.actualSize != null ? sc.actualSize.height() : 0f;
			float max = Math.max(0f, sc.totalContentHeight - viewport);
			return Math.max(0f, Math.min(max, value));
		}
		return value;	
	}

	private static final float SCROLLBAR_PADDING = 2f;

	private float totalContentHeight = 0f;
	private boolean needsScrollbar = false;

	private final ScrollbarComponent scrollbar;

	public ScrollComponent() {
		scrollbar = new ScrollbarComponent();
		scrollbar.setParent(this);
		scrollbar.setOnScrollChanged(this::onScrollbarChanged);
	}

	public float getViewportHeight() {
		return actualSize != null ? actualSize.height() : 0f;
	}

	@Override
	public void initialize() {
		super.initialize();
		if (!scrollbar.isInitialized())
			scrollbar.initialize();
	}

	@Override
	public void update() {
		super.update();
		scrollbar.update();
	}

	@Override
	public void onVisibilityChanged(Boolean oldValue, Boolean newValue) {
		super.onVisibilityChanged(oldValue, newValue);
		if (newValue)
			Tw9ine.getInstance().eventManager.AddListener(MouseScrollListener.class, this);
		else
			Tw9ine.getInstance().eventManager.RemoveListener(MouseScrollListener.class, this);
	}

	private void onScrollbarChanged(float newOffset) {
		setProperty(ScrollOffsetProperty, newOffset);
	}

	@Override
	public Size measure(Size availableSize) {
		UIElement content = getContent();

		// Measure scrollbar to know its width.
		scrollbar.measureCore(availableSize);

		// Measure content with unconstrained height. measureCore so preferredSize is updated.
		if (content != null) {
			content.measureCore(new Size(availableSize.width(), Float.MAX_VALUE));
			totalContentHeight = content.getPreferredSize().height();
		} else {
			totalContentHeight = 0f;
		}

		// Cap to the viewport height.
		Float viewportMax = getProperty(UIElement.MaxHeightProperty);
		float reportedHeight = totalContentHeight;
		if (viewportMax != null && reportedHeight > viewportMax)
			reportedHeight = viewportMax;

		return new Size(availableSize.width(), reportedHeight);
	}

	@Override
	public void arrange(Rectangle finalSize) {
		setActualSize(finalSize);

		UIElement content = getContent();
		float viewportHeight = finalSize.height();

		// Determine if scrollbar is needed and remeasure the content if needed.
		needsScrollbar = totalContentHeight > viewportHeight;
		if (needsScrollbar && content != null) {
			float scrollbarWidth = scrollbar.getPreferredSize().width() + SCROLLBAR_PADDING;
			float reducedWidth = finalSize.width() - scrollbarWidth;

			content.measureCore(new Size(reducedWidth, Float.MAX_VALUE));
			totalContentHeight = content.getPreferredSize().height();
		}

		// Clamp the scroll offset and push to the scrollbar.
		float maxScroll = Math.max(0f, totalContentHeight - viewportHeight);
		float scrollOffset = Math.max(0f, Math.min(getProperty(ScrollOffsetProperty), maxScroll));
		setProperty(ScrollOffsetProperty, scrollOffset);
		scrollbar.setProperty(ScrollbarComponent.ScrollOffsetProperty, scrollOffset);

		// Get the width of the scrollbar if it exists and reduce the content width by it.
		float scrollbarWidth = needsScrollbar
				? scrollbar.getPreferredSize().width() + SCROLLBAR_PADDING
				: 0f;
		float contentWidth = finalSize.width() - scrollbarWidth;

		// Arrange content
		if (content != null) {
			content.arrange(new Rectangle(
					finalSize.x(),
					finalSize.y() - scrollOffset,
					contentWidth,
					totalContentHeight));
		}

		// Arrange scrollbar on the right edge.
		if (needsScrollbar) {
			scrollbar.setScrollState(viewportHeight, totalContentHeight);
			scrollbar.arrange(new Rectangle(
					finalSize.x() + contentWidth + SCROLLBAR_PADDING,
					finalSize.y(),
					scrollbar.getPreferredSize().width(),
					viewportHeight));
		}
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		boolean isVisible = getProperty(UIElement.IsVisibleProperty);
		if (!isVisible)
			return;

		UIElement content = getContent();
		if (content != null) {
			renderer.beginClip(actualSize);
			content.draw(renderer, partialTicks);
			renderer.endClip();
		}

		if (needsScrollbar)
			scrollbar.draw(renderer, partialTicks);
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		if (event.action == MouseAction.DOWN
				&& actualSize != null && !actualSize.intersects((float) event.mouseX, (float) event.mouseY))
			return;
		scrollbar.onMouseClick(event);
		if (event.isCancelled())
			return;
		super.onMouseClick(event);
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		scrollbar.onMouseMove(event);
		super.onMouseMove(event);
	}

	@Override
	public void onMouseScroll(MouseScrollEvent event) {
		if (Tw9ine.getInstance().guiManager.isClickGuiOpen() && getProperty(UIElement.IsHoveredProperty)) {
			if (totalContentHeight <= actualSize.height())
				return;

			float scrollAmount = 20f;
			float maxScroll = totalContentHeight - actualSize.height();
			float scrollOffset = getProperty(ScrollOffsetProperty);

			if (event.GetVertical() > 0)
				scrollOffset = Math.max(0f, scrollOffset - scrollAmount);
			else if (event.GetVertical() < 0)
				scrollOffset = Math.min(maxScroll, scrollOffset + scrollAmount);

			setProperty(ScrollOffsetProperty, scrollOffset);
			event.cancel();
		}
	}
}
