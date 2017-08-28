package net.torocraft.nemesissystem.gui.displays;

public interface GuiDisplay {

	void draw(float mouseX, float mouseY);

	void setPosition(int x, int y);

	void clicked(int mouseX, int mouseY, int mouseButton);

}
