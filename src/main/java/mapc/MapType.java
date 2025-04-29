package mapc;

public enum MapType {
    // *****************  WARNING ********************
    //If you are on Windows, the path will be "maps\\localization.."
    // On Linux it will be "maps/localization.."

        LOCALIZATION_4x3("maps/localization_map_4x3.json"),
        LOCALIZATION_5x5("maps/localization_map_5x5.json"),
//        LOCALIZATION_10x10("maps\\localization_map_10x10.json"),
//        LOCALIZATION_20x20("maps\\localization_map_20x20.json"),
//        LOCALIZATION_30x30("maps\\localization_map_30x30.json"),
//        LOCALIZATION_40x40("maps\\localization_map_40x40.json"),
//        LOCALIZATION_50x50("maps\\localization_map_50x50.json"),
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