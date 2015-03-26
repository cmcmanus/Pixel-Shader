/*
 * Robert Conner McManus
 * PA4
 * 12/3/14
 * 
 * Transform.java
 * creates a transformation matrix for rotating, scaling, and translating the vertices along the screen
*/

public class Transform {

	private float[][] transformmatrix;
	private float[][] normalmatrix;
	private float[][] worldtransform;
	private float[][] normworldtrans;
	
	// creates a new transformation matrix and intializes it to the identity
	public Transform(){
		transformmatrix = new float[4][4];
		normalmatrix = new float[4][4];
		worldtransform = new float[4][4];
		normworldtrans = new float[4][4];
		loadIdentity();
	}
	
	// loads the identity into the normal and regular transformation matrices
	public void loadIdentity(){
		float i[][] = {{1, 0, 0, 0},{0, 1, 0, 0},{0, 0, 1, 0},{0, 0, 0, 1}};
		setMatrix(i);
		setNormalMatrix(i);
		setWorldMatrix(i);
		setWorldNormMatrix(i);
	}
	
	// sets the normal transformation matrix to the given matrix
	public void setNormalMatrix(float[][] trans){
		assert(trans.length == 4 && trans[0].length == 4);
		normalmatrix = trans.clone();
	}
	
	// sets the regular transform matrix to the given matrix
	public void setMatrix(float[][] trans){
		assert(trans.length == 4 && trans[0].length == 4);
		transformmatrix = trans.clone();
	}

	// sets the world transformation matrix to the given matrix
	public void setWorldMatrix(float[][] trans){
		assert(trans.length == 4 && trans[0].length == 4);
		worldtransform = trans.clone();
	}
	
	// sets the world normal transformation matrix to the given matrix
	public void setWorldNormMatrix(float[][] trans){
		assert(trans.length == 4 && trans[0].length == 4);
		normworldtrans = trans.clone();
	}
	
	// apply the given rotation to the world transformation matrix
	public void applyWorldRotation(float angle, float xaxis, float yaxis, float zaxis){
		// calculate the scalar portion
		float s = (float) Math.cos(Math.toRadians(-angle)/2); // angle negative to apply inverse rotation to world
		// calculate normalized versions of of the axes for the vector portion
		float d = xaxis*xaxis + yaxis*yaxis + zaxis*zaxis;
		d = (float) Math.sqrt(d);
		float ux = xaxis / d;
		ux *= Math.sin(Math.toRadians(angle)/2);
		float uy = yaxis / d;
		uy *= Math.sin(Math.toRadians(angle)/2);
		float uz = zaxis / d;
		uz *= Math.sin(Math.toRadians(angle)/2);
		// create a quaternion for the rotation
		Quaternion q = new Quaternion(s, ux, uy, uz);
		// convert the quaternion to a matrix
		float m[] = q.to_matrix();
		float mat[][] = {{m[0], m[4], m[8], m[12]}, {m[1], m[5], m[9], m[13]},
				{m[2], m[6], m[10], m[14]}, {m[3], m[7], m[11], m[15]}};
		// multiply the matrix by the current transformation matrix
		worldmult(mat);
		normworldmult(mat);
	}

	// apply the given translation to the world translation matrix
	public void applyWorldTranslation(float x, float y, float z){
		// define the translation matrix
		float[][] mat = {{1, 0, 0, -x}, {0, 1, 0, -y}, {0, 0, 1, -z}, {0, 0, 0, 1}}; // apply inverse translation to the world
		// multiply it by the world transformation matrix
		worldmult(mat);
	}
	
	// apply the given rotation to the transformation matrices
	public void applyRotation(float angle, float xaxis, float yaxis, float zaxis){
		// calculate the scalar portion
		float s = (float) Math.cos(Math.toRadians(angle)/2);
		// calculate normalized versions of of the axes for the vector portion
		float d = xaxis*xaxis + yaxis*yaxis + zaxis*zaxis;
		d = (float) Math.sqrt(d);
		float ux = xaxis / d;
		ux *= Math.sin(Math.toRadians(angle)/2);
		float uy = yaxis / d;
		uy *= Math.sin(Math.toRadians(angle)/2);
		float uz = zaxis / d;
		uz *= Math.sin(Math.toRadians(angle)/2);
		// create a quaternion for the rotation
		Quaternion q = new Quaternion(s, ux, uy, uz);
		// convert the quaternion to a matrix
		float m[] = q.to_matrix();
		float mat[][] = {{m[0], m[4], m[8], m[12]}, {m[1], m[5], m[9], m[13]},
				{m[2], m[6], m[10], m[14]}, {m[3], m[7], m[11], m[15]}};
		// multiply the matrix by the current transformation matrix
		multiply(mat);
		normalmult(mat);
	}

	// apply the given translation to the transformation matrix
	public void applyTranslation(float x, float y, float z){
		// define the translation matrix
		float[][] mat = {{1, 0, 0, x}, {0, 1, 0, y}, {0, 0, 1, z}, {0, 0, 0, 1}};
		// multiply it by the regular transformation matrix
		multiply(mat);
	}

