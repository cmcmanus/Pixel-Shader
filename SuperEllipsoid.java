/*
 * Robert Conner McManus
 * PA4
 * 12/3/14
 * 
 * SuperEllipsoid.java
 * defines functions to draw a super Ellipsoid to the screen
*/

public class SuperEllipsoid {

	private float rx, ry, rz, e1, e2;
	private SketchBase sketch;
	
	public SuperEllipsoid(float _rx, float _ry, float _rz, float _e1, float _e2, SketchBase _sketch){
		rx = _rx;
		ry = _ry;
		rz = _rz;
		e1 = _e1;
		e2 = _e2;
		sketch = _sketch;
	}
	
	// draws the super ellipsoid using the given number of stacks and slices
	public void draw(int stacks, int slices){
		// defines the steps in u and v
		float ustep = (float) (Math.PI / (float) stacks);
		float vstep = (float) (2 * Math.PI / (float) slices);
		for (int i = 0; i < stacks; i++){
			// defines two us for use
			float u1 = (float) (i * ustep - Math.PI / 2);
			float u2 = u1 + ustep;
			
			// draws each row of the super ellipsoid
			sketch.beginTriangleStrip();
			for (int j = 0; j < slices+1; j++){
				float v = (float) (j * vstep - Math.PI);
				
				float x1 = (float) (rx * Math.signum(Math.cos(u1)) * Math.pow(Math.abs(Math.cos(u1)), e1) * Math.signum(Math.cos(v)) * Math.pow(Math.abs(Math.cos(v)), e2));
				float y1 = (float) (ry * Math.signum(Math.cos(u1)) * Math.pow(Math.abs(Math.cos(u1)), e1) * Math.signum(Math.sin(v)) * Math.pow(Math.abs(Math.sin(v)), e2));
				float z1 = (float) (rz * Math.signum(Math.sin(u1)) * Math.pow(Math.abs(Math.sin(u1)), e1));
				// calculates the normal at the location
				float tmp1 = (float) Math.pow(Math.abs(x1/rx), 2/e2);
				float tmp2 = (float) Math.pow(Math.abs(y1 / ry), 2/e2);
				float tmp3 = (float) Math.pow(Math.abs(tmp1 + tmp2), (e2/e1)-1);
				float nx1 = (float) (2 * tmp1 * tmp3 / (e1 * x1));
				float ny1 = (float) (2 * tmp2 * tmp3 / (e1 * y1));
				float nz1 = (float) (2 * Math.pow(Math.abs(z1 / rz), 2/e1) / (e1 * z1));
				
				float x2 = (float) (rx * Math.signum(Math.cos(u2)) * Math.pow(Math.abs(Math.cos(u2)), e1) * Math.signum(Math.cos(v)) * Math.pow(Math.abs(Math.cos(v)), e2));
				float y2 = (float) (ry * Math.signum(Math.cos(u2)) * Math.pow(Math.abs(Math.cos(u2)), e1) * Math.signum(Math.sin(v)) * Math.pow(Math.abs(Math.sin(v)), e2));
				float z2 = (float) (rz * Math.signum(Math.sin(u2)) * Math.pow(Math.abs(Math.sin(u2)), e1));
				// calculates the normal at point 2
				tmp1 = (float) Math.pow(Math.abs(x2/rx), 2/e2);
				tmp2 = (float) Math.pow(Math.abs(y2 / ry), 2/e2);
				tmp3 = (float)  Math.pow(Math.abs(tmp1 + tmp2), (e2/e1)-1);
				float nx2 = (float) (2 * tmp1 * tmp3 / (e1 * x2));
				float ny2 = (float) (2 * tmp2 * tmp3 / (e1 * y2));
				float nz2 = (float) (2 * Math.pow(Math.abs(z2 / rz), 2/e1) / (e1 * z2));
				
				sketch.setNormal(new Point3D(nx1, ny1, nz1).normalize());
				sketch.addVertex(new Point3D(x1, y1, z1));
				sketch.setNormal(new Point3D(nx2, ny2, nz2).normalize());
				sketch.addVertex(new Point3D(x2, y2, z2));
			}
			sketch.end();
		}
	}
}
