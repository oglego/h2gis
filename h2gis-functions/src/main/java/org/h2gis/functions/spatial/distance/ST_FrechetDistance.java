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

package org.h2gis.functions.spatial.distance;

import org.h2.util.geometry.GeometryUtils;
import org.locationtech.jts.algorithm.match.FrechetSimilarityMeasure;
import org.locationtech.jts.geom.Geometry;
import org.h2.value.Value;
import org.h2.value.ValueDouble;
import org.h2.value.ValueNull;

public class ST_FrechetDistance {

    public static Value compute(Value geom1, Value geom2) {
        if (geom1.getType() == Value.NULL || geom2.getType() == Value.NULL) {
            return ValueNull.INSTANCE;
        }

        // Unpack H2 Values into JTS Geometry objects
        Geometry g1 = GeometryUtils.getGeometry(geom1);
        Geometry g2 = GeometryUtils.getGeometry(geom2);

        // Fail gracefully or return null if they aren't linear features
        if (g1 == null || g2 == null) {
            return ValueNull.INSTANCE;
        }

        // Initialize the JTS Frechet execution engine
        FrechetSimilarityMeasure frechet = new FrechetSimilarityMeasure();
        
        // JTS returns a metric scale measure, which we convert to the absolute distance
        double distance = frechet.distance(g1, g2);

        return ValueDouble.get(distance);
    }
}