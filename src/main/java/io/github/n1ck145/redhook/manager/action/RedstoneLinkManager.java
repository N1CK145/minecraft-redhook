package io.github.n1ck145.redhook.manager.action;

import io.github.n1ck145.redhook.api.action.RedstoneAction;
import io.github.n1ck145.redhook.config.ConfigManager;
import io.github.n1ck145.redhook.config.LinkConfig;
import io.github.n1ck145.redhook.constants.TriggerCondition;
import io.github.n1ck145.redhook.model.action.RedstoneActionInstance;
import io.github.n1ck145.redhook.util.ResponseMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class RedstoneLinkManager {
	private static final Map<Location, ArrayList<RedstoneActionInstance>> bindings = new HashMap<>();
	private static LinkConfig bindingsConfig;

	public static void initialize(Plugin plugin) {
		bindingsConfig = ConfigManager.getInstance().getBindingsConfig();
		bindings.putAll(bindingsConfig.loadBindings());
	}

	public static ResponseMessage bindBlock(Block block, RedstoneActionInstance actionInstance) {
		Location loc = block.getLocation();

		if (bindings.containsKey(loc)) {
			ArrayList<RedstoneActionInstance> blockInstances = bindings.get(loc);

			if (blockInstances.contains(actionInstance)) {
				return ResponseMessage.of("§cBlock already bound to this action", false);
			}

			boolean overwritten =
					blockInstances.removeIf(instance -> instance.getAction().equals(actionInstance.getAction()));
			blockInstances.add(actionInstance);

			if (overwritten) {
				saveBindings();
				return ResponseMessage.of(
						"§aChanged trigger condition for action to " + actionInstance.getTriggerCondition().name(),
						true);
			}

			saveBindings();
			return ResponseMessage.of("§aBound action to block", true);
		}
		else {
			bindings.put(loc, new ArrayList<>(List.of(actionInstance)));
		}

		saveBindings();
		return ResponseMessage.of("§aBound action to block", true);
	}

	public static ResponseMessage bindBlock(Block block, RedstoneAction action, TriggerCondition triggerCondition) {
		return bindBlock(block, new RedstoneActionInstance(action, triggerCondition));
	}

	public static ArrayList<RedstoneActionInstance> getActionInstances(Block block) {
		return getActionInstances(block.getLocation());
	}

	public static ArrayList<RedstoneActionInstance> getActionInstances(Location location) {
		return bindings.get(location);
	}

	public static boolean hasBinding(Location location) {
		return bindings.containsKey(location);
	}

	public static void unbindBlock(Location location) {
		bindings.remove(location);
		saveBindings();
	}

	public static void unbindBlock(Location location, RedstoneAction action) {
		ArrayList<RedstoneActionInstance> instances = getActionInstances(location);
		boolean removed = instances.removeIf(instance -> instance.getAction().equals(action));

		if (removed && instances.isEmpty())
			bindings.remove(location);

		saveBindings();
	}

	public static void triggerOn(Block block, Player triggerPlayer) {
		handleTrigger(block, triggerPlayer, true);
	}

	public static void triggerOff(Block block, Player triggerPlayer) {
		handleTrigger(block, triggerPlayer, false);
	}

	private static void handleTrigger(Block block, Player triggerPlayer, boolean isRedstoneOn) {
		ArrayList<RedstoneActionInstance> instances = getActionInstances(block);
		if (instances == null)
			return;

		for (RedstoneActionInstance instance : instances) {
			RedstoneAction action = ActionRegistry.get(instance.getAction().getId());
			if (action != null && instance.getTriggerCondition().matches(isRedstoneOn)) {
				action.execute(triggerPlayer);
			}
		}
	}

	private static void saveBindings() {
		if (bindingsConfig != null) {
			bindingsConfig.saveBindings(bindings);
		}
	}

	public static void clear() {
		bindings.clear();
	}
}