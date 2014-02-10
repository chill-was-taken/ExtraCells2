package extracells.gui;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import extracells.container.ContainerBusIOFluid;
import extracells.gui.widget.WidgetFluidSlot;
import extracells.gui.widget.WidgetRedstoneModes;
import extracells.network.packet.PacketBusIOFluid;
import extracells.part.PartFluidIO;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiBusIOFluid extends GuiContainer implements WidgetFluidSlot.IConfigurable
{
	private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/busiofluid.png");
	public static final int xSize = 176;
	public static final int ySize = 184;
	private PartFluidIO part;
	private EntityPlayer player;
	private byte filterSize;
	private List<WidgetFluidSlot> fluidSlotList = new ArrayList<WidgetFluidSlot>();
	private boolean redstoneControlled;

	public GuiBusIOFluid(PartFluidIO _terminal, EntityPlayer _player)
	{
		super(new ContainerBusIOFluid(_terminal, _player));
		((ContainerBusIOFluid) inventorySlots).setGui(this);
		part = _terminal;
		player = _player;

		fluidSlotList.add(new WidgetFluidSlot(player, part, 0, 61, 12, this, (byte) 2));
		fluidSlotList.add(new WidgetFluidSlot(player, part, 1, 79, 12, this, (byte) 1));
		fluidSlotList.add(new WidgetFluidSlot(player, part, 2, 97, 12, this, (byte) 2));
		fluidSlotList.add(new WidgetFluidSlot(player, part, 3, 61, 30, this, (byte) 1));
		fluidSlotList.add(new WidgetFluidSlot(player, part, 4, 79, 30, this, (byte) 0));
		fluidSlotList.add(new WidgetFluidSlot(player, part, 5, 97, 30, this, (byte) 1));
		fluidSlotList.add(new WidgetFluidSlot(player, part, 6, 61, 48, this, (byte) 2));
		fluidSlotList.add(new WidgetFluidSlot(player, part, 7, 79, 48, this, (byte) 1));
		fluidSlotList.add(new WidgetFluidSlot(player, part, 8, 97, 48, this, (byte) 2));

		new PacketBusIOFluid(player, part).sendPacketToServer();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int mouseX, int mouseY)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
		drawTexturedModalRect(posX + 179, posY, 179, 0, 32, 86);
	}

	public void shiftClick(ItemStack itemStack)
	{
		FluidStack containerFluid = FluidUtil.getFluidFromContainer(itemStack);
		Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();
		for (WidgetFluidSlot fluidSlot : fluidSlotList)
		{
			if (fluidSlot.getFluid() == null || (fluid != null && fluidSlot.getFluid() == fluid))
			{
				fluidSlot.mouseClicked(itemStack);
				return;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{

		boolean overlayRendered = false;
		for (byte i = 0; i < 9; i++)
		{
			fluidSlotList.get(i).drawWidget();
			if (!overlayRendered && fluidSlotList.get(i).canRender())
				overlayRendered = renderOverlay(fluidSlotList.get(i), mouseX, mouseY);
		}

		for (Object button : buttonList)
		{
			if (button instanceof WidgetRedstoneModes)
				((WidgetRedstoneModes) button).drawTooltip(guiLeft, guiTop);
		}
	}

	public void changeConfig(byte _filterSize)
	{
		filterSize = _filterSize;
	}

	public boolean renderOverlay(WidgetFluidSlot fluidSlot, int mouseX, int mouseY)
	{
		if (isPointInRegion(fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY))
		{
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			drawGradientRect(fluidSlot.getPosX() + 1, fluidSlot.getPosY() + 1, fluidSlot.getPosX() + 17, fluidSlot.getPosY() + 17, -0x7F000001, -0x7F000001);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			return true;
		}
		return false;
	}

	public void updateFluids(List<Fluid> fluidList)
	{
		for (int i = 0; i < fluidSlotList.size() && i < fluidList.size(); i++)
		{
			fluidSlotList.get(i).setFluid(fluidList.get(i));
		}
	}

	public void updateRedstoneMode(RedstoneMode mode)
	{
		if (redstoneControlled && buttonList.size() > 0)
			((WidgetRedstoneModes) buttonList.get(0)).setRedstoneMode(mode);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn)
	{
		Slot slot = getSlotAtPosition(mouseX, mouseY);

		if (slot != null && slot.getStack() != null && slot.getStack().isItemEqual(AEApi.instance().items().itemNetworkTool.stack(1)))
			return;
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		for (WidgetFluidSlot fluidSlot : fluidSlotList)
		{
			if (isPointInRegion(fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY))
			{
				fluidSlot.mouseClicked(player.inventory.getItemStack());
				break;
			}
		}
	}

	public void actionPerformed(GuiButton button)
	{
		new PacketBusIOFluid(player, (byte) button.id, part).sendPacketToServer();
	}

	protected boolean isPointInRegion(int top, int left, int height, int width, int pointX, int pointY)
	{
		int k1 = guiLeft;
		int l1 = guiTop;
		pointX -= k1;
		pointY -= l1;
		return pointX >= top - 1 && pointX < top + height + 1 && pointY >= left - 1 && pointY < left + width + 1;
	}

	@Override
	public byte getConfigState()
	{
		return filterSize;
	}

	public void setRedstoneControlled(boolean _redstoneControlled)
	{
		redstoneControlled = _redstoneControlled;
		buttonList.clear();
		if (redstoneControlled)
			buttonList.add(new WidgetRedstoneModes(0, guiLeft - 18, guiTop, 16, 16, part.getRedstoneMode()));
	}

	protected Slot getSlotAtPosition(int p_146975_1_, int p_146975_2_)
	{
		for (int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k)
		{
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get(k);

			if (this.isMouseOverSlot(slot, p_146975_1_, p_146975_2_))
			{
				return slot;
			}
		}

		return null;
	}

	private boolean isMouseOverSlot(Slot p_146981_1_, int p_146981_2_, int p_146981_3_)
	{
		return this.func_146978_c(p_146981_1_.xDisplayPosition, p_146981_1_.yDisplayPosition, 16, 16, p_146981_2_, p_146981_3_);
	}
}
