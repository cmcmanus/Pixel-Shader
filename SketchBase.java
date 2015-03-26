//****************************************************************************
// SketchBase.  
//****************************************************************************
//
// Robert Conner McManus
// CS 680
// Programming Assignment 4
// 12/3/14
//
// Comments : 
//   Subroutines to manage and triangles in a scene
//
// History :
//   Aug 2014 Created by Jianming Zhang (jimmie33@gmail.com) based on code by
//   Stan Sclaroff (from CS480 '06 poly.c)

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

public class SketchBase 
{
	
	private ArrayList<Point3D> vertices;
	private ArrayList<Point3D> normals;
	private ArrayList<Point3D> bumpcoord;
	
	private Stack<Transform> transformstack;
	private Map<Integer, Light> lighttable;
	private Set<Integer> disabledlights;
	
	private Point3D curbumptex;
	private Point3D curnormal;
	
	private BufferedImage buff;
	private BufferedImage depth;
	private BufferedImage bumpmap;
	private BufferedImage bu;
	private BufferedImage bv;
	
	private Transform transform;
	
	private Material material;
	
	private Type curtype;
	
	private Style shadestyle;
	
	private final int maxdepth = 10;
	
	private boolean ambient;
	private boolean diffuse;
	private boolean specular;
	
	private boolean bump = true;
	
	private enum Type {
		NONE,
		TRIANGLE,
		TRIANGLE_STRIP,
		TRIANGLE_FAN;
	}
	
	private enum Style{
		FLAT,
		GOURAUD,
		PHONG
	}
	