	// apply the given scaling to the transformation matrix
	public void applyScale(float xs, float ys, float zs){
		// create the scaling matrix
		float[][] mat = {{xs, 0, 0, 0}, {0, ys, 0, 0}, {0, 0, zs, 0}, {0, 0, 0, 1}};
		// multiply it by the regular and normal transformation matrices
		multiply(mat);
		normalmult(mat);
	}

	// create a copy of the current transformation
	public Transform clone(){
		float trans[][] = transformmatrix.clone();
		Transform ret = new Transform();
		ret.setMatrix(trans);
		trans = normalmatrix.clone();
		ret.setNormalMatrix(trans);
		trans = worldtransform.clone();
		ret.setWorldMatrix(trans);
		trans = normworldtrans.clone();
		ret.setWorldNormMatrix(trans);
		
		return ret;
	}
	
	// multiply the normal matrix by the given matrix
	private void normalmult(float[][] mat){
		assert(mat.length == 4 && mat[0].length == 4);
		float[][] result = new float[4][4];
		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 4; j++){
				float res = 0;
				for (int k = 0; k < 4; k++){
					float val = normalmatrix[k][j] * mat[i][k]; // vertices ordered so that it is mat * T
					res += val;
				}
				result[i][j] = res;
			}
		}
		normalmatrix = result; // set the normal matrix to the result
	}

	// multiply the regular matrix by the given matrix
	private void multiply(float[][] mat){
		assert(mat.length == 4 && mat[0].length == 4);
		float[][] result = new float[4][4];
		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 4; j++){
				float res = 0;
				for (int k = 0; k < 4; k++){
					float val = transformmatrix[k][j] * mat[i][k]; // indices ordered so that it is mat * T
					res += val;
				}
				result[i][j] = res;
			}
		}
		transformmatrix = result; // set the transformation matrix to the result
	}

	private void normworldmult(float[][] mat){
		assert(mat.length == 4 && mat[0].length == 4);
		float[][] result = new float[4][4];
		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 4; j++){
				float res = 0;
				for (int k = 0; k < 4; k++){
					float val = normworldtrans[k][j] * mat[i][k]; // indices ordered so that it is mat * T
					res += val;
				}
				result[i][j] = res;
			}
		}
		normworldtrans = result; // set the normal world transformation matrix to the result
	}
	
	// multiply the world matrix by the given matrix
	private void worldmult(float[][] mat){
		assert(mat.length == 4 && mat[0].length == 4);
		float[][] result = new float[4][4];
		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 4; j++){
				float res = 0;
				for (int k = 0; k < 4; k++){
					float val = worldtransform[k][j] * mat[i][k]; // indices ordered so that it is mat * T
					res += val;
				}
				result[i][j] = res;
			}
		}
		worldtransform = result; // set the world transformation matrix to the result
	}
	
	// multiply a point by the normal world transformation matrix
	private Point3D normworldmult(Point3D point){
		float vec[] = {point.x, point.y, point.z, 1}; // make the vector homogenous
		float ret[] = new float[4];
		for (int i = 0; i < 4; i++){
			float val = 0;
			for (int j = 0; j < 4; j++){
				float v = normworldtrans[i][j] * vec[j];
				val += v;
			}
			ret[i] = val;
		}
		
		// return a new point for the value
		Point3D retval = new Point3D(ret[0], ret[1], ret[2]);
		return retval.normalize();
	}
	
	// multiply a point by the normal transformation matrix
	public Point3D normmult(Point3D point){
		float vec[] = {point.x, point.y, point.z, 1}; // make the vector homogenous
		float ret[] = new float[4];
		for (int i = 0; i < 4; i++){
			float val = 0;
			for (int j = 0; j < 4; j++){
				float v = normalmatrix[i][j] * vec[j];
				val += v;
			}
			ret[i] = val;
		}
		
		// return a new point for the value
		Point3D retval = new Point3D(ret[0], ret[1], ret[2]);
		return normworldmult(retval);
	}
	
	// multiply a point by the world transformation matrix
	private Point3D worldmult(Point3D point){
		float vec[] = {point.x, point.y, point.z, 1}; // augment the vector so that it is homogenous
		float ret[] = new float[4];
		for (int i = 0; i < 4; i++){
			float val = 0;
			for (int j = 0; j < 4; j++){
				float v = worldtransform[i][j] * vec[j];
				val += v;
			}
			ret[i] = val;
		}
		
		// create a point to return
		Point3D retval = new Point3D(ret[0], ret[1], ret[2]);
		return retval;
	}
	
	// multiply a point by the transformation matrix
	public Point3D multiply(Point3D point){
		float vec[] = {point.x, point.y, point.z, 1}; // augment the vector so that it is homogenous
		float ret[] = new float[4];
		for (int i = 0; i < 4; i++){
			float val = 0;
			for (int j = 0; j < 4; j++){
				float v = transformmatrix[i][j] * vec[j];
				val += v;
			}
			ret[i] = val;
		}
		
		// create a point to return
		Point3D retval = new Point3D(ret[0], ret[1], ret[2]);
		return worldmult(retval);
	}
}
