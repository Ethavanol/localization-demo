package basic_navigation_demo;

import jason.environment.grid.GridWorldView;

import java.awt.*;

public class MapcView extends GridWorldView {

    MapcModel mapcmodel;

    public MapcView(MapcModel model) {
        super(model, "MAPC Robot", 700);
        this.mapcmodel = model;
        defaultFont = new Font("Arial", Font.BOLD, 16); // change default font
        setVisible(true);
        repaint();
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        g.setColor(Color.yellow);
        g.fillRect(x * this.cellSizeW + 1, y * this.cellSizeH + 1, this.cellSizeW - 2, this.cellSizeH - 2);
        g.setColor(Color.lightGray);
        if (object == MapcModel.GOAL){
            g.setColor(Color.red);
            drawString(g, x, y, defaultFont, "Goal");
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        super.drawAgent(g, x, y, c, id);
    }


}
