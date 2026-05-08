/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.functions.spatial.properties;

import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

/**
 * Returns the angle in radians between 3 points (P1-P2-P3) or 4 points (P1-P2, P3-P4).
 * PostGIS compatible: Swept clockwise from the first vector to the second.
 * 
 * @author Aaron Ogle
 */
public class ST_Angle extends DeterministicScalarFunction {

    public ST_Angle() {
        addProperty(PROP_REMARKS, "Returns the angle in radians between 3 or 4 points.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "getAngle";
    }

    // Overload for 3 points
    public static Double getAngle(Geometry p1, Geometry p2, Geometry p3) {
        return getAngle(p1, p2, p3, null);
    }

    // Core logic for 3 and 4 points
    public static Double getAngle(Geometry p1, Geometry p2, Geometry p3, Geometry p4) {
        if (p1 == null || p2 == null || p3 == null) {
            return null;
        }

        double az1, az2;

        if (p4 == null) {
            // 3-point case: P2 is the vertex. Vectors: P2->P1 and P2->P3
            az1 = getAzimuth(p2.getCoordinate(), p1.getCoordinate());
            az2 = getAzimuth(p2.getCoordinate(), p3.getCoordinate());
        } else {
            // 4-point case: Vectors: P1->P2 and P3->P4
            az1 = getAzimuth(p1.getCoordinate(), p2.getCoordinate());
            az2 = getAzimuth(p3.getCoordinate(), p4.getCoordinate());
        }

        double result = az2 - az1;

        // PostGIS normalization: wrap to [0, 2PI]
        if (result < 0) {
            result += 2 * Math.PI;
        }

        return result;
    }

    /**
     * Geographic azimuth: 0 is North, clockwise positive.
     */
    private static double getAzimuth(Coordinate c1, Coordinate c2) {
        return Math.atan2(c2.x - c1.x, c2.y - c1.y);
    }
}