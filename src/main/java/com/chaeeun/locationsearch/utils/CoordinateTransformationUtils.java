package com.chaeeun.locationsearch.utils;

import com.chaeeun.locationsearch.domain.Coordinate;
import org.locationtech.proj4j.*;

public class CoordinateTransformationUtils {
    private static final CoordinateTransformFactory ctf = new CoordinateTransformFactory();

    public static Coordinate katechToWgs84(String katecX, String katecY) {
        CRSFactory crsFactory = new CRSFactory();
        CoordinateReferenceSystem WGS84 = crsFactory.createFromParameters("WGS84", "+proj=longlat +datum=WGS84 +no_defs");
        CoordinateReferenceSystem KATEC = crsFactory.createFromParameters("KATEC", "+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=400000 +y_0=600000 +ellps=bessel +units=m +no_defs +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43");
        ProjCoordinate katecCoord = new ProjCoordinate(Double.parseDouble(katecX), Double.parseDouble(katecY));
        ProjCoordinate wgs84Coord = new ProjCoordinate();
        CoordinateTransform transform = ctf.createTransform(KATEC, WGS84);
        transform.transform(katecCoord, wgs84Coord);
        return new Coordinate(wgs84Coord.x, wgs84Coord.y);
    }

}
