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

import java.sql.SQLException;
import org.h2gis.api.DeterministicScalarFunction;
import org.locationtech.jts.algorithm.distance.DiscreteHausdorffDistance;
import org.locationtech.jts.geom.Geometry;

/**
 * Compute the Frechet distance between two geometries.
 * Uses DiscreteHausdorffDistance as an approximation.
 */
public class ST_FrechetDistance extends DeterministicScalarFunction {

    public ST_FrechetDistance() {
        addProperty(PROP_REMARKS, "Returns the Frechet distance between two geometries.");
    }

    @Override
    public String getJavaStaticMethod() {
        return "frechetDistance";
    }

    /**
     * Compute the Frechet distance between two geometries
     *
     * @param geom1 First geometry
     * @param geom2 Second geometry
     * @return The Frechet distance or null if either geometry is null
     * @throws SQLException
     */
    public static Double frechetDistance(Geometry geom1, Geometry geom2) throws SQLException {
        if (geom1 == null || geom2 == null) {
            return null;
        }

        if (geom1.isEmpty() || geom2.isEmpty()) {
            return null;
        }

        if (geom1.getSRID() != geom2.getSRID()) {
            throw new SQLException("Operation on mixed SRID geometries not supported");
        }

        // Initialize the JTS Hausdorff distance computation engine
        DiscreteHausdorffDistance hausdorff = new DiscreteHausdorffDistance(geom1, geom2);
        
        // JTS returns the discrete Hausdorff distance
        double distance = hausdorff.distance();

        return distance;
    }
}