	// initializes the sketch base
	public SketchBase(BufferedImage _buff){
		// initializes vertex variables
		vertices = new ArrayList<Point3D>();
		normals = new ArrayList<Point3D>();
		bumpcoord = new ArrayList<Point3D>();
		
		// sets the initial normal and texture variables
		curnormal = new Point3D(0, 0, 0);
		curbumptex = new Point3D(0, 0, 0);
		
		// creates the transformation matrix and stack
		transformstack = new Stack<Transform>();
		transform = new Transform();
		transformstack.add(transform.clone());
		
		// creates the material
		material = new Material();
		
		// sets up the light table
		lighttable = new HashMap<Integer,Light>();
		disabledlights = new HashSet<Integer>();
		
		// sets up the frame and depth buffer
		buff = _buff;
		depth = new BufferedImage(buff.getWidth(), buff.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		
		// loads in the bump map texture
		try {
			bumpmap = ImageIO.read(new File("textured-glass-bump-map.jpg"));
		} catch (IOException e){
	    	System.out.println("Error: reading texture image.");
	    	e.printStackTrace();
	    }
		bu = new BufferedImage(bumpmap.getWidth(), bumpmap.getHeight(), bumpmap.getType());
		bv = new BufferedImage(bumpmap.getWidth(), bumpmap.getHeight(), bumpmap.getType());
		//calculateBumpMapDerivative();
		
		// sets the shading style and triangle type
		curtype = Type.NONE;
		shadestyle = Style.GOURAUD;
		
		// indicates that all lighting should be used initially
		ambient = true;
		diffuse = true;
		specular = true;
	}

	// calculates the derivative of the bump map
	/*private void calculateBumpMapDerivative(){
		for (int i = 0; i < bumpmap.getWidth(); i++){
			for (int j = 0; j < bumpmap.getHeight(); j++){
				Point3D v = new Point3D(0, 0, 0);
				Point3D u = new Point3D(0, 0, 0);
				if (i > 0){
					if (j > 0){
						Point3D val = new Point3D(bumpmap.getRGB(i-1, j-1));
						v = v.subtract(val);
						u = u.subtract(val);
					}
					if (j < bumpmap.getHeight()-1){
						Point3D val = new Point3D(bumpmap.getRGB(i-1, j+1));
						u = u.subtract(val);
						v.add(val);
					}
					Point3D val = new Point3D(bumpmap.getRGB(i-1, j));
					u = u.subtract(val);
				}
				if (i < bumpmap.getWidth()-1){
					if (j > 0){
						Point3D val = new Point3D(bumpmap.getRGB(i+1, j-1));
						v = v.subtract(val);
						u = u.add(val);
					}
					if (j < bumpmap.getHeight()-1){
						Point3D val = new Point3D(bumpmap.getRGB(i+1, j+1));
						u = u.add(val);
						v = v.add(val);
					}
					Point3D val = new Point3D(bumpmap.getRGB(i+1, j));
					u = u.add(val);
				}
				if (j > 0){
					Point3D val = new Point3D(bumpmap.getRGB(i, j-1));
					v = v.subtract(val);
				}
				if (j < bumpmap.getHeight()-1){
					Point3D val = new Point3D(bumpmap.getRGB(i, j+1));
					v = v.add(val);
				}
				bu.setRGB(i, j, u.getBRGUint8());
				bv.setRGB(i, j, v.getBRGUint8());
			}
		}
	}*/

	// toggles whether the ambient light will be used
	public void toggleAmbient(){
		ambient = !ambient;
	}
	
	// toggles whether diffuse light will be used
	public void toggleDiffuse(){
		diffuse = !diffuse;
	}
	
	// toggles whether specular light will be used
	public void toggleSpecular(){
		specular = !specular;
	}
	
	// sets the ambient lighting of the specified light
	public void setLightAmbient(int light, float[] a){
		Light l;
		if (lighttable.containsKey(light)) // if the light currently exists alter it
			l = lighttable.get(light);
		else // if it doesn't, create a new light
			l = new Light();
		l.setAmbient(a);
		lighttable.put(light, l);
	}
	
	// sets the diffuse lighting of the specified light
	public void setLightDiffuse(int light, float[] d){
		Light l;
		if (lighttable.containsKey(light)) // if the light exists, alter it
			l = lighttable.get(light);
		else // if it doesn't, create a new light
			l = new Light();
		l.setDiffuse(d);
		lighttable.put(light, l);
	}
	
	// sets the specular lighting of the specified light
	public void setLightSpecular(int light, float[] s){
		Light l;
		if (lighttable.containsKey(light)) // if the light exists, alter it
			l = lighttable.get(light);
		else // if it doesn't create a new light
			l = new Light();
		l.setSpecular(s);
		lighttable.put(light, l);
	}

	// sets the radial attenuation of the specified light
	public void setLightRadialAttentuation(int light, float a0, float a1, float a2){
		Light l;
		if (lighttable.containsKey(light)) // if the light exists, alter it
			l = lighttable.get(light);
		else // if it doesn't, create a new light
			l = new Light();
		l.setRadialAttentuation(a0, a1, a2);
		lighttable.put(light, l);
	}

	// sets the spot light variables for the specified light
	public void setLightSpot(int light, float[] lookat, float angle, float a){
		Light l;
		if (lighttable.containsKey(light)) // if the light exists, alter it
			l = lighttable.get(light);
		else // if it doesn't create a new light
			l = new Light();
		l.setSpot(lookat, angle, a);
		lighttable.put(light, l);
	}
	
	// sets the position of the current light
	public void setLightPosition(int light, float[] p){
		Light l;
		if (lighttable.containsKey(light)) // if the light exists, alter it
			l = lighttable.get(light);
		else // otherwise create a new light
			l = new Light();
		l.setPosition(p);
		lighttable.put(light, l);
	}

	// toggles whether a light is on or off
	public void toggleLight(int light){
		if (disabledlights.contains(light)) // if it's currently off, remove it from the list of unused lights
			disabledlights.remove(light);
		else // otherwise add it to the list of unused lights
			disabledlights.add(light);
	}
	
	// turns on all the lights in the scene
	public void turnOnLights(){
		disabledlights.clear(); // removes every light from the unused light list
	}
	
	// loads the identity matrix into the transformation matrix
	public void loadIdentity(){
		transform.loadIdentity();
	}
	
	// applies the specified rotation to the world transformation matrix
	public void worldRotate(float angle, float x, float y, float z){
		transform.applyWorldRotation(angle, x, y, z);
	}
	
	// applies the specified translation to the world transform matrix
	public void worldTranslate(float x, float y, float z){
		transform.applyWorldTranslation(x, y, z);
	}
	
	// applies the specified rotation to the transformation matrix
	public void rotate(float angle, float x, float y, float z){
		transform.applyRotation(angle, x, y, z);
	}
	
	// applies the specified translation to the transformation matrix
	public void translate(float x, float y, float z){
		transform.applyTranslation(x, y, z);
	}
	
	// applies the specified scaling to the transformation matrix
	public void scale(float xs, float ys, float zs){
		transform.applyScale(xs, ys, zs);
	}
	
	// tells the draw function to use the flat shading
	public void useFlat(){
		shadestyle = Style.FLAT;
	}
	
	// tells the draw function to use the gouraud shading
	public void useGouraud(){
		shadestyle = Style.GOURAUD;
	}
	
	// tells the draw function to use the phong shading
	public void usePhong(){
		shadestyle = Style.PHONG;
	}
	
	// set the material ka value to the given ka
	public void setMaterialKa(float ka[]){
		material.setAmbient(ka);
	}
	
	// set the material kd value to the given kd
	public void setMaterialKd(float kd[]){
		material.setDiffuse(kd);
	}
	
	// set the material ks value to the given ks
	public void setMaterialKs(float ks[]){
		material.setSpecular(ks);
	}
	
	// set the material ns value to the given ns
	public void setMaterialNs(float ns){
		material.setNs(ns);
	}
	
	// computes the color at a given position with a given normal
	public Point3D computeColor(Point3D normal, Point3D position){
		// initialize the color to black
		float cr = 0;
		float cg = 0;
		float cb = 0;
		
		// normalize the normal just in case
		normal = normal.normalize();
		
		// specify the vector from the camera
		Point3D camera = new Point3D(0, 0, -1);
		
		// iterate through each light in the light table
		for (Integer index : lighttable.keySet()){
			if (disabledlights.contains(index)) // if the light is disabled, skip it
				continue;
			Light l = lighttable.get(index);
			
			// if ambient light is on, add in the ambient lighting
			cr += ambient ? l.ambient[0] * material.ka[0] : 0;
			cg += ambient ? l.ambient[1] * material.ka[1] : 0;
			cb += ambient ? l.ambient[2] * material.ka[2] : 0;
			
			// specifies the ray from the object to the light source
			Point3D ray;
			
			// if the fourth item in the position is 0, the light is infinite, so use its vector for the light ray
			if (l.position[3] == 0)
				ray = new Point3D(l.position[0], l.position[1], l.position[2]);
			else // otherwise calculate the vector from the object to the light source
				ray = new Point3D(l.position[0]-position.x, l.position[1] - position.y, l.position[2] - position.z);
			
			// calculate how far the point is from the light source
			float distance = ray.length();
			
			// normalize the ray for use in the dot product
			ray = ray.normalize();
			
			float ndotl = normal.dot(ray);
			
			// if the ray does not hit the front face of the point, don't do anything else
			if (ndotl > 0){
				
				// calculate radial attenuation if the light is not infinite and the light is using radial attenuation
				float fradial = !l.radial || l.position[3] == 0 ? 1 : 1 / (l.a0 + l.a1 * distance + l.a2 * distance * distance);
				// calculate angular attenuation if the light is not infinite and the light is using angular attenuation
				float fangular = !l.spot || l.position[3] == 0 ? 1 : ray.x * l.spotlookat[0] + ray.y * l.spotlookat[1] + ray.z * l.spotlookat[2];
				// if the ray is within the spread of the beam, use it, otherwise set the coefficient to 0
				if (fangular < Math.cos(l.spotangle))
					fangular = 0;
				// take the coefficient to the given power
				fangular = (float) Math.pow(fangular, l.a);
				
				// add in the diffuse component of the lighting if diffuse lighting is on
				cr += diffuse ? fangular * fradial * l.diffuse[0] * material.kd[0] * ndotl : 0;
				cg += diffuse ? fangular * fradial * l.diffuse[1] * material.kd[1] * ndotl : 0;
				cb += diffuse ? fangular * fradial * l.diffuse[2] * material.kd[2] * ndotl : 0;
				
				// calculate the the reflection vector
				float s = 2 * ray.dot(normal);
				Point3D tmp = normal.multiply(s);
				Point3D reflection = ray.subtract(tmp);
				reflection = reflection.normalize();
				
				// if the reflection vector hits the camera
				float vdotr = camera.dot(reflection);
				if (vdotr > 0){
					// add in the specular lighting component if specular lighting is turn on
					cr += specular ? fangular * fradial * material.ks[0] * l.specular[0] * Math.pow(vdotr, material.ns) : 0;
					cg += specular ? fangular * fradial * material.ks[1] * l.specular[1] * Math.pow(vdotr, material.ns) : 0;
					cb += specular ? fangular * fradial * material.ks[2] * l.specular[2] * Math.pow(vdotr, material.ns) : 0;
				}
			}
			
		}
		
		// cap the r, g, and b values at 1
		cr = Math.min(cr, 1);
		cg = Math.min(cg, 1);
		cb = Math.min(cb, 1);
		
		// return the color
		return new Point3D(cr, cg, cb);
	}

	// draw a triangle from the vertices array with the given indices using flat shading
	private void drawFlat(int i1, int i2, int i3){
		
		// get width and height of the window to convert the coordinates to screen coordinates
		int width = buff.getWidth();
		int height = buff.getHeight();
		
		Point3D v1 = vertices.get(i1).clone();
		Point3D v2 = vertices.get(i2).clone();
		Point3D v3 = vertices.get(i3).clone();
		// if the points are all the same, don't draw anything
		if (v1.equals(v2) || v2.equals(v3) || v3.equals(v1))
			return;
		
		// get the vector from the first vector to the second and the second to the third to calculate the normal of the face
		Point3D e1 = v1.subtract(v2);
		Point3D e2 = v2.subtract(v3);
		
		// cross edge vectors to get normal
		Point3D norm = e1.cross(e2);
		norm = norm.normalize();
		
		// get an average position for the face
		float vx = (v1.x + v2.x + v3.x) / 3;
		float vy = (v1.y + v2.y + v3.y) / 3;
		float vz = (v1.z + v2.z + v3.z) / 3;
		
		// if the triangle isn't facing the camera, don't draw it
		if (norm.z <= 0)
			return;
		
		// compute the color for the face
		Point3D color = computeColor(norm, new Point3D(vx, vy, vz));
		
		// convert the coordinates to screen coordinates, and the z values to the proper depth buffer value
		v1.x = (v1.x * width/2 + width/2);
		v1.y = (v1.y * height/2 + height/2);
		v1.z = (v1.z / -maxdepth);
		v2.x = (v2.x * width/2 + width/2);
		v2.y = (v2.y * height/2 + height/2);
		v2.z = (v2.z / -maxdepth);
		v3.x = (v3.x * width/2 + width/2);
		v3.y = (v3.y * height/2 + height/2);
		v3.z = (v3.z / -maxdepth);
		
		// arrange the points so that they are in order from largest y to smallest y
		if (v2.y > v1.y){
			Point3D tmp = v1;
			v1 = v2;
			v2 = tmp;
		}
		if (v3.y > v2.y){
			Point3D tmp = v2;
			v2 = v3;
			v3 = tmp;
		}
		if (v2.y > v1.y){
			Point3D tmp = v1;
			v1 = v2;
			v2 = tmp;
		}
		
		// if the first two points are not in a horizontal line, draw the bottom triangle
		if ((int)(v1.y+0.5) != (int)v2.y){
			// get the change in x and z
			float dx1 = -(v1.x-v2.x) / (float)((int)(v1.y+0.5)-(int)v2.y);
			float dx2 = -(v1.x-v3.x) / (float)((int)(v1.y+0.5)-(int)v3.y);
			float dz1 = -(v1.z-v2.z) / (float)((int)(v1.y+0.5)-(int)v2.y);
			float dz2 = -(v1.z-v3.z) / (float)((int)(v1.y+0.5)-(int)v3.z); 
			
			// switch the changes if dx1 is larger than dx2
			if (dx1 > dx2){
				float tmp = dx1;
				dx1 = dx2;
				dx2 = tmp;
				tmp = dz1;
				dz1 = dz2;
				dz2 = tmp;
			}
			
			// set the initial x and z values
			float x1 = v1.x;
			float x2 = v1.x;
			float z1 = v1.z;
			float z2 = v1.z;
			
			// iterate through the ys from v1 to v2
			for (int i = (int)(v1.y+0.5); i >= (int)v2.y; i--){
				// if the y is outside the frame buffer, skip it
				if (i >= buff.getHeight() || i < 0){
					x1 += dx1;
					x2 += dx2;
					z1 += dz1;
					z2 += dz2;
					continue;
				}
				
				// calculate the change in z as x changes
				float dz = (z1-z2) / (float)((int)x1-(int)(x2+0.5));
				// if x doesn't change, z doesn't change
				if ((int)x1-(int)(x2+0.5) == 0)
					dz = 0;
				
				// iterate across the row to fill in the triangle
				for (int j = (int)x1; j <= (int)(x2+0.5); j++){
					// if the x is outside the frame buffer, skip it
					if (j >= buff.getWidth() || j < 0)
						continue;
					
					// calculate z at the location
					float z = z1 + dz * (j-(int)x1);
					// get the current depth value at that point
					long depthval = 0xffffffffl & depth.getRGB(j, i);
					long zcomp = (long) (z * 0xffffffffl);
					
					// if the point is closer to the screen, draw it and update the depth buffer
					if (zcomp <= depthval){
						buff.setRGB(j, i, color.getBRGUint8());
						depth.setRGB(j, i, (int)zcomp);
					}
				}
				
				// update the xs and zs
				x1 += dx1;
				x2 += dx2;
				z1 += dz1;
				z2 += dz2;
			}
		}
		// if the second two points do not form a line, draw the upper triangle
		if ((int)v2.y != (int)v3.y){
			// get the change in x and z
			float dx1 = (v3.x-v2.x) / (float)((int)v3.y-(int)v2.y);
			float dx2 = (v1.x-v3.x) / (float)((int)v1.y-(int)v3.y);
			float dz1 = (v3.z-v2.z) / (float)((int)v3.y-(int)v2.y);
			float dz2 = (v1.z-v3.z) / (float)((int)v1.y-(int)v3.z); 
			
			// if dx1 is larger than dx2, switch the two
			if (dx1 > dx2){
				float tmp = dx1;
				dx1 = dx2;
				dx2 = tmp;
				tmp = dz1;
				dz1 = dz2;
				dz2 = tmp;
			}
			
			// set the initial x and z
			float x1 = v3.x;
			float x2 = v3.x;
			float z1 = v3.z;
			float z2 = v3.z;
			
			// iterate through the ys from v3 to v2
			for (int i = (int)v3.y; i <= (int)v2.y; i++){
				// if the y is outside the frame buffer, skip it
				if (i >= buff.getHeight() || i < 0){
					x1 += dx1;
					x2 += dx2;
					z1 += dz1;
					z2 += dz2;
					continue;
				}
				
				// calculate the change in z as x changes
				float dz = (z1-z2) / (float)((int)x1-(int)(x2+0.5));
				// if x does not change, z does not either
				if ((int)x1-(int)(x2+0.5) == 0)
					dz = 0;
				
				// iterate through the pixels on the row to fill the triangle
				for (int j = (int)x1; j <= (int)(x2+0.5); j++){
					// if the x is outside the frame buffer, skip it
					if (j >= buff.getWidth() || j < 0)
						continue;
					
					// calculate the depth at the current pixel
					float z = z1 + dz * (j-(int)x1);
					// get the current depth there
					long depthval = 0xffffffffl & depth.getRGB(j, i);
					long zcomp = (long) (z * 0xffffffffl);
					// if the point is closer to the camera, draw it to the screen
					if (zcomp <= depthval){
						buff.setRGB(j, i, color.getBRGUint8());
						depth.setRGB(j, i, (int)zcomp);
					} 
				}
				
				// update x and z
				x1 += dx1;
				x2 += dx2;
				z1 += dz1;
				z2 += dz2;
			}
		}
	}

	// draw a triangle from the vertices array with the given indices using gouraud shading
	private void drawGouraud(int i1, int i2, int i3){
		
		// get the width and the height of the window for conversion to screen coordinates
		int width = buff.getWidth();
		int height = buff.getHeight();
		
		Point3D v1 = vertices.get(i1).clone();
		Point3D v2 = vertices.get(i2).clone();
		Point3D v3 = vertices.get(i3).clone();
		Point3D n1 = normals.get(i1).clone();
		Point3D n2 = normals.get(i2).clone();
		Point3D n3 = normals.get(i3).clone();
		
		// if all the vertices are the same, don't draw them
		if (v1.equals(v2) || v2.equals(v3) || v3.equals(v1))
			return;
		
		// if all the vertices are facing away from the camera, don't draw it
		if (n1.z <= 0 && n2.z <= 0 && n3.z <= 0)
			return;
		
		// compute the color at each of the vertices
		Point3D color1 = computeColor(n1, v1);
		Point3D color2 = computeColor(n2, v2);
		Point3D color3 = computeColor(n3, v3);
		
		// convert the coordinates to screen coordinates and the z to the depth value
		v1.x = (v1.x * width/2 + width/2);
		v1.y = (v1.y * height/2 + height/2);
		v1.z = (v1.z / -maxdepth);
		v2.x = (v2.x * width/2 + width/2);
		v2.y = (v2.y * height/2 + height/2);
		v2.z = (v2.z / -maxdepth);
		v3.x = (v3.x * width/2 + width/2);
		v3.y = (v3.y * height/2 + height/2);
		v3.z = (v3.z / -maxdepth);
		
		// order the vertices from largest y to smallest y
		if (v2.y > v1.y){
			Point3D tmp = v1;
			v1 = v2;
			v2 = tmp;
			tmp = color1;
			color1 = color2;
			color2 = tmp;
		}
		if (v3.y > v2.y){
			Point3D tmp = v2;
			v2 = v3;
			v3 = tmp;
			tmp = color2;
			color2 = color3;
			color3 = tmp;
		}
		if (v2.y > v1.y){
			Point3D tmp = v1;
			v1 = v2;
			v2 = tmp;
			tmp = color1;
			color1 = color2;
			color2 = tmp;
		}
		
		// if v1 and v2 do not form a horizontal line, draw the bottom triangle
		if ((int)(v1.y+0.5) != (int)v2.y){
			// get the change in x, z, r, g, and b
			float dx1 = -(v1.x-v2.x) / (float)((int)(v1.y+0.5)-(int)v2.y);
			float dx2 = -(v1.x-v3.x) / (float)((int)(v1.y+0.5)-(int)v3.y);
			float dz1 = -(v1.z-v2.z) / (float)((int)(v1.y+0.5)-(int)v2.y);
			float dz2 = -(v1.z-v3.z) / (float)((int)(v1.y+0.5)-(int)v3.y); 
			float dr1 = -(color1.x-color2.x) / (float)((int)(v1.y+0.5)-(int)v2.y); 
			float dr2 = -(color1.x-color3.x) / (float)((int)(v1.y+0.5)-(int)v3.y);
			float dg1 = -(color1.y-color2.y) / (float)((int)(v1.y+0.5)-(int)v2.y); 
			float dg2 = -(color1.y-color3.y) / (float)((int)(v1.y+0.5)-(int)v3.y);
			float db1 = -(color1.z-color2.z) / (float)((int)(v1.y+0.5)-(int)v2.y); 
			float db2 = -(color1.z-color3.z) / (float)((int)(v1.y+0.5)-(int)v3.y);
			
			// if dx1 moves faster than dx2, swap all the values
			if (dx1 > dx2){
				float tmp = dx1;
				dx1 = dx2;
				dx2 = tmp;
				tmp = dz1;
				dz1 = dz2;
				dz2 = tmp;
				tmp = dr1;
				dr1 = dr2;
				dr2 = tmp;
				tmp = dg1;
				dg1 = dg2;
				dg2 = tmp;
				tmp = db1;
				db1 = db2;
				db2 = tmp;
			}
			
			// initialize x, z, r, g, and b
			float x1 = v1.x;
			float x2 = v1.x;
			float z1 = v1.z;
			float z2 = v1.z;
			float r1 = color1.x;
			float r2 = r1;
			float g1 = color1.y;
			float g2 = g1;
			float b1 = color1.z;
			float b2 = b1;
			
			// iterate through the ys from v1 to v2
			for (int i = (int)(v1.y+0.5); i >= (int)v2.y; i--){
				
				// if the y is outside the frame buffer, skip it
				if (i >= buff.getHeight() || i < 0){
					x1 += dx1;
					x2 += dx2;
					z1 += dz1;
					z2 += dz2;
					r1 += dr1;
					r2 += dr2;
					g1 += dg1;
					g2 += dg2;
					b1 += db1;
					b2 += db2;
					continue;
				}
				
				// calculate the change in z, r, g, and b as x changes
				float dz = (z1-z2) / (float)((int)x1-(int)(x2+0.5));
				float dr = (r1-r2) / (float)((int)x1-(int)(x2+0.5));
				float dg = (g1-g2) / (float)((int)x1-(int)(x2+0.5));
				float db = (b1-b2) / (float)((int)x1-(int)(x2+0.5));
				
				// if x does not change, none of the others do
				if ((int)x1-(int)(x2+0.5) == 0){
					dz = 0;
					dr = 0;
					dg = 0;
					db = 0;
				}
				
				// iterates through the pixels in the row to fill the triangle
				for (int j = (int)x1; j <= (int)(x2+0.5); j++){
					// if the x is outside the frame buffer, skip it
					if (j >= buff.getWidth() || j < 0)
						continue;
					
					// calculate the current z, r, g, and b
					float z = z1 + dz * (j-(int)x1);
					float r = r1 + dr * (j-(int)x1);
					r = Math.min(r, 1);
					float g = g1 + dg * (j-(int)x1);
					g = Math.min(g, 1);
					float b = b1 + db * (j-(int)x1);
					b = Math.min(b, 1);
					Point3D color = new Point3D(r, g, b);
					
					// get the current depth value of the pixel
					long depthval = 0xffffffffl & depth.getRGB(j, i);
					long zcomp = (long) (z * 0xffffffffl);
					
					// if the point is closer than the current pixel, draw it
					if (zcomp <= depthval){
						buff.setRGB(j, i, color.getBRGUint8());
						depth.setRGB(j, i, (int)zcomp);
					}
				}
				
				// update x, z, r, g, and b
				x1 += dx1;
				x2 += dx2;
				z1 += dz1;
				z2 += dz2;
				r1 += dr1;
				r2 += dr2;
				g1 += dg1;
				g2 += dg2;
				b1 += db1;
				b2 += db2;
			}
		}
		
		// if v2 and v3 don't form a line draw the upper triangle
		if ((int)v2.y != (int)v3.y){
			// calculate the change in x, z, r, g, and b as y changes
			float dx1 = (v3.x-v2.x) / (float)((int)v3.y-(int)v2.y);
			float dx2 = (v1.x-v3.x) / (float)((int)v1.y-(int)v3.y);
			float dz1 = (v3.z-v2.z) / (float)((int)v3.y-(int)v2.y);
			float dz2 = (v1.z-v3.z) / (float)((int)v1.y-(int)v3.y); 
			float dr1 = (color3.x-color2.x) / (float)((int)v3.y-(int)v2.y); 
			float dr2 = (color1.x-color3.x) / (float)((int)v1.y-(int)v3.y);
			float dg1 = (color3.y-color2.y) / (float)((int)v3.y-(int)v2.y); 
			float dg2 = (color1.y-color3.y) / (float)((int)v1.y-(int)v3.y);
			float db1 = (color3.z-color2.z) / (float)((int)v3.y-(int)v2.y); 
			float db2 = (color1.z-color3.z) / (float)((int)v1.y-(int)v3.y);
			
			// swap the values if dx1 is larger than dx2
			if (dx1 > dx2){
				float tmp = dx1;
				dx1 = dx2;
				dx2 = tmp;
				tmp = dz1;
				dz1 = dz2;
				dz2 = tmp;
				tmp = dr1;
				dr1 = dr2;
				dr2 = tmp;
				tmp = dg1;
				dg1 = dg2;
				dg2 = tmp;
				tmp = db1;
				db1 = db2;
				db2 = tmp;
			}
			
			// set the initial values of x, z, r, g, and b
			float x1 = v3.x;
			float x2 = v3.x;
			float z1 = v3.z;
			float z2 = v3.z;
			float r1 = color3.x;
			float r2 = r1;
			float g1 = color3.y;
			float g2 = g1;
			float b1 = color3.z;
			float b2 = b1;
			
			// iterate through the ys from v3 to v2
			for (int i = (int)v3.y; i <= (int)v2.y; i++){
				// if the y is outside the frame buffer, skip it
				if (i >= buff.getHeight() || i < 0){
					x1 += dx1;
					x2 += dx2;
					z1 += dz1;
					z2 += dz2;
					r1 += dr1;
					r2 += dr2;
					g1 += dg1;
					g2 += dg2;
					b1 += db1;
					b2 += db2;
					continue;
				}
				
				// calculate the change in x, z, r, g, and b
				float dz = (z1-z2) / (float)((int)x1-(int)(x2+0.5));
				float dr = (r1-r2) / (float)((int)x1-(int)(x2+0.5));
				float dg = (g1-g2) / (float)((int)x1-(int)(x2+0.5));
				float db = (b1-b2) / (float)((int)x1-(int)(x2+0.5));
				
				// if x does not change, neither do the other variables
				if ((int)x1-(int)(x2+0.5) == 0){
					dz = 0;
					dr = 0;
					dg = 0;
					db = 0;
				}
				
				// iterate through the row to fill the triangle
				for (int j = (int)x1; j <= (int)(x2+0.5); j++){
					// if the x is outside the frame buffer, skip it
					if (j >= buff.getWidth() || j < 0)
						continue;

					// calculate the current value of z, r, g, and b
					float r = r1 + dr * (j-(int)x1);
					r = Math.min(r, 1);
					float g = g1 + dg * (j-(int)x1);
					g = Math.min(g, 1);
					float b = b1 + db * (j-(int)x1);
					b = Math.min(b, 1);
					Point3D color = new Point3D(r, g, b);
					float z = z1 + dz * (j-(int)x1);
					
					// get the current depth value
					long depthval = 0xffffffffl & depth.getRGB(j, i);
					long zcomp = (long) (z * 0xffffffffl);
					// if the point is closer than the current pixel, draw it
					if (zcomp <= depthval){
						buff.setRGB(j, i, color.getBRGUint8());
						depth.setRGB(j, i, (int)zcomp);
					}
				}
				
				// update the x, z, r, g, and b values
				x1 += dx1;
				x2 += dx2;
				z1 += dz1;
				z2 += dz2;
				r1 += dr1;
				r2 += dr2;
				g1 += dg1;
				g2 += dg2;
				b1 += db1;
				b2 += db2;
			}
		}
	}

	// draw a triangle from the vertices array with the given indices using phong shading
	private void drawPhong(int i1, int i2, int i3){	
		
		// get the width and height for screen coordinate conversion
		int width = buff.getWidth();
		int height = buff.getHeight();
		
		Point3D v1 = vertices.get(i1).clone();
		Point3D v2 = vertices.get(i2).clone();
		Point3D v3 = vertices.get(i3).clone();
		Point3D n1 = normals.get(i1).clone();
		Point3D n2 = normals.get(i2).clone();
		Point3D n3 = normals.get(i3).clone();
		
		// if the points are all the same, don't draw them
		if (v1.equals(v2) || v2.equals(v3) || v3.equals(v1))
			return;
		
		// if all the vertices are facing away from the camera, don't draw them
		if (n1.z <= 0 && n2.z <= 0 && n3.z <= 0)
			return;
		
		// convert the vertices into screen coordinates
		v1.x = (v1.x * width/2 + width/2);
		v1.y = (v1.y * height/2 + height/2);
		v1.z = (v1.z / -maxdepth);
		v2.x = (v2.x * width/2 + width/2);
		v2.y = (v2.y * height/2 + height/2);
		v2.z = (v2.z / -maxdepth);
		v3.x = (v3.x * width/2 + width/2);
		v3.y = (v3.y * height/2 + height/2);
		v3.z = (v3.z / -maxdepth);
		
		// order the points from largest y to smallest y
		if (v2.y > v1.y){
			Point3D tmp = v1;
			v1 = v2;
			v2 = tmp;
			tmp = n1;
			n1 = n2;
			n2 = tmp;
		}
		if (v3.y > v2.y){
			Point3D tmp = v2;
			v2 = v3;
			v3 = tmp;
			tmp = n2;
			n2 = n3;
			n3 = tmp;
		}
		if (v2.y > v1.y){
			Point3D tmp = v1;
			v1 = v2;
			v2 = tmp;
			tmp = n1;
			n1 = n2;
			n2 = tmp;
		}
		
		// if v1 and v2 don't form a horizontal line, draw the bottom triangle
		if ((int)(v1.y+0.5) != (int)v2.y){
			
			// calculate the change in x, z, and the normal x, y, and z as y changes
			float dx1 = -(v1.x-v2.x) / (float)((int)(v1.y+0.5)-(int)v2.y);
			float dx2 = -(v1.x-v3.x) / (float)((int)(v1.y+0.5)-(int)v3.y);
			float dz1 = -(v1.z-v2.z) / (float)((int)(v1.y+0.5)-(int)v2.y);
			float dz2 = -(v1.z-v3.z) / (float)((int)(v1.y+0.5)-(int)v3.y); 
			float dnx1 = -(n1.x-n2.x) / (float)((int)(v1.y+0.5)-(int)v2.y); 
			float dnx2 = -(n1.x-n3.x) / (float)((int)(v1.y+0.5)-(int)v3.y);
			float dny1 = -(n1.y-n2.y) / (float)((int)(v1.y+0.5)-(int)v2.y); 
			float dny2 = -(n1.y-n3.y) / (float)((int)(v1.y+0.5)-(int)v3.y);
			float dnz1 = -(n1.z-n2.z) / (float)((int)(v1.y+0.5)-(int)v2.y); 
			float dnz2 = -(n1.z-n3.z) / (float)((int)(v1.y+0.5)-(int)v3.y);
			
			// swap the values if dx1 is larger than dx2
			if (dx1 > dx2){
				float tmp = dx1;
				dx1 = dx2;
				dx2 = tmp;
				tmp = dz1;
				dz1 = dz2;
				dz2 = tmp;
				tmp = dnx1;
				dnx1 = dnx2;
				dnx2 = tmp;
				tmp = dny1;
				dny1 = dny2;
				dny2 = tmp;
				tmp = dnz1;
				dnz1 = dnz2;
				dnz2 = tmp;
			}
			
			// set the initial values of x, z, and the normal x, y, and z
			float x1 = v1.x;
			float x2 = v1.x;
			float z1 = v1.z;
			float z2 = v1.z;
			float nx1 = n1.x;
			float nx2 = nx1;
			float ny1 = n1.y;
			float ny2 = ny1;
			float nz1 = n1.z;
			float nz2 = nz1;
			
			// iterate through the ys from v1 to v2
			for (int i = (int)(v1.y+0.5); i >= (int)v2.y; i--){
				// if the y is outside the frame buffer, skip it
				if (i >= buff.getHeight() || i < 0){
					x1 += dx1;
					x2 += dx2;
					z1 += dz1;
					z2 += dz2;
					nx1 += dnx1;
					nx2 += dnx2;
					ny1 += dny1;
					ny2 += dny2;
					nz1 += dnz1;
					nz2 += dnz2;
					continue;
				}
				
				// calculate the change in z, and the normal x, y, and z as x changes
				float dz = (z1-z2) / (float)((int)x1-(int)(x2+0.5));
				float dnx = (nx1-nx2) / (float)((int)x1-(int)(x2+0.5));
				float dny = (ny1-ny2) / (float)((int)x1-(int)(x2+0.5));
				float dnz = (nz1-nz2) / (float)((int)x1-(int)(x2+0.5));
				// if x does not change, neither do the others
				if ((int)x1-(int)(x2+0.5) == 0){
					dz = 0;
					dnx = 0;
					dny = 0;
					dnz = 0;
				}
				
				// iterate through the row pixels to fill the triangle
				for (int j = (int)x1; j <= (int)(x2+0.5); j++){
					// if the x is outside the frame buffer, skip it
					if (j >= buff.getWidth() || j < 0)
						continue;
					// get the current z and normal x, y, and z values
					float z = z1 + dz * (j-(int)x1);
					float nx = nx1 + dnx * (j-(int)x1);
					float ny = ny1 + dny * (j-(int)x1);
					float nz = nz1 + dnz * (j-(int)x1);
					
					// normalize the normal
					Point3D normal = new Point3D(nx, ny, nz);
					normal = normal.normalize();
					
					// convert the current x, y, and z to world coordinates
					float xpos = (j - width / 2) / (float) width * 2;
					float ypos = (i - height / 2) / (float) height * 2;
					float zpos = z * -maxdepth;
					Point3D location = new Point3D(xpos, ypos, zpos);
					// calculate the color of the current pixel
					Point3D color = computeColor(normal, location);
					
					// get the depth of the current pixel
					long depthval = 0xffffffffl & depth.getRGB(j, i);
					long zcomp = (long) (z * 0xffffffffl);
					// if the point is closer, draw it
					if (zcomp <= depthval){
						buff.setRGB(j, i, color.getBRGUint8());
						depth.setRGB(j, i, (int)zcomp);
					}
				}
				
				// update x, z, and the normal x, y, and z
				x1 += dx1;
				x2 += dx2;
				z1 += dz1;
				z2 += dz2;
				nx1 += dnx1;
				nx2 += dnx2;
				ny1 += dny1;
				ny2 += dny2;
				nz1 += dnz1;
				nz2 += dnz2;
			}
		}
		
		// if v2 and v3 are not a horizontal line, draw the upper triangle
		if ((int)v2.y != (int)v3.y){
			
			// get the change in x, z, and the normal x, y, and z as y changes
			float dx1 = (v3.x-v2.x) / (float)((int)v3.y-(int)v2.y);
			float dx2 = (v1.x-v3.x) / (float)((int)v1.y-(int)v3.y);
			float dz1 = (v3.z-v2.z) / (float)((int)v3.y-(int)v2.y);
			float dz2 = (v1.z-v3.z) / (float)((int)v1.y-(int)v3.y); 
			float dnx1 = (n3.x-n2.x) / (float)((int)v3.y-(int)v2.y); 
			float dnx2 = (n1.x-n3.x) / (float)((int)v1.y-(int)v3.y);
			float dny1 = (n3.y-n2.y) / (float)((int)v3.y-(int)v2.y); 
			float dny2 = (n1.y-n3.y) / (float)((int)v1.y-(int)v3.y);
			float dnz1 = (n3.z-n2.z) / (float)((int)v3.y-(int)v2.y); 
			float dnz2 = (n1.z-n3.z) / (float)((int)v1.y-(int)v3.y);
			
			// swap the values if dx1 is greater than dx2
			if (dx1 > dx2){
				float tmp = dx1;
				dx1 = dx2;
				dx2 = tmp;
				tmp = dz1;
				dz1 = dz2;
				dz2 = tmp;
				tmp = dnx1;
				dnx1 = dnx2;
				dnx2 = tmp;
				tmp = dny1;
				dny1 = dny2;
				dny2 = tmp;
				tmp = dnz1;
				dnz1 = dnz2;
				dnz2 = tmp;
			}
			
			// initialize the x, y, and normal x, y, and z values
			float x1 = v3.x;
			float x2 = v3.x;
			float z1 = v3.z;
			float z2 = v3.z;
			float nx1 = n3.x;
			float nx2 = nx1;
			float ny1 = n3.y;
			float ny2 = ny1;
			float nz1 = n3.z;
			float nz2 = nz1;
			
			// iterate through the ys from v3 to v2
			for (int i = (int)v3.y; i <= (int)v2.y; i++){
				// if the y is outside the frame buffer, skip it
				if (i >= buff.getHeight() || i < 0){
					x1 += dx1;
					x2 += dx2;
					z1 += dz1;
					z2 += dz2;
					nx1 += dnx1;
					nx2 += dnx2;
					ny1 += dny1;
					ny2 += dny2;
					nz1 += dnz1;
					nz2 += dnz2;
					continue;
				}
				
				// get the change in z and the normal x, y, and z as x changes
				float dz = (z1-z2) / (float)((int)x1-(int)(x2+0.5));
				float dnx = (nx1-nx2) / (float)((int)x1-(int)(x2+0.5));
				float dny = (ny1-ny2) / (float)((int)x1-(int)(x2+0.5));
				float dnz = (nz1-nz2) / (float)((int)x1-(int)(x2+0.5));
				
				// if x doesn't change, neither do the other variables
				if ((int)x1-(int)(x2+0.5) == 0){
					dz = 0;
					dnx = 0;
					dny = 0;
					dnz = 0;
				}
				
				// iterate through the pixels in the row to fill the triangle
				for (int j = (int)x1; j <= (int)(x2+0.5); j++){
					// if the x is outside the frame buffer, skip it
					if (j >= buff.getWidth() || j < 0)
						continue;

					// calculate the current z and normal x, y, and z
					float z = z1 + dz * (j-(int)x1);
					float nx = nx1 + dnx * (j-(int)x1);
					float ny = ny1 + dny * (j-(int)x1);
					float nz = nz1 + dnz * (j-(int)x1);
					
					// normalize the new normal
					Point3D normal = new Point3D(nx, ny, nz);
					normal = normal.normalize();
					
					// convert the current location back into world coordinates for lighting
					float xpos = (j - width / 2) / (float) width * 2;
					float ypos = (i - height / 2) / (float) height * 2;
					float zpos = z * -maxdepth;
					Point3D location = new Point3D(xpos, ypos, zpos);
					// calculate the lighting of the point
					Point3D color = computeColor(normal, location);
					
					// get the depth of the current pixel
					long depthval = 0xffffffffl & depth.getRGB(j, i);
					long zcomp = (long) (z * 0xffffffffl);
					// if the point is closer to the camera, draw it
					if (zcomp <= depthval){
						buff.setRGB(j, i, color.getBRGUint8());
						depth.setRGB(j, i, (int)zcomp);
					}
				}
				
				// update the x, z, and normal x, y, and z values
				x1 += dx1;
				x2 += dx2;
				z1 += dz1;
				z2 += dz2;
				nx1 += dnx1;
				nx2 += dnx2;
				ny1 += dny1;
				ny2 += dny2;
				nz1 += dnz1;
				nz2 += dnz2;
			}
		}
	}

	// draws a triangle to the screen using the proper shading
	private void drawTriangle(int index1, int index2, int index3){
		Point3D v1 = vertices.get(index1);
		Point3D v2 = vertices.get(index2);
		Point3D v3 = vertices.get(index3);
		
		// if the triangle is completely outside the field of view, don't draw it
		if (v1.x < -1 && v2.x < -1 && v3.x < -1)
			return;
		if (v1.x > 1 && v2.x > 1 && v3.x > 1)
			return;
		if (v1.y < -1 && v2.y < -1 && v3.y < -1)
			return;
		if (v1.y > 1 && v2.y > 1 && v3.y > 1)
			return;
		if (v1.z > 0 && v2.z > 0 && v3.z > 0)
			return;
		if (v1.z < -maxdepth && v2.z < -maxdepth && v3.z < -maxdepth)
			return;
		
		// draw the triangle using the proper shading
		switch(shadestyle){
		case FLAT:
			drawFlat(index1, index2, index3);
			break;
		case GOURAUD:
			drawGouraud(index1, index2, index3);
			break;
		case PHONG:
			drawPhong(index1, index2, index3);
			break;
		}
	}

	// clears the depth buffer
	public void clearDepth(){
		
		Graphics2D g = depth.createGraphics();
		g.setBackground(Color.white);
	    g.clearRect(0, 0, depth.getWidth(), depth.getHeight());
	    g.dispose();
	}
	
	// clears the frame buffer, and removes all the lights
	public void clearColor(){
		Graphics2D g = buff.createGraphics();
	    g.setColor(Color.BLACK);
	    g.fillRect(0, 0, buff.getWidth(), buff.getHeight());
	    g.dispose();
	    
	    lighttable.clear();
	}
	
	// push the current transformation onto the transformation stack
	public void pushTransform(){
		transformstack.push(transform.clone());
	}

	// pop the top of the transformation stack into the current transformation matrix
	public void popTransform(){
		transform = transformstack.pop();
	}
	
	/*public void setBumpTex(Point3D tex){
		curbumptex = tex.clone();
	}*/
	
	// set the value of the current normal
	public void setNormal(Point3D normal){
		curnormal = normal;
	}
	
	// adds a vertex to the vertex array and draws it if there are enough triangles for the current triangle type
	public void addVertex(Point3D vertex){
		
		// transforms the vertex and current normal by the current transformation
		Point3D newvertex = transform.multiply(vertex);
		Point3D newnormal = transform.normmult(curnormal);
		newnormal = newnormal.normalize();
		
		// adds the vertex and normal to the arrays
		vertices.add(newvertex);
		normals.add(newnormal);
		//bumpcoord.add(curbumptex);
		
		int size = vertices.size();
		switch (curtype){
		case TRIANGLE:
			if (size % 3 == 0){ // draws the the triangle for every set of three vertices given
				drawTriangle(size-3, size-2, size-1);
			}
			break;
		case TRIANGLE_STRIP:
			if (size >= 3){ // draw the triangle using the current vertex and the previous three for every vertex given once there are three
				if (size %2 == 0) // ensures vertices will be given in the right order for normal calculation
					drawTriangle(size-3, size-2, size-1);
				else
					drawTriangle(size-1, size-2, size-3);
			}
			break;
		case TRIANGLE_FAN:
			if (size >= 3){ // draw the triangle using the first vertex and the previous 2 for every vertex given once there are three vertices
				drawTriangle(0, size-2, size-1);
			}
			break;
		default:
			break;	
		}
	}

	// tells the sketch base that a triangle fan will be drawn
	public void beginTriangleFan(){
		vertices.clear(); // clears all the vertices and normals
		normals.clear();
		curtype = Type.TRIANGLE_FAN; // sets the type
	}
	
	// tells the sketch base that a triangle strip will be drawn
	public void beginTriangleStrip(){
		vertices.clear(); // clears the vertices and normals
		normals.clear();
		curtype = Type.TRIANGLE_STRIP; // sets the type to triangle strip
	}
	
	// tells the sketch base that regular triangles will be drawn
	public void beginTriangle(){
		vertices.clear(); // clears the vertices and normals
		normals.clear();
		curtype = Type.TRIANGLE; // sets the type
	}
	
	// tells the sketch base that the program is done drawing
	public void end(){
		vertices.clear();
		normals.clear();
		curtype = Type.NONE;
	}
}
