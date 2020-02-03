/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Psi Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Psi
 *
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 *
 * File Created @ [03/02/2016, 18:10:29 (GMT)]
 */
package vazkii.psi.client.gui.button;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import vazkii.psi.api.internal.TooltipHelper;
import vazkii.psi.client.gui.GuiProgrammer;

public class GuiButtonIO extends Button {

    public final boolean out;
    final GuiProgrammer gui;

    public GuiButtonIO(int x, int y, boolean out, GuiProgrammer gui) {
        super(x, y, 12, 12, "", button -> {
        });
        this.out = out;
        this.gui = gui;
    }


    @Override
    public void renderButton(int par2, int par3, float pticks) {
        if (active && !gui.takingScreenshot) {
            isHovered = par2 >= x && par3 >= y && par2 < x + width && par3 < y + height;

            Minecraft.getInstance().textureManager.bindTexture(GuiProgrammer.texture);
            RenderSystem.color4f(1F, 1F, 1F, 1F);
            blit(x, y, isHovered() ? 186 : 174, out ? 169 : 181, width, height);

            if (isHovered()) {
                gui.tooltip.add(new StringTextComponent((out ? TextFormatting.RED : TextFormatting.BLUE) + TooltipHelper.local(out ? "psimisc.exportToClipboard" : "psimisc.importFromClipboard").toString()));
                gui.tooltip.add(new StringTextComponent(TextFormatting.GRAY + TooltipHelper.local("psimisc.mustHoldShift").toString()));
            }
        }
    }


}
