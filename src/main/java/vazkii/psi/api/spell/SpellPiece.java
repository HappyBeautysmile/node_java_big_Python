/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 * 
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 * 
 * File Created @ [16/01/2016, 15:19:22 (GMT)]
 */
package vazkii.psi.api.spell;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.TooltipHelper;

/**
 * A basic abstract piece of a spell. Instances of this class are created as needed
 * by the {@link Spell} object.
 */
public abstract class SpellPiece {

	private static final String TAG_KEY = "spellKey";
	private static final String TAG_PARAMS = "params";

	public final String registryKey;
	public final Spell spell;
	
	public boolean isInGrid = false;
	public int x, y;

	public Map<String, SpellParam> params = new LinkedHashMap();
	public Map<SpellParam, SpellParam.Side> paramSides = new LinkedHashMap<SpellParam, SpellParam.Side>();

	public SpellPiece(Spell spell) {
		this.spell = spell;
		registryKey = PsiAPI.spellPieceRegistry.getNameForObject(getClass());
		initParams();
	}

	/**
	 * Called to init this SpellPiece's {@link #params}. It's recommended you keep all params
	 * registered here as fields in your implementation, as they should be used in {@link #getParamValue(SpellContext, 
	 * SpellParam)} or {@link #getParamEvaluation(SpellParam)}.
	 */
	public void initParams() {
		// NO-OP
	}
	
	/**
	 * Gets what type of piece this is.
	 */
	public abstract EnumPieceType getPieceType();
	
	/**
	 * Gets what type this piece evaluates as. This is what other pieces
	 * linked to it will read. For example, a number sum operator will return
	 * Double.class, whereas a vector sum operator will return Vector3.class.<br>
	 * If you want this piece to not evaluate to anything (for Tricks, for example),
	 * return {@link Null}.class.
	 */
	public abstract Class<?> getEvaluationType();
	
	/**
	 * Evaluates this piece for the purpose of spell metadata calculation. If the piece
	 * is not a constant, you can safely return null.
	 */
	public abstract Object evaluate();

	/**
	 * Executes this piece and returns the value of this piece for later pieces to pick up
	 * on. For example, the number sum operator would use this function to act upon its parameters
	 * and return the result.
	 */
	public abstract Object execute(SpellContext context) throws SpellRuntimeException;
	
	/**
	 * Gets the string to be displayed describing this piece's evaluation type.
	 * @see {@link #getEvaluationType()}
	 */
	public String getEvaluationTypeString() {
		Class<?> evalType = getEvaluationType();
		String evalStr = evalType == null ? "Null" : evalType.getSimpleName();
		String s = StatCollector.translateToLocal("psi.datatype." + evalStr);
		if(getPieceType() == EnumPieceType.CONSTANT)
			s += " " + StatCollector.translateToLocal("psimisc.constant");
		
		return s;
	}
	
