package org.jeo.data;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

import java.util.ArrayList;
import java.util.List;

import org.jeo.proj.Proj;
import org.jeo.util.Pair;
import org.osgeo.proj4j.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Represents a multi level structure of tiles.
 * <p>
 * 
 * </p> 
 * @author Justin Deoliveira, OpenGeo
 */
public class TilePyramid {

    /**
     * Creates a new builder.
     */
    public static TilePyramidBuilder build() {
        return new TilePyramidBuilder();
    }

    /**
     * Tile coordinate system origin. 
     */
    public static enum Origin {
        BOTTOM_LEFT, TOP_LEFT, BOTTOM_RIGHT, TOP_RIGHT;
    }

    CoordinateReferenceSystem crs = Proj.EPSG_4326;
    Envelope bounds = new Envelope(-180,180,-90,90);
    List<TileGrid> grids = new ArrayList<TileGrid>();
    Origin origin = Origin.BOTTOM_LEFT;
    Integer tileWidth = 256;
    Integer tileHeight = 256;

    /**
     * The grids making up the pyramid, sorted by ascending z/zoom value.
     */
    public List<TileGrid> getGrids() {
        return grids;
    }

    /**
     * The spatial extent of the tile pyramid.
     */
    public Envelope getBounds() {
        return bounds;
    }

    /**
     * Sets the spatial extent of the tile pyramid.
     */
    public void setBounds(Envelope bounds) {
        this.bounds = bounds;
    }

    /**
     * The coordinate reference system of the tile pyramid. 
     */
    public CoordinateReferenceSystem getCRS() {
        return crs;
    }

    /**
     * Sets the coordinate reference system of the tile pyramid.
     */
    public void setCRS(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    /**
     * The origin of tile coordinate system.
     * <p>
     * This value dictates how tile coordinates (x,y) should be interpreted. The defaule value for
     * this property is {@link Origin#BOTTOM_LEFT}.
     * </p>
     */
    public Origin getOrigin() {
        return origin;
    }

    /**
     * Sets origin of tile coordinate system.
     */
    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    /**
     * The width in pixels of tiles in the pyramid.
     * <p>
     * The default value for this property is 256.
     * </p>
     */
    public Integer getTileWidth() {
        return tileWidth;
    }

    /**
     * Sets the width in pixels of tiles in the pyramid.
     */
    public void setTileWidth(Integer tileWidth) {
        this.tileWidth = tileWidth;
    }

    /**
     * The height in pixels of tiles in the pyramid.
     * <p>
     * The default value for this property is 256.
     * </p>
     */
    public Integer getTileHeight() {
        return tileHeight;
    }

    /**
     * Sets the height in pixels of tiles in the pyramid.
     */
    public void setTileHeight(Integer tileHeight) {
        this.tileHeight = tileHeight;
    }

    /**
     * Returns the tile grid at the specified zoom level.
     * 
     * @param z The zoom level
     * 
     * @return The tile grid, or <code>null</code> if no such grid exists for the specified zoom.
     */
    public TileGrid grid(int z) {
        for (TileGrid grid : grids) {
            if (grid.getZ() == z) {
                return grid;
            }
        }
        return null;
    }

    /**
     * Returns the spatial extent of the specified tile.
     * <p>
     * The tile z value must match a zoom level defined by the pyramid. The tile x/y need not fall
     * within the bounds of the pyramid.  
     * </p>
     * @param t The tile.
     * 
     * @return The spatial extent of the tile.
     * 
     * @throws IllegalArgumentException If the tile has a z value not defined by the pyramid. 
     */
    public Envelope bounds(Tile t) {
        TileGrid grid = grid(t.getZ());
        if (grid == null) {
            throw new IllegalArgumentException(String.format("no grid at zoom %d", t.getZ()));
        }

        int w = grid.getWidth();
        int h = grid.getHeight();

        Envelope b = bounds;
        
        double dx = b.getWidth() / ((double)w);
        double dy = b.getHeight() / ((double)h);

        double x,y;

        switch(origin) {
        case BOTTOM_LEFT:
        case TOP_LEFT:
            x = b.getMinX() + dx*t.getX();
            break;
        default:
            x = b.getMinX() + dx*(w - t.getX());
        }

        switch(origin) {
        case BOTTOM_LEFT:
        case BOTTOM_RIGHT:
            y = b.getMinY() + dy*t.getY();
            break;
        default:
            y = b.getMinY() + dy*(h - t.getY());
        }

        return new Envelope(x, x+dx, y, y+dx);
    }

    /**
     * Creates a tile cover for the specified bounds using the specified width, height to 
     * determine the appropriate tile resolution.
     */
    public TileCover cover(Envelope e, int width, int height) {
        Pair<Double,Double> res = res(e, width, height);
        return cover(e, res.first(), res.second());
    }

    /**
     * Creates a tile cover for the specified bounds at the specified resolutions.
     */
    public TileCover cover(Envelope e, double resx, double resy) {
        return cover(e, match(e, resx, resy));
    }

    /**
     * Creates a tile cover for the specified bounds at the specified zoom level.
     */
    public TileCover cover(Envelope e, int z) {
        TileGrid grid = grid(z);
        return grid != null ? cover(e, grid) : null;
    }

    /**
     * Creates a tile cover for the specified bounds at the tile grid.
     */
    public TileCover cover(Envelope e, TileGrid grid) {
        int[] cov = cov(e, grid);
        if (cov == null) {
            return null;
        }

        return new TileCover(grid, cov[0], cov[2], cov[1], cov[3]);
    }

    Pair<Double,Double> res(Envelope bbox, int width, int height) {
        double resx = bbox.getWidth() / ((double)width);
        double resy = bbox.getHeight() / ((double)height);
        return new Pair<Double,Double>(resx, resy);
    }

    TileGrid match(Envelope bbox, double resx, double resy) {
        TileGrid best = null;
        double score = Double.MAX_VALUE;
        for (TileGrid grid : grids) {
            double res = Math.abs(resx - grid.getXRes()) + Math.abs(resy - grid.getYRes());
            if (res < score) {
                score = res;
                best = grid;
            }
        }

        if (best == null) {
            return null;
        }

        return best;
    }
    
    int[] cov(Envelope bbox, TileGrid grid) {
        int x1 = (int) 
            floor((((bbox.getMinX() - bounds.getMinX()) / bounds.getWidth()) * grid.getWidth()));
        int x2 = (int) 
            ceil(((bbox.getMaxX() - bounds.getMinX()) / bounds.getWidth()) * grid.getWidth())-1;
        int y1 = (int)
            floor(((bbox.getMinY() - bounds.getMinY()) / bounds.getHeight()) * grid.getHeight());
        int y2 = (int) 
            ceil(((bbox.getMaxY() - bounds.getMinY()) / bounds.getHeight()) * grid.getHeight())-1;

        return new int[]{x1, x2, y1, y2};
    }
}
