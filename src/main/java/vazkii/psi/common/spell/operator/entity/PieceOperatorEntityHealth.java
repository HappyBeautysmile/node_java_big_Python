package vazkii.psi.common.spell.operator.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import vazkii.psi.api.spell.Spell;
import vazkii.psi.api.spell.SpellContext;
import vazkii.psi.api.spell.SpellParam;
import vazkii.psi.api.spell.SpellRuntimeException;
import vazkii.psi.api.spell.param.ParamEntity;
import vazkii.psi.api.spell.piece.PieceOperator;

public class PieceOperatorEntityHealth extends PieceOperator {

	SpellParam<Entity> target;


	public PieceOperatorEntityHealth(Spell spell) {
		super(spell);
	}

	@Override
	public void initParams() {
		addParam(target = new ParamEntity(SpellParam.GENERIC_NAME_TARGET, SpellParam.YELLOW, false, false));
	}

	@Override
	public Class<?> getEvaluationType() {
		return Double.class;
	}

	@Override
	public Object execute(SpellContext context) throws SpellRuntimeException {
		Entity entity = this.getNonnullParamValue(context, target);
		if(!(entity instanceof LivingEntity)){
			throw new SpellRuntimeException(SpellRuntimeException.NULL_TARGET);
		}


		return ((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth();
	}
}
