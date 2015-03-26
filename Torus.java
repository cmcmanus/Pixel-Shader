/*
 * Robert Conner McManus
 * PA4
 * 12/3/14
 * 
 * Torus.java
 * defines functionality to draw a torus to the screen
*/

public class Torus {

	float raxial, r;
	SketchBase sketch;
	
	public Torus(float _raxial, float _r, SketchBase _sketch){
		raxial = _raxial;
		r = _r;
		sketch = _sketch;
	}
	
	// draws the torus using the given number of stacks and slices
	public void draw(int stacks, int slices){
		// defines steps in u and v
		float ustep = (float) (2 * Math.PI / (float)stacks);
		float vstep = (float) (2 * Math.PI / (float) slices);
		for (int i = 0; i < slices; i++){
			// defines two us for drawing the torus
			float u1 = (float) (i * ustep - Math.PI);
			float u2 = u1 + ustep;
			
			// draws each row of the torus
			sketch.beginTriangleStrip();
			for (int j = 0; j < slices+1; j++){
				float v = (float) (j * vstep - Math.PI);
				float x1 = (float) ((raxial + r * Math.cos(u1)) * Math.cos(v));
				float y1 = (float) ((raxial + r * Math.cos(u1)) * Math.sin(v));
				float z1 = (float) (r * Math.sin(u1));
				// defines the normal for the first point
				float nx1 = (float) (2 * x1 * (1 - (raxial / Math.sqrt(x1*x1+y1*y1))));
				float ny1 = (float) (2 * y1 * (1 - (raxial / Math.sqrt(x1*x1+y1*y1))));
				float nz1 = 2 * z1;
				
				float x2 = (float) ((raxial + r * Math.cos(u2)) * Math.cos(v));
				float y2 = (float) ((raxial + r * Math.cos(u2)) * Math.sin(v));
				float z2 = (float) (r * Math.sin(u2));
				// defines the normal for the second point
				float nx2 = (float) (float) (2 * x2 * (1 - (raxial / Math.sqrt(x2*x2+y2*y2))));
				float ny2 = (float) (2 * y2 * (1 - (raxial / Math.sqrt(x2*x2+y2*y2))));
				float nz2 = 2 * z2;
				
				sketch.setNormal((new Point3D(nx1, ny1, nz1)).normalize());
				sketch.addVertex(new Point3D(x1, y1, z1));
				sketch.setNormal((new Point3D(nx2, ny2, nz2)).normalize());
				sketch.addVertex(new Point3D(x2, y2, z2));
			}
			sketch.end();
		}
	}
}
