package uk.joshiejack.shopaholic.client.gui.button;

//public class ButtonOutOfStock extends GuiButton {
//    protected final ShopScreen shop;
//
//    @SuppressWarnings("unchecked")
//    public ButtonOutOfStock(ShopScreen shop, int buttonId, int x, int y) {
//        super(buttonId, x, y, shop.shop.getOutofText());
//        this.height = 18;
//        this.shop = shop;
//    }
//
//    @Override
//    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
//        if (visible) {
//            FontRenderer fontrenderer = mc.fontRenderer;
//            mc.getTextureManager().bindTexture(ShopScreen.EXTRA);
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//            GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//            drawBackground();
//            drawForeground(fontrenderer, 14737632);
//            GlStateManager.color(1.0F, 1.0F, 1.0F);
//        }
//    }
//
//    private void drawBackground() {
//        drawTexturedModalRect(x, y, 0, 18, width / 2, height);
//        drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 18, width / 2, height);
//    }
//
//    private void drawForeground(FontRenderer fontrenderer, int j) {
//        drawString(fontrenderer, displayString, x + 10, y + (height - 8) / 2, j);
//        GlStateManager.color(1.0F, 1.0F, 1.0F);
//    }
//
//    @Override
//    public void playPressSound(SoundHandler soundHandlerIn) {}
//}

