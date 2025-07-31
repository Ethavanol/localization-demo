package navigation_changing;

import MAP.Direction;
import MAP.MapType;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.*;
import java.util.Set;

public class NavChangingView extends GridWorldView {

    NavChangingModel navModel;

    public NavChangingView(NavChangingModel model) {
        super(model, "Navigation with Uncertainty", 700);
        navModel = model;
        defaultFont = new Font("Arial", Font.BOLD, 16); // change default font
        setVisible(true);
        repaint();
    }

    public NavChangingView(MapType mapType) {
        this(NavChangingModel.loadFromFile(mapType));
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        Graphics2D g2 = (Graphics2D) g;

        if (object == NavChangingModel.GOAL){
            g.setColor(Color.yellow);
            g.fillRect(x * this.cellSizeW + 1, y * this.cellSizeH + 1, this.cellSizeW - 2, this.cellSizeH - 2);
            g.setColor(Color.red);
            drawString(g, x, y, defaultFont, "Goal");
        }
        if (object == NavChangingModel.POSSIBLE_LOC){
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(5));
            g2.drawOval(x * cellSizeW + 3, y * cellSizeH + 3, cellSizeW - 6, cellSizeH - 6);
            if(this.navModel.gridPossibleObjects.containsKey(new Location(x,y))){
                Set<Direction> directionSet = this.navModel.possibleLocsWithDirections.get(new Location(x,y));
                int sizeSet = directionSet.size();

                g.setFont(defaultFont);
                FontMetrics metrics = g.getFontMetrics();
                int height = metrics.getHeight();

                int startY = - sizeSet / 2 * height + height / 2;
                for(Direction direction : directionSet){
                    String str = direction.toString();
                    int width = metrics.stringWidth(str);

                    g.drawString(str, x * this.cellSizeW + ((4 *this.cellSizeW) / 5 - width / 2), y * this.cellSizeH + this.cellSizeH / 2  + startY);
                    startY += height;
                }
                if(this.navModel.gridPossibleObjects.get(new Location(x,y)).contains(NavChangingModel.POSSIBLE_LOC_GOAL)){
                    g2.setColor(Color.blue);
                    g2.drawRect(x * cellSizeW + 3, y * cellSizeH + 3, cellSizeW - 6, cellSizeH - 6);
                }
            }
            g2.setStroke(new BasicStroke(1));
        }
        if (object == NavChangingModel.POSSIBLE_LOC_GOAL){
            g2.setColor(Color.blue);
            g2.setStroke(new BasicStroke(5));
            g2.drawRect(x * cellSizeW + 3, y * cellSizeH + 3, cellSizeW - 6, cellSizeH - 6);
            if(this.navModel.gridPossibleObjects.containsKey(new Location(x,y))){
                if(this.navModel.gridPossibleObjects.get(new Location(x,y)).contains(NavChangingModel.POSSIBLE_LOC)){
                    g2.setColor(Color.red);
                    g2.drawOval(x * cellSizeW + 3, y * cellSizeH + 3, cellSizeW - 6, cellSizeH - 6);
                }
            }
            g2.setStroke(new BasicStroke(1));
        }

    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        g.setColor(c);
        g.fillOval(x * this.cellSizeW + 5, y * this.cellSizeH + 5, this.cellSizeW - 10, this.cellSizeH - 10);
        if (id >= 0) {
            g.setColor(Color.black);
            this.drawString(g, x, y, this.defaultFont, String.valueOf(id + 1));
        }
        if(this.navModel.gridPossibleObjects.containsKey(new Location(x,y))){

            Set<Direction> directionSet = this.navModel.possibleLocsWithDirections.get(new Location(x,y));
            int sizeSet = (directionSet != null) ? directionSet.size() : 0;

            g.setFont(defaultFont);
            FontMetrics metrics = g.getFontMetrics();
            int height = metrics.getHeight();

            int startY = - sizeSet / 2 * height + height / 2;

            if(directionSet != null){
                for(Direction direction : directionSet){
                    String str = direction.toString();
                    int width = metrics.stringWidth(str);

                    g.drawString(str, x * this.cellSizeW + ((4 *this.cellSizeW) / 5 - width / 2), y * this.cellSizeH + this.cellSizeH / 2  + startY);
                    startY += height;
                }
            }
        }
    }

}
