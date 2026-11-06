/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.managers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.logging.LogUtils;

import net.tw9ine.Tw9ine;
import net.tw9ine.api.IAddon;
import net.tw9ine.event.events.KeyDownEvent;
import net.tw9ine.event.events.KeyUpEvent;
import net.tw9ine.event.listeners.KeyDownListener;
import net.tw9ine.event.listeners.KeyUpListener;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.module.AntiCheat;
import net.tw9ine.module.Module;
import net.tw9ine.module.modules.combat.Aimbot;
import net.tw9ine.module.modules.combat.AutoAnchor;
import net.tw9ine.module.modules.combat.AutoTotem;
import net.tw9ine.module.modules.combat.BowAimbot;
import net.tw9ine.module.modules.combat.AutoCrystal;
import net.tw9ine.module.modules.combat.HitCrystal;
import net.tw9ine.module.modules.combat.MaceAura;
import net.tw9ine.module.modules.combat.TriggerBot;
import net.tw9ine.module.modules.misc.*;
import net.tw9ine.module.modules.movement.ClickTP;
import net.tw9ine.module.modules.movement.EntityControl;
import net.tw9ine.module.modules.movement.FastLadder;
import net.tw9ine.module.modules.movement.Fly;
import net.tw9ine.module.modules.movement.Freecam;
import net.tw9ine.module.modules.movement.Glide;
import net.tw9ine.module.modules.movement.GuiMove;
import net.tw9ine.module.modules.movement.HighJump;
import net.tw9ine.module.modules.movement.Jesus;
import net.tw9ine.module.modules.movement.Jetpack;
import net.tw9ine.module.modules.movement.NoFall;
import net.tw9ine.module.modules.movement.NoJumpDelay;
import net.tw9ine.module.modules.movement.NoSlowdown;
import net.tw9ine.module.modules.movement.Noclip;
import net.tw9ine.module.modules.movement.ReverseStep;
import net.tw9ine.module.modules.movement.Safewalk;
import net.tw9ine.module.modules.movement.Sneak;
import net.tw9ine.module.modules.movement.Speed;
import net.tw9ine.module.modules.movement.Spider;
import net.tw9ine.module.modules.movement.Sprint;
import net.tw9ine.module.modules.movement.Step;
import net.tw9ine.module.modules.movement.Strafe;
import net.tw9ine.module.modules.render.Breadcrumbs;
import net.tw9ine.module.modules.render.ChestESP;
import net.tw9ine.module.modules.render.EntityESP;
import net.tw9ine.module.modules.render.FocusFps;
import net.tw9ine.module.modules.render.Fullbright;
import net.tw9ine.module.modules.render.ItemESP;
import net.tw9ine.module.modules.render.NoRender;
import net.tw9ine.module.modules.render.POV;
import net.tw9ine.module.modules.render.BlockESP;
import net.tw9ine.module.modules.render.Tooltips;
import net.tw9ine.module.modules.render.Tracer;
import net.tw9ine.module.modules.render.Trajectory;
import net.tw9ine.module.modules.render.XRay;
import net.tw9ine.module.modules.render.Zoom;
import net.tw9ine.module.modules.world.*;
import net.tw9ine.settings.Setting;
import net.tw9ine.settings.types.EnumSetting;
import net.tw9ine.settings.types.KeybindSetting;
import net.tw9ine.utils.input.Keybind;
import net.minecraft.client.Minecraft;

public class ModuleManager implements KeyDownListener, KeyUpListener {
	private static final Minecraft MC = Minecraft.getInstance();

	public ArrayList<Module> modules = new ArrayList<Module>();

