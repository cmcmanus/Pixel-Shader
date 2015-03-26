/*
 * Robert Conner McManus
 * PA4
 * 12/3/14
 * 
 * Point3D.java
 * defines functionality for a vector holding either location information or color information
*/

public class Point3D {

	public float x,y,z;
	
	// creates a new point using an integer color value
	public Point3D(int color){
		z = (float) ((color & 0xff) / 255.0);
		y = (float) (((color>>8) & 0xff) / 255.0);
		x = (float) (((color>>16) & 0xff) / 255.0);
	}
	
	// creates a new point using the given x, y, and z values
	public Point3D(float _x, float _y, float _z){
		x = _x;
		y = _y;
		z = _z;
	}
	
	// converts the point to a string
	public String toString(){
		return x + ", " + y + ", " + z;
	}
	
	// gets the integer version of the color interpreted points
	public int getBRGUint8(){
		int _b = Math.round(z*255.0f); 
		int _g = Math.round(y*255.0f); 
		int _r = Math.round(x*255.0f);
		
		return (_r<<16) | (_g<<8) | _b;
	}
	
	// return a normalized version of the vector
	public Point3D normalize(){
		float d = x*x + y*y + z*z;
		d = (float) Math.sqrt(d);
		float nx = x / d;
		float ny = y / d;
		float nz = z / d;
		return new Point3D(nx, ny, nz);
	}
	
	// return a vector equal to the the this vector dotted with the given vector
	public float dot(Point3D p){
		return x*p.x + y*p.y + z*p.z;
	}
	
	// return a vector equal to the the this vector dotted with the given vector
	public float dot(float[] f){
		assert(f.length >= 3);
		return x*f[0] + y*f[1] + z*f[2];
	}
	
	// return a vector equal to the current vector minus the given vector
	public Point3D subtract(Point3D p){
		float nx = x - p.x;
		float ny = y - p.y;
		float nz = z - p.z;
		return new Point3D(nx, ny, nz);
	}
	
	// return a vector equal to the current vector plus the given vector
	public Point3D add(Point3D p){
		float nx = x + p.x;
		float ny = y + p.y;
		float nz = z + p.z;
		return new Point3D(nx, ny, nz);
	}
	
	// return a vector equal to the current vector multiplied by a scalar
	public Point3D multiply(float scalar){
		return new Point3D(x*scalar, y*scalar, z*scalar);
	}
	
	// make a copy of the current vector
	public Point3D clone(){
		return new Point3D(x, y, z);
	}
	
	// return a vector equal to the current vector crossed with the given vector
	public Point3D cross(Point3D p){
		return new Point3D(y*p.z - z*p.y, z*p.x - x*p.z, x*p.y - y*p.x);
	}
	
	// compare two points
	public boolean equals(Point3D p){
		return (Math.abs(p.x - x) < 0.00001) && (Math.abs(p.y - y) < 0.00001) && (Math.abs(p.z - z) < 0.00001);
	}
	
	// calculate the length of the current vector
	public float length(){
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
}
