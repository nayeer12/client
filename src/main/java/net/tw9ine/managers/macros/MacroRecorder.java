/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.managers.macros;

import java.util.HashSet;
import java.util.LinkedList;
import com.mojang.blaze3d.platform.InputConstants;
import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.KeyDownEvent;
import net.tw9ine.event.events.KeyUpEvent;
import net.tw9ine.event.events.MouseClickEvent;
import net.tw9ine.event.events.MouseMoveEvent;
import net.tw9ine.event.events.MouseScrollEvent;
import net.tw9ine.event.listeners.KeyDownListener;
import net.tw9ine.event.listeners.KeyUpListener;
import net.tw9ine.event.listeners.MouseClickListener;
import net.tw9ine.event.listeners.MouseMoveListener;
import net.tw9ine.event.listeners.MouseScrollListener;
import net.tw9ine.managers.macros.actions.KeyClickMacroEvent;
import net.tw9ine.managers.macros.actions.MacroEvent;
import net.tw9ine.managers.macros.actions.MouseClickMacroEvent;
import net.tw9ine.managers.macros.actions.MouseMoveMacroEvent;
import net.tw9ine.managers.macros.actions.MouseScrollMacroEvent;

/**
 * Class responsible for recording Macros
 */
public class MacroRecorder
		implements MouseClickListener, MouseMoveListener, MouseScrollListener, KeyDownListener, KeyUpListener {

	private static final long SAMPLE_INTERVAL_NS = 4_000_000; // 1ms = 1000Hz, default 4000hz

	private LinkedList<MacroEvent> currentMacro = new LinkedList<MacroEvent>();
	private long startTime = 0;
	private long lastMoveTime = 0;
	private boolean recording = false;

	private final HashSet<Integer> heldKeys = new HashSet<>();
	private final HashSet<Integer> heldMouseButtons = new HashSet<>();

	/**
	 * Begins recording a Macro
	 */
	public void startRecording() {
		if (!recording) {
			currentMacro = new LinkedList<MacroEvent>();
			recording = true;
			startTime = System.nanoTime();

			Tw9ine.getInstance().eventManager.AddListener(MouseClickListener.class, this);
			Tw9ine.getInstance().eventManager.AddListener(MouseMoveListener.class, this);
			Tw9ine.getInstance().eventManager.AddListener(MouseScrollListener.class, this);
			Tw9ine.getInstance().eventManager.AddListener(KeyDownListener.class, this);
			Tw9ine.getInstance().eventManager.AddListener(KeyUpListener.class, this);
		}
	}

	/**
	 * Stops recording a Macro
	 */
	public void stopRecording() {
		if (recording) {
			long timeStamp = System.nanoTime() - startTime;

			// Set the macro to release all keys currently pressed at the end.
			for (int key : heldKeys)
				currentMacro.add(new KeyClickMacroEvent(timeStamp, key, 0, InputConstants.RELEASE, 0));
			for (int button : heldMouseButtons)
				currentMacro.add(new MouseClickMacroEvent(timeStamp, button, InputConstants.RELEASE, 0));
			heldKeys.clear();
			heldMouseButtons.clear();

			recording = false;
			startTime = 0;

			Tw9ine.getInstance().eventManager.RemoveListener(MouseClickListener.class, this);
			Tw9ine.getInstance().eventManager.RemoveListener(MouseMoveListener.class, this);
			Tw9ine.getInstance().eventManager.RemoveListener(MouseScrollListener.class, this);
			Tw9ine.getInstance().eventManager.RemoveListener(KeyDownListener.class, this);
			Tw9ine.getInstance().eventManager.RemoveListener(KeyUpListener.class, this);
		}
	}

	/**
	 * Returns the recorder to its default state.
	 */
	public void reset() {
		heldKeys.clear();
		heldMouseButtons.clear();
		recording = false;
		startTime = 0;
		currentMacro.clear();
		currentMacro = null;
	}
	
	/**
	 * Constructs and returns a macro built from this recorder.
	 */
	public Macro constructMacro() {
		if (!recording && currentMacro != null) {
			Macro macro = new Macro(currentMacro);
			return macro;
		}
		return null;
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		if (event.GetKey() != Tw9ine.getInstance().guiManager.clickGuiButton.getValue().getKeyCode()
				&& event.GetKey() != 256
				&& !Tw9ine.getInstance().guiManager.isClickGuiOpen()) {
			heldKeys.remove(Integer.valueOf(event.GetKey()));
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new KeyClickMacroEvent(timeStamp, event.GetKey(), event.GetKeyCode(), event.GetAction(),
					event.GetModifiers()));
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (event.GetKey() != Tw9ine.getInstance().guiManager.clickGuiButton.getValue().getKeyCode()
				&& event.GetKey() != 256
				&& !Tw9ine.getInstance().guiManager.isClickGuiOpen()) {
			heldKeys.add(event.GetKey());
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new KeyClickMacroEvent(timeStamp, event.GetKey(), event.GetKeyCode(), event.GetAction(),
					event.GetModifiers()));
		}
	}


	@Override
	public void onMouseScroll(MouseScrollEvent event) {
		if (!Tw9ine.getInstance().guiManager.isClickGuiOpen()) {
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new MouseScrollMacroEvent(timeStamp, event.GetHorizontal(), event.GetVertical()));
		}
	}

	@Override
	public void onMouseMove(MouseMoveEvent mouseMoveEvent) {
		if (!Tw9ine.getInstance().guiManager.isClickGuiOpen()) {
			if (mouseMoveEvent.getX() == 0 && mouseMoveEvent.getY() == 0)
				return;

			long now = System.nanoTime();
			if (now - lastMoveTime < SAMPLE_INTERVAL_NS)
				return;

			lastMoveTime = now;
			long timeStamp = now - startTime;
			currentMacro.add(new MouseMoveMacroEvent(timeStamp, mouseMoveEvent.getX(), mouseMoveEvent.getY()));
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent mouseClickEvent) {
		if (!Tw9ine.getInstance().guiManager.isClickGuiOpen()) {
			if (mouseClickEvent.action == InputConstants.PRESS)
				heldMouseButtons.add(mouseClickEvent.button);
			else if (mouseClickEvent.action == InputConstants.RELEASE)
				heldMouseButtons.remove(Integer.valueOf(mouseClickEvent.button));
			long timeStamp = System.nanoTime() - startTime;
			currentMacro.add(new MouseClickMacroEvent(timeStamp, mouseClickEvent.button, mouseClickEvent.action,
					mouseClickEvent.mods));
		}
	}
}
