/*
 * This class is distributed as part of the Psi Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: https://psi.vazkii.net/license.php
 */
package vazkii.psi.common.spell.trick.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;

public class PieceTrickCollapseBlock extends PieceTrick {

	SpellParam<Vector3> position;

	public PieceTrickCollapseBlock(Spell spell) {
		super(spell);
		setStatLabel(EnumSpellStat.POTENCY, new StatLabel(80));
		setStatLabel(EnumSpellStat.COST, new StatLabel(125));
	}

	@Override
	public void initParams() {
		addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
	}

	@Override
	public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
		super.addToMetadata(meta);

		meta.addStat(EnumSpellStat.POTENCY, 80);
		meta.addStat(EnumSpellStat.COST, 125);
	}

	@Override
	public Object execute(SpellContext context) throws SpellRuntimeException {
		ItemStack tool = context.getHarvestTool();
		Vector3 positionVal = this.getParamValue(context, position);

		if (positionVal == null) {
			throw new SpellRuntimeException(SpellRuntimeException.NULL_VECTOR);
		}
		if (!context.isInRadius(positionVal)) {
			throw new SpellRuntimeException(SpellRuntimeException.OUTSIDE_RADIUS);
		}

		World world = context.focalPoint.getEntityWorld();
		BlockPos pos = positionVal.toBlockPos();
		BlockPos posDown = pos.down();
		BlockState state = world.getBlockState(pos);
		BlockState stateDown = world.getBlockState(posDown);

		if (!world.isBlockModifiable(context.caster, pos)) {
			return null;
		}

		if (stateDown.isAir(world, posDown) && state.getBlockHardness(world, pos) != -1 &&
				PieceTrickBreakBlock.canHarvestBlock(state, context.caster, world, pos, tool) &&
				world.getTileEntity(pos) == null) {

			BlockEvent.BreakEvent event = PieceTrickBreakBlock.createBreakEvent(state, context.caster, world, pos, tool);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled()) {
				return null;
			}

			FallingBlockEntity falling = new FallingBlockEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, state);
			world.addEntity(falling);
		}
		return null;
	}

}
