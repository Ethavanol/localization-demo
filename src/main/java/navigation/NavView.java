package navigation;

import MAP.MapType;
import jason.environment.grid.GridWorldView;
import navigation_changing.NavChangingModel;

import java.awt.*;

public class NavView extends GridWorldView {

    NavModel mapcmodel;

    public NavView(NavModel model) {
        super(model, "MAPC Robot", 700);
        this.mapcmodel = model;
        defaultFont = new Font("Arial", Font.BOLD, 16); // change default font
        setVisible(true);
        repaint();
    }

    public NavView(MapType mapType) {
        this(NavModel.loadFromFile(mapType));
    }

    @Override
    public NavModel getModel() {
        return mapcmodel;
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        if (object == NavChangingModel.GOAL){
            g.setColor(Color.yellow);
            g.fillRect(x * this.cellSizeW + 1, y * this.cellSizeH + 1, this.cellSizeW - 2, this.cellSizeH - 2);
            g.setColor(Color.red);
            drawString(g, x, y, defaultFont, "Goal");
        }
        Graphics2D g2 = (Graphics2D) g;
        if (object == NavModel.POSSIBLE){
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(5));
            g2.drawRect(x * cellSizeW + 10, y * cellSizeH + 10, cellSizeW - 20, cellSizeH - 20);
            g2.setStroke(new BasicStroke(1));
        }
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        super.drawAgent(g, x, y, c, id);
    }




}
