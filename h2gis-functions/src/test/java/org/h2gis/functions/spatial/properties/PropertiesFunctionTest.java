/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 * <p>
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 * <p>
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.properties;

import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;
import org.locationtech.jts.io.WKTReader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Erwan Bocher, CNRS (2020)
 */
public class PropertiesFunctionTest {

    private static Connection connection;
    private Statement st;
    private static WKTReader WKT_READER;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(PropertiesFunctionTest.class.getSimpleName());
        WKT_READER = new WKTReader();
        WKT_READER.setIsOldJtsCoordinateSyntaxAllowed(false);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void testST_GeometryType() throws Exception {
        HashMap<String, String> geometriesToTest = new HashMap();
        geometriesToTest.put("POINT", "SRID=4326;POINT(0 0)");
        geometriesToTest.put("POINTZ", "SRID=4326;POINTZ(0 0 0)");
        geometriesToTest.put("POINTM", "SRID=4326;POINTM(0 0 0)");
        geometriesToTest.put("POINTZM", "SRID=4326;POINTZM(0 0 0 0)");
        geometriesToTest.put("LINESTRING", "SRID=4326;LINESTRING(20 10,20 20)");
        geometriesToTest.put("LINESTRINGZ", "SRID=4326;LINESTRINGZ(20 10 0,20 20 0)");
        geometriesToTest.put("LINESTRINGM", "SRID=4326;LINESTRINGM(20 10 0,20 20 0)");
        geometriesToTest.put("LINESTRINGZM", "SRID=4326;LINESTRINGZM(20 10 0 0,20 20 0 0)");
        geometriesToTest.put("MULTILINESTRING", "SRID=4326;MULTILINESTRING((0 0, 10 15), (56 50, 10 15))");
        geometriesToTest.put("MULTILINESTRINGZ", "SRID=4326;MULTILINESTRINGZ((0 0 0, 10 15 0), (56 50 0, 10 15 0))");
        geometriesToTest.put("MULTILINESTRINGM", "SRID=4326;MULTILINESTRINGM((0 0 0, 10 15 0), (56 50 0, 10 15 0))");
        geometriesToTest.put("MULTILINESTRINGZM", "SRID=4326;MULTILINESTRINGZM((0 0 0 0, 10 15 0 0), (56 50 0 0, 10 15 0 0))");

        for (Map.Entry<String, String> entry : geometriesToTest.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            ResultSet rs = st.executeQuery("SELECT ST_GeometryType('" + val + "'::GEOMETRY);");
            assertTrue(rs.next());
            assertEquals(key, rs.getString(1));
            rs.close();
        }
    }

    @Test
    public void testST_MemSize() throws Exception {
        ResultSet res = st.executeQuery("SELECT ST_MEmSIZE(null)");
        res.next();
        assertNull(res.getObject(1));
        res.close();
    }

    @Test
    public void testST_MemSize1() throws Exception {
        ResultSet res = st.executeQuery("SELECT ST_MemSize(ST_GeomFromText('POINT(0 0)'))");
        res.next();
        assertEquals(21l, res.getObject(1));
        res.close();
    }

    @Test
    public void testST_MemSize2() throws Exception {
        ResultSet res = st.executeQuery("SELECT ST_MemSize(ST_GeomFromText('POINT(0 0)', 4326))");
        res.next();
        assertEquals(25l, res.getObject(1));
        res.close();
    }

    @Test
    public void testST_MemSize3() throws Exception {
        ResultSet res = st.executeQuery("SELECT ST_MemSize(ST_GeomFromText('POINT EMPTY'))");
        res.next();
        assertEquals(21l, res.getObject(1));
        res.close();
    }

    @Test
    public void testST_Angle() throws Exception {
        // 3-point case (North to East)
        // P1(0,1) North, P2(0,0) Vertex, P3(1,0) East
        // Expected: PI/2 (1.570796...)
        try (ResultSet rs = st.executeQuery("SELECT ST_Angle('POINT(0 1)'::GEOMETRY, 'POINT(0 0)'::GEOMETRY, 'POINT(1 0)'::GEOMETRY)")) {
            assertTrue(rs.next());
            assertEquals(Math.PI / 2, rs.getDouble(1), 1E-6, "3-point North to East should be PI/2");
        }

        // 3-point case (North to South)
        // P1(0,1), P2(0,0), P3(0,-1)
        // Expected: PI (3.14159...)
        try (ResultSet rs = st.executeQuery("SELECT ST_Angle('POINT(0 1)'::GEOMETRY, 'POINT(0 0)'::GEOMETRY, 'POINT(0 -1)'::GEOMETRY)")) {
            assertTrue(rs.next());
            assertEquals(Math.PI, rs.getDouble(1), 1E-6, "3-point North to South should be PI");
        }

        // 4-point case (Parallel vectors)
        // V1: (0,0)->(0,1) [North], V2: (1,1)->(1,2) [North]
        // Expected: 0
        try (ResultSet rs = st.executeQuery("SELECT ST_Angle('POINT(0 0)'::GEOMETRY, 'POINT(0 1)'::GEOMETRY, 'POINT(1 1)'::GEOMETRY, 'POINT(1 2)'::GEOMETRY)")) {
            assertTrue(rs.next());
            assertEquals(0.0, rs.getDouble(1), 1E-6, "4-point parallel should be 0");
        }

        // 4-point case (North vector to West vector)
        // V1: North, V2: West
        // Expected: 3PI/2 (4.71238...) because we sweep clockwise North -> East -> South -> West
        try (ResultSet rs = st.executeQuery("SELECT ST_Angle('POINT(0 0)'::GEOMETRY, 'POINT(0 1)'::GEOMETRY, 'POINT(0 0)'::GEOMETRY, 'POINT(-1 0)'::GEOMETRY)")) {
            assertTrue(rs.next());
            assertEquals(3 * Math.PI / 2, rs.getDouble(1), 1E-6, "4-point North to West should be 3PI/2");
        }

        // Null handling
        try (ResultSet rs = st.executeQuery("SELECT ST_Angle(NULL, 'POINT(0 0)'::GEOMETRY, 'POINT(1 0)'::GEOMETRY)")) {
            assertTrue(rs.next());
            assertNull(rs.getObject(1), "ST_Angle with NULL input should return NULL");
        }

        // Full circle (P1 and P3 are the same)
        try (ResultSet rs = st.executeQuery("SELECT ST_Angle('POINT(0 1)'::GEOMETRY, 'POINT(0 0)'::GEOMETRY, 'POINT(0 1)'::GEOMETRY)")) {
            assertTrue(rs.next());
            assertEquals(0.0, rs.getDouble(1), 1E-6);
        }
    }

}
