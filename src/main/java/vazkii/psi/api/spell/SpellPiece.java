/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 * 
 * Psi is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 * 
 * File Created @ [16/01/2016, 15:19:22 (GMT)]
 */
package vazkii.psi.api.spell;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.psi.api.PsiAPI;
import vazkii.psi.api.internal.TooltipHelper;

public abstract class SpellPiece {

	private static final String TAG_KEY = "spellKey";
	private static final String TAG_PARAMS = "params";

	public final String registryKey;
	public final Spell spell;
	
	public boolean isInGrid = false;
	public int x, y;

	public Map<String, SpellParam> params = new HashMap();
	public Map<SpellParam, SpellParam.Side> paramSides = new HashMap<SpellParam, SpellParam.Side>();

	public SpellPiece(Spell spell) {
		this.spell = spell;
		registryKey = PsiAPI.spellPieceRegistry.getNameForObject(getClass());
		initParams();
	}

	public void initParams() {
		// NO-OP
	}
	
	public abstract EnumPieceType getPieceType();
	
	public abstract Class<?> getEvaluationType();
	
	public abstract Object evaluate();

	public abstract Object execute(SpellContext context);
	
	public void addToMetadata(SpellMetadata meta) {
		// NO-OP
	}
	
	public void addParam(SpellParam param) {
		params.put(param.name, param);
		paramSides.put(param, SpellParam.Side.OFF);
	}
	
	public <T>T getParamValue(SpellParam param) {
		SpellParam.Side side = paramSides.get(param);
		if(!side.isEnabled())
			return null;
		
		return (T) spell.grid.getPieceAtSideWithRedirections(x, y, side);
	}

	public String getUnlocalizedName() {
		return "psi.spellpiece." + registryKey;
	}

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
	
	public void drawAdditional() {
		// NO-OP
	}

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
	}

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

	public static class Null { };
	
}