	/**
	 * Adds this piece's stats to the Spell's metadata.
	 */
	public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
		// NO-OP
	}
	
	/**
	 * Adds a {@link SpellParam} to this piece.
	 */
	public void addParam(SpellParam param) {
		params.put(param.name, param);
		paramSides.put(param, SpellParam.Side.OFF);
	}
	
	/**
	 * Gets the value of one of this piece's params in the given context.
	 */
	public <T>T getParamValue(SpellContext context, SpellParam param) {
		SpellParam.Side side = paramSides.get(param);
		if(!side.isEnabled())
			return null;
		
		try {
			SpellPiece piece = spell.grid.getPieceAtSideWithRedirections(x, y, side);
			if(piece == null || !param.canAccept(piece))
				return null;
			
			return (T) context.cspell.evaluatedObjects[piece.x][piece.y];
		} catch(SpellCompilationException e) {
			return null;
		}
	}
	
	/**
	 * Gets the evaluation of one of this piece's params in the given context. This calls
	 * {@link #evaluate()} and should only be used for {@link #addToMetadata(SpellMetadata)}
	 */
	public <T>T getParamEvaluation(SpellParam param) throws SpellCompilationException {
		SpellParam.Side side = paramSides.get(param);
		if(!side.isEnabled())
			return null;
		
		SpellPiece piece = spell.grid.getPieceAtSideWithRedirections(x, y, side);
		
		if(piece == null || !param.canAccept(piece))
			return null;
		
		return (T) piece.evaluate();
	}

	public String getUnlocalizedName() {
		return "psi.spellpiece." + registryKey;
	}
	
	public String getSortingName() {
		return StatCollector.translateToLocal(getUnlocalizedName());
	}
	
	public String getUnlocalizedDesc() {
		return "psi.spellpiece." + registryKey + ".desc";
	}

	/**
	 * Draws this piece onto the programmer GUI or the programmer TE projection.<br>
	 * All appropriate transformations are already done. Canvas is 16x16 starting from (0, 0, 0).<br>
	 * To avoid z-fighting in the TE projection, translations are applied every step.
	 */
	@SideOnly(Side.CLIENT)
	public void draw() {
		drawBackground();
		GlStateManager.translate(0F, 0F, 0.1F);
		drawAdditional();
		if(isInGrid) {
			GlStateManager.translate(0F, 0F, 0.1F);
			drawParams();
		}
		
		GlStateManager.color(1F, 1F, 1F);
	}
	
	/**
	 * Draws this piece's background.
	 * @see #draw()
	 */
	@SideOnly(Side.CLIENT)
	public void drawBackground() {
		ResourceLocation res = PsiAPI.simpleSpellTextures.get(registryKey);
		Minecraft.getMinecraft().renderEngine.bindTexture(res);
		
		GlStateManager.color(1F, 1F, 1F);
		WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
		wr.begin(7, DefaultVertexFormats.POSITION_TEX);
		wr.pos(0, 16, 0).tex(0, 1).endVertex();
		wr.pos(16, 16, 0).tex(1, 1).endVertex();
		wr.pos(16, 0, 0).tex(1, 0).endVertex();;
		wr.pos(0, 0, 0).tex(0, 0).endVertex();
		Tessellator.getInstance().draw();
	}
	
	/**
	 * Draws any additional stuff for this piece. Used in connectors
	 * to draw the lines.
	 * @see #draw()
	 */
	public void drawAdditional() {
		// NO-OP
	}

	/**
	 * Draws the parameters coming into this piece.
	 * @see #draw()
	 */
	@SideOnly(Side.CLIENT)
	public void drawParams() {
		Minecraft.getMinecraft().renderEngine.bindTexture(PsiAPI.internalHandler.getProgrammerTexture());
		for(SpellParam param : paramSides.keySet()) {
			SpellParam.Side side = paramSides.get(param);
			if(side.isEnabled()) {
				int minX = 4;
				int minY = 4;
				minX += side.offx * 9;
				minY += side.offy * 9;
				
				int maxX = minX + 8;
				int maxY = minY + 8;
				
				float wh = 8F;
				float minU = (side.u) / 256F;
				float minV = (side.v) / 256F;
				float maxU = (side.u + wh) / 256F;
				float maxV = (side.v + wh) / 256F;
				Color color = new Color(param.color);
				GlStateManager.color((float) color.getRed() / 255F, (float) color.getGreen() / 255F, (float) color.getBlue() / 255F);
				
				WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
				wr.begin(7, DefaultVertexFormats.POSITION_TEX);
				wr.pos(minX, maxY, 0).tex(minU, maxV).endVertex();
				wr.pos(maxX, maxY, 0).tex(maxU, maxV).endVertex();
				wr.pos(maxX, minY, 0).tex(maxU, minV).endVertex();;
				wr.pos(minX, minY, 0).tex(minU, minV).endVertex();
				Tessellator.getInstance().draw();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void getTooltip(List<String> tooltip) {
		TooltipHelper.addToTooltip(tooltip, getUnlocalizedName());
		TooltipHelper.tooltipIfShift(tooltip, () -> {
			addToTooltipAfterShift(tooltip);
		});
	}
	
	@SideOnly(Side.CLIENT)
	public void addToTooltipAfterShift(List<String> tooltip) {
		TooltipHelper.addToTooltip(tooltip, EnumChatFormatting.GRAY + "%s", getUnlocalizedDesc());
		
		TooltipHelper.addToTooltip(tooltip, "");
		String eval = getEvaluationTypeString();
		TooltipHelper.addToTooltip(tooltip, "<- " + EnumChatFormatting.GOLD + eval);
		
		for(SpellParam param : paramSides.keySet()) {
			String pName = StatCollector.translateToLocal(param.name);
			String pEval = param.getRequiredTypeString();
			TooltipHelper.addToTooltip(tooltip, (param.canDisable ? "[->] " : " ->  ") + EnumChatFormatting.YELLOW + pName + " [" + pEval + "]");
		}
	}

	/**
	 * Checks whether this piece should intercept keystrokes in the programmer interface.
	 * This is used for the number constant piece to change its value.
	 */
	@SideOnly(Side.CLIENT)
	public boolean interceptKeystrokes() {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean onKeyPressed(char c, int i) {
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean hasConfig() {
		return !params.isEmpty();
	}
	
	@SideOnly(Side.CLIENT)
	public void getShownPieces(List<SpellPiece> pieces) {
		pieces.add(this);
	}
	
	public static SpellPiece createFromNBT(Spell spell, NBTTagCompound cmp) {
		String key = cmp.getString(TAG_KEY);
		Class<? extends SpellPiece> clazz = PsiAPI.spellPieceRegistry.getObject(key);
		if(clazz != null) {
			SpellPiece p = create(clazz, spell);
			p.readFromNBT(cmp);
			return p;
		}

		return null;
	}

	public static SpellPiece create(Class<? extends SpellPiece> clazz, Spell spell) {
		try {
			return clazz.getConstructor(Spell.class).newInstance(spell);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public SpellPiece copy() {
		NBTTagCompound cmp = new NBTTagCompound();
		writeToNBT(cmp);
		return createFromNBT(spell, cmp);
	}

	public void readFromNBT(NBTTagCompound cmp) {
		NBTTagCompound paramCmp = cmp.getCompoundTag(TAG_PARAMS);
		for(String s : params.keySet()) {
			SpellParam param = params.get(s);
			paramSides.put(param, SpellParam.Side.fromInt(paramCmp.getInteger(s)));
		}
	}

	public void writeToNBT(NBTTagCompound cmp) {
		cmp.setString(TAG_KEY, registryKey);
		
		NBTTagCompound paramCmp = new NBTTagCompound();
		for(String s : params.keySet()) {
			SpellParam param = params.get(s);
			SpellParam.Side side = paramSides.get(param);
			paramCmp.setInteger(s, side.asInt());
		}
		cmp.setTag(TAG_PARAMS, paramCmp);
	}

	/**
	 * Empty helper class for use with evaluation types when none is present.
	 */
	public static class Null { }
	
}
