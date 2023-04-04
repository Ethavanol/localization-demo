package localization.view;

import epistemic.DebugConfig;
import jason.environment.grid.GridWorldView;
import localization.models.LocalizationMapModel;

import java.awt.*;

public class LocalizationMapView extends GridWorldView {

    private final LocalizationMapModel model;
    private SettingsPanel settingsPanel;


    private LocalizationMapView(LocalizationMapModel model) {
        super(model,
                "Localization Map",
                500);

        super.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.model = model;
        this.getCanvas().addKeyListener(model);
        this.getModel().addMapListener(settingsPanel);

    }

    @Override
    public void initComponents(int width) {
        super.initComponents(width);

        settingsPanel = new SettingsPanel(this);

        // Initialize settings bar
        if (DebugConfig.getInstance().showSettingsPanel())
            super.getContentPane().add(BorderLayout.SOUTH, settingsPanel);
    }

    @Override
    public void drawAgent(Graphics g, int x, int y, Color c, int id) {
        if (id == 0)
            super.drawAgent(g, x, y, c, id);
        if (id == 1)
            super.drawAgent(g, x, y, Color.red, id);
        if (id == 2)
            super.drawAgent(g, x, y, Color.green, id);
    }

    @Override
    public void draw(Graphics g, int x, int y, int object) {
        if ((object & LocalizationMapModel.GOAL) != 0) {
            drawGoal(g, x, y);
        }

        if ((object & LocalizationMapModel.RED_DISP) != 0) {
            drawDispenser(g, x, y, Color.RED);
        }

        if ((object & LocalizationMapModel.BLUE_DISP) != 0) {
            drawDispenser(g, x, y, Color.BLUE);
        }

        if ((object & LocalizationMapModel.POSSIBLE) != 0) {
            drawEmpty(g, x, y);
            drawPossible(g, x, y);
        }


    }

    private void drawDispenser(Graphics g, int x, int y, Color color) {
        g.setColor(color);
        var size = 20;
        g.fill3DRect(x * cellSizeW + size, y * cellSizeH + size, cellSizeW - (2 * size), cellSizeH - (2 * size), true);

    }

    private void drawGoal(Graphics g, int x, int y) {
        g.setColor(Color.ORANGE);
        g.fillRoundRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4, 5, 5);

        g.setColor(Color.BLACK);
        g.drawString("GOAL", x * cellSizeW + (cellSizeW / 2) - 16, y * cellSizeH + (cellSizeH / 2));
    }

    private void drawPossible(Graphics g, int x, int y) {
        if (!settingsPanel.showPossible())
            return;

        g.setColor(Color.RED);
        if (model.getAgAtPos(x, y) == -1)
            g.drawOval(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 2, cellSizeH - 2);
        else {
            g.fillOval(x * cellSizeW, y * cellSizeH, cellSizeW, cellSizeH);
        }
    }

    public LocalizationMapView(MapType mapType) {
        this(LocalizationMapModel.loadFromFile(mapType));
    }

    @Override
    public LocalizationMapModel getModel() {
        return model;
    }

    public SettingsPanel getSettingsPanel() {
        return this.settingsPanel;
    }


    public enum MapType {
        LOCALIZATION_5x5("maps\\localization_map_5x5.json"),
        LOCALIZATION_10x10("maps\\localization_map_10x10.json"),
        LOCALIZATION_20x20("maps\\localization_map_20x20.json"),
        LOCALIZATION_30x30("maps\\localization_map_30x30.json"),
        LOCALIZATION_40x40("maps\\localization_map_40x40.json"),
        LOCALIZATION_50x50("maps\\localization_map_50x50.json"),
        // LOCALIZATION_100x100("maps\\localization_map_100x100.json"),
        IDENTIFICATION("identification_map.json");

        private String fileName;

        MapType(String s) {
            this.fileName = s;
        }

        public String getFileName() {
            return fileName;
        }
    }

}

