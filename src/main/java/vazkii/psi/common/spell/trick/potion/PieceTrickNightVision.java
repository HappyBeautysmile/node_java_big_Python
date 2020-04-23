/*
 * This class is distributed as a part of the Psi Mod.
 * Get the Source Code on GitHub:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.trick.potion;

import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;

import vazkii.psi.api.spell.Spell;

public class PieceTrickNightVision extends PieceTrickPotionBase {

	public PieceTrickNightVision(Spell spell) {
		super(spell);
	}

	@Override
	public Effect getPotion() {
		return Effects.NIGHT_VISION;
	}

	@Override
	public boolean hasPower() {
		return false;
	}
}