	// Modules
	public Aimbot aimbot = new Aimbot();
	public AirPlace airplace = new AirPlace();
	public AntiCactus anticactus = new AntiCactus();
	public AutoBreed autobreed = new AutoBreed();
	public AutoAnchor autoanchor = new AutoAnchor();
	public AutoCrystal autocrystal = new AutoCrystal();
	public AutoEat autoeat = new AutoEat();
	public AutoFarm autofarm = new AutoFarm();
	public AutoFish autofish = new AutoFish();
	public AntiHunger antihunger = new AntiHunger();
	public AutoOminousBottle autoOminousBottle = new AutoOminousBottle();
	public AutoShear autoShear = new AutoShear();
	public AutoSign autosign = new AutoSign();
	public AutoTotem autoTotem = new AutoTotem();
	public AutoTool autotool = new AutoTool();
	public AutoWalk autowalk = new AutoWalk();
	public BlockESP blockesp = new BlockESP();
	public BowAimbot bowaimbot = new BowAimbot();
	public Breadcrumbs breadcrumbs = new Breadcrumbs();
	public ChestESP chestesp = new ChestESP();
	public ClickTP clickTP = new ClickTP();
	public DiscordRPCModule discordRPC = new DiscordRPCModule();
	public EntityControl entityControl = new EntityControl();
	public EntityESP entityesp = new EntityESP();
	public EXPThrower expthrower = new EXPThrower();
	public FakePlayer fakeplayer = new FakePlayer();
	public FastLadder fastladder = new FastLadder();
	public FastPlace fastplace = new FastPlace();
	public FastBreak fastbreak = new FastBreak();
	public Fly fly = new Fly();
	public Freecam freecam = new Freecam();
	public Fullbright fullbright = new Fullbright();
	public ItemESP itemesp = new ItemESP();
	public NoRender norender = new NoRender();
	public FocusFps focusfps = new FocusFps();
	public Glide glide = new Glide();
	public GuiMove guimove = new GuiMove();
	public HitCrystal hitCrystal = new HitCrystal();
	public HighJump higherjump = new HighJump();
	public Jesus jesus = new Jesus();
	public Jetpack jetpack = new Jetpack();
	public MaceAura maceaura = new MaceAura();
	public MCA mcf = new MCA();
	public Noclip noclip = new Noclip();
	public NoFall nofall = new NoFall();
	public NoJumpDelay nojumpdelay = new NoJumpDelay();
	public NoSlowdown noslowdown = new NoSlowdown();
	public Nuker nuker = new Nuker();
	public POV pov = new POV();
	public RandomPlace randomplace = new RandomPlace();
	public ReverseStep reverseStep = new ReverseStep();
	public Safewalk safewalk = new Safewalk();
	public Scaffold scaffold = new Scaffold();
	public Sneak sneak = new Sneak();
	public Speed speed = new Speed();
	public Spider spider = new Spider();
	public Sprint sprint = new Sprint();
	public Step step = new Step();
	public Strafe strafe = new Strafe();
	public Surround surround = new Surround();
	public TileBreaker tilebreaker = new TileBreaker();
	public Timer timer = new Timer();
	public Tooltips tooltips = new Tooltips();
	public Tracer tracer = new Tracer();
	public Trajectory trajectory = new Trajectory();
	public TriggerBot triggerbot = new TriggerBot();
	public XCarry xCarry = new XCarry();
	public XRay xray = new XRay();
	public Zoom zoom = new Zoom();

	public EnumSetting<AntiCheat> antiCheat = EnumSetting.<AntiCheat>builder().id("tw9ine_anticheat")
			.displayName("Current AntiCheat")
			.description(
					"This setting will disable any modules or features that are known to be detected by a specific anticheat. ")
			.defaultValue(AntiCheat.Vanilla).onUpdate(s -> {
				for (Module module : modules) {
					if (module.isDetectable(s))
						module.state.setValue(false);
				}
			}).build();

	public ModuleManager(List<IAddon> addons) {
		try {
			// Attempts to find each field of type Module and add it to the module list.
			for (Field field : ModuleManager.class.getDeclaredFields()) {
				if (!Module.class.isAssignableFrom(field.getType()))
					continue;
				Module module = (Module) field.get(this);
				addModule(module);
			}

			// Gets each Addon and adds their modules to the client.
			addons.stream().filter(Objects::nonNull).forEach(addon -> {
				addon.modules().forEach(module -> {
					addModule(module);
				});
			});
		} catch (Exception e) {
			LogUtils.getLogger().error("Error initializing tw9ine modules: " + e.getMessage());
		}

		// Registers all Module settings to the settings manager.
		for (Module module : modules) {
			for (Setting<?> setting : module.getSettings()) {
				SettingManager.registerSetting(setting);
			}
		}

		Tw9ine.getInstance().eventManager.AddListener(KeyDownListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(KeyUpListener.class, this);
	}

	public void addModule(Module module) {
		modules.add(module);
	}

	public void disableAll() {
		for (Module module : modules) {
			module.state.setValue(false);
		}
	}

	public Module getModuleByName(String string) {
		for (Module module : modules) {
			if (module.getName().equalsIgnoreCase(string)) {
				return module;
			}
		}
		return null;
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (GuiManager.isKeyboardInputActive())
			return;
		if (event.GetKey() == InputConstants.UNKNOWN.getValue())
			return;

		if (MC.gui.screen() == null) {
			for (Module module : modules) {
				if (module.isDetectable(antiCheat.getValue()))
					continue;
				
				KeybindSetting setting = module.getBind();
				Keybind binding = setting.getValue();
				int keyCode = binding.getKeyCode();
				
				if (keyCode == InputConstants.UNKNOWN.getValue())
					continue;
				
				
				if (keyCode == event.GetKey()) {
					if(binding.getIsMomentary())
						module.enable();
					else
						module.toggle();
				}
			}
		}
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		if (GuiManager.isKeyboardInputActive())
			return;
		if (event.GetKey() == InputConstants.UNKNOWN.getValue())
			return;

		if (MC.gui.screen() == null) {
			for (Module module : modules) {
				KeybindSetting setting = module.getBind();
				Keybind binding = setting.getValue();
				int keyCode = binding.getKeyCode();
				if (keyCode == InputConstants.UNKNOWN.getValue())
					continue;
				
				if (binding.getIsMomentary() && keyCode == event.GetKey()) {
					module.disable();
				}
			}
		}
	}
}
