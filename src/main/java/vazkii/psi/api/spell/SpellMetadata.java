/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.api.spell;

import net.minecraft.item.ItemStack;

import vazkii.psi.api.cad.EnumCADStat;
import vazkii.psi.api.cad.ICAD;

import java.util.*;

/**
 * Metadata for a given spell. Basically just a fancy holder for a map of the spell's
 * stats.
 */
public final class SpellMetadata {

	public final Map<EnumSpellStat, Integer> stats = new EnumMap<>(EnumSpellStat.class);
	private Set<String> flags = new HashSet<>();
	/**
	 * Should errors from this spell not be sent to the player's chat?
	 */
	public boolean errorsSuppressed = false;

	public SpellMetadata() {
		for (EnumSpellStat stat : EnumSpellStat.class.getEnumConstants()) {
			stats.put(stat, 0);
		}
	}

	/**
	 * Adds a stat to the metadata, incrementing over the previous value.
	 */
	public void addStat(EnumSpellStat stat, int val) {
		int curr = stats.get(stat);
		setStat(stat, val + curr);
	}

	/**
	 * Sets a stat's value. No consideration over the previous value is done, so
	 * unless you really want to be weird, use {@link #addStat(EnumSpellStat, int)} instead.
	 */
	public void setStat(EnumSpellStat stat, int val) {
		stats.put(stat, val);
	}

	/**
	 * Sets a flag in the metadata.
	 */
	public void setFlag(String flag, boolean val) {
		if (val) {
			flags.add(flag);
		} else {
			flags.remove(flag);
		}
	}

	/**
	 * Returns if a flag exists in the metadata.
	 */
	public boolean getFlag(String flag) {
		return flags.contains(flag);
	}

	/**
	 * Evaluates this metadata's stats against a passed in stack (whose item must be an implementation
	 * of {@link ICAD}). Returns true if the stats are equal to or above the CAD.
	 */
	public boolean evaluateAgainst(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ICAD)) {
			return false;
		}

		ICAD cad = (ICAD) stack.getItem();
		for (EnumSpellStat stat : stats.keySet()) {
			EnumCADStat cadStat = stat.getTarget();
			if (cadStat == null) {
				continue;
			}

			int statVal = stats.get(stat);
			int cadVal = cad.getStatValue(stack, cadStat);
			if (cadVal != -1 && cadVal < statVal) {
				return false;
			}
		}

		return true;
	}

}
