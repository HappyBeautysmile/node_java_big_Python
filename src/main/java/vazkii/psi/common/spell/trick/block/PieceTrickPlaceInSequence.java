/*
 * This class is distributed as a part of the Psi Mod.
 * Get the Source Code on GitHub:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import vazkii.psi.api.internal.MathHelper;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.EnumSpellStat;
import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellCompilationException;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellMetadata;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickPlaceInSequence extends PieceTrick {

	SpellParam<Vector3> position;
	SpellParam<Vector3> target;
	SpellParam<Number> maxBlocks;
	SpellParam<Vector3> direction;

	public PieceTrickPlaceInSequence(Spell spell) {
		super(spell);
	}

	@Override
	public void initParams() {
		addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
		addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.GREEN, false, false));
		addParam(maxBlocks = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, false, true));
		addParam(direction = new ParamVector(SpellParam.GENERIC_NAME_DIRECTION, SpellParam.CYAN, true, false));
	}

	@Override
	public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
		super.addToMetadata(meta);

		Double maxBlocksVal = this.<Double>getParamEvaluation(maxBlocks);
		if (maxBlocksVal == null || maxBlocksVal <= 0) {
			throw new SpellCompilationException(SpellCompilationException.NON_POSITIVE_VALUE, x, y);
		}

		meta.addStat(EnumSpellStat.POTENCY, (int) (maxBlocksVal * 8));
		meta.addStat(EnumSpellStat.COST, (int) (int) ((9.6 + (maxBlocksVal - 1) * 5.6)));
	}

	@Override
	public Object execute(SpellContext context) throws SpellRuntimeException {
		Vector3 positionVal = this.getParamValue(context, position);
		Vector3 targetVal = this.getParamValue(context, target);
		Number maxBlocksVal = this.getParamValue(context, maxBlocks);
		int maxBlocksInt = maxBlocksVal.intValue();
		Vector3 directionVal = this.getParamValue(context, direction);

		Direction direction = Direction.UP;
		if (directionVal != null) {
			direction = Direction.getFacingFromVector(directionVal.x, directionVal.y, directionVal.z);
		}

		if (positionVal == null) {
			throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
		}
		Vector3 targetNorm = targetVal.copy().normalize();

		for (BlockPos blockPos : MathHelper.getBlocksAlongRay(positionVal.toVec3D(), positionVal.copy().add(targetNorm.copy().multiply(maxBlocksInt)).toVec3D(), maxBlocksInt)) {
			if (!context.isInRadius(Vector3.fromBlockPos(blockPos))) {
				throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
			}

			PieceTrickPlaceBlock.placeBlock(context.caster, context.caster.getEntityWorld(), blockPos, context.getTargetSlot(), false, direction);
		}

		return null;
	}

}
