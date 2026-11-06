package net.tw9ine.rendering;

import java.util.Locale;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.device.GpuDevice;

import net.tw9ine.Tw9ineClient;

/**
 * Represents a set of known / implemented Render APIS
 * for use with the rendering pipelines.
 */
public enum RenderApi {
	OPENGL,
	VULKAN;

	private static RenderApi cached;

	/**
	 * Returns the active rendering backend.
	 */
	public static RenderApi current() {
		if (cached == null) {
			GpuDevice device = RenderSystem.tryGetDevice();
			if (device == null) {
				// Device not initialized yet. Don't cache it yet.
				return OPENGL;
			}
			String backend = device.getDeviceInfo().backendName();
			cached = backend != null && backend.toLowerCase(Locale.ROOT).contains("vulkan") ? VULKAN : OPENGL;
			Tw9ineClient.LOGGER.info("[tw9ine] Render API: " + (cached == VULKAN ? "Vulkan" : "OpenGL"));
		}
		return cached;
	}
}
