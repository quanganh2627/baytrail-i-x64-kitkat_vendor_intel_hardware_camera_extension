package com.intel.camera2.extensions.depthcamera;
import android.util.FloatMath;
/**
* Point3DF holds 3D float coordinates
*/
public class Point3DF
{
    public float x;
    public float y;
    public float z;

    public Point3DF() {}

    public Point3DF(float x, float y, float z ) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set the point's x,y,  and z coordinates
     */
    public final void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Set the point's x,y and z coordinates to the coordinates of p
     */
    public final void set(Point3DF p) {
        this.x = p.x;
        this.y = p.y;
        this.z = p.z;
    }

    public final void negate() {
        x = -x;
        y = -y;
        z = -z;
    }

    public final void offset(float dx, float dy, float dz) {
        x += dx;
        y += dy;
        z += dz;
    }

    /**
     * Returns true if the point's coordinates equal (x,y,z)
     */
    public final boolean equals(float x, float y, float z) {
        return this.x == x && this.y == y && this.z == z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point3DF point3d = (Point3DF) o;

        if (Float.compare(point3d.x, x) != 0) return false;
        if (Float.compare(point3d.y, y) != 0) return false;
        if (Float.compare(point3d.z, z) != 0) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Point3DF(" + x + ", " + y  + ", " + z + ")";
    }

    /**
     * Return the euclidian distance from (0,0,0) to the point
     */
    public final float length() {
        return FloatMath.sqrt(x * x + y * y + z*z);
    }
}
