/*
 * Robert Conner McManus
 * PA4
 * 12/3/14
 * 
 * SuperToroid.java
 * defines a functionality to draw a Super Toroid to the screen
*/

public class SuperToroid {

	private float raxial, r, e1, e2;
	private SketchBase sketch;
	
	public SuperToroid(float _raxial, float _r, float _e1, float _e2, SketchBase _sketch){
		raxial = _raxial;
		r = _r;
		e1 = _e1;
		e2 = _e2;
		sketch = _sketch;
	}
	
	// draws the super toroid using the given number of stacks and slices
	public void draw(int stacks, int slices){
		// defines the steps in u and v
		float ustep = (float) (2 * Math.PI / (float) stacks);
		float vstep = (float) (2 * Math.PI / (float) slices);
		for (int i = 0; i < stacks; i++){
			// defines two us for drawing
			float u1 = (float) (i * ustep - Math.PI);
			float u2 = u1 + ustep;
			
			// draws each row of the supertoroid
			sketch.beginTriangleStrip();
			for (int j = 0; j < slices+1; j++){
				float v = (float) (j * vstep - Math.PI);
				
				float x1 = (float) (Math.signum(Math.cos(v)) * Math.pow(Math.abs(Math.cos(v)), e1) * (raxial + r * Math.signum(Math.cos(u1)) * Math.pow(Math.abs(Math.cos(u1)), e2)));
				float y1 = (float) (Math.signum(Math.sin(v)) * Math.pow(Math.abs(Math.sin(v)), e1) * (raxial + r * Math.signum(Math.cos(u1)) * Math.pow(Math.abs(Math.cos(u1)), e2)));
				float z1 = (float) (r * Math.signum(Math.sin(u1)) * Math.pow(Math.abs(Math.sin(u1)), e2));
				// gets the normal for the first point
				float tmp1 = (float) (Math.signum(x1/r) * Math.pow(Math.abs(x1/r), 2/e1) + Math.signum(y1/r) * Math.pow(Math.abs(y1/r), 2/e1));
				float tmp2 = (float) (Math.signum(tmp1) * Math.pow(Math.abs(tmp1), e1/2) - raxial);
				float nx1 = (float) (2 * Math.signum(x1/r) * Math.pow(Math.abs(x1/r), 2/e1) * Math.signum(tmp1) * Math.pow(Math.abs(tmp1), e1/2-1) * Math.signum(tmp2) * Math.pow(Math.abs(tmp2), 2/e2 - 1) / (e2 * x1));
				float ny1 = (float) (2 * Math.signum(y1/r) * Math.pow(Math.abs(y1/r), 2/e1) * Math.signum(tmp1) * Math.pow(Math.abs(tmp1), e1/2-1) * Math.signum(tmp2) * Math.pow(Math.abs(tmp2), 2/e2 - 1) / (e2 * y1));
				float nz1 = (float) (2 * Math.signum(z1/r) * Math.pow(Math.abs(z1/r), 2/e2) / (e2 * z1));
				
				float x2 = (float) (Math.signum(Math.cos(v)) * Math.pow(Math.abs(Math.cos(v)), e1) * (raxial + r * Math.signum(Math.cos(u2)) * Math.pow(Math.abs(Math.cos(u2)), e2)));
				float y2 = (float) (Math.signum(Math.sin(v)) * Math.pow(Math.abs(Math.sin(v)), e1) * (raxial + r * Math.signum(Math.cos(u2)) * Math.pow(Math.abs(Math.cos(u2)), e2)));
				float z2 = (float) (r * Math.signum(Math.sin(u2)) * Math.pow(Math.abs(Math.sin(u2)), e2));
				// gets the normal for the second point
				tmp1 = (float) (Math.signum(x2/r) * Math.pow(Math.abs(x2/r), 2/e1) + Math.signum(y2/r) * Math.pow(Math.abs(y2/r), 2/e1));
				tmp2 = (float) (Math.signum(tmp1) * Math.pow(Math.abs(tmp1), e1/2) - raxial);
				float nx2 = (float) (2 * Math.signum(x2/r) * Math.pow(Math.abs(x2/r), 2/e1) * Math.signum(tmp1) * Math.pow(Math.abs(tmp1), e1/2-1) * Math.signum(tmp2) * Math.pow(Math.abs(tmp2), 2/e2 - 1) / (e2 * x2));
				float ny2 = (float) (2 * Math.signum(y2/r) * Math.pow(Math.abs(y2/r), 2/e1) * Math.signum(tmp1) * Math.pow(Math.abs(tmp1), e1/2-1) * Math.signum(tmp2) * Math.pow(Math.abs(tmp2), 2/e2 - 1) / (e2 * y2));
				float nz2 = (float) (2 * Math.signum(z2/r) * Math.pow(Math.abs(z2/r), 2/e2) / (e2 * z2));
				
				sketch.setNormal((new Point3D(nx1, ny1, nz1)).normalize());
				sketch.addVertex(new Point3D(x1, y1, z1));
				sketch.setNormal((new Point3D(nx2, ny2, nz2)).normalize());
				sketch.addVertex(new Point3D(x2, y2, z2));
			}
			sketch.end();
		}
	}
}
