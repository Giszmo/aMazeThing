import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author leo
 * generates a Labyrinth with a given <em>width</em>, <em>height</em> and a 
 * statistic measure of the wall building <em>biasToLongWalls</em>.
 * There are 2 sets of png tiles for the html-output. <em>size</em> set to 25,
 * 50 or 100 avoids scaling and uses the pre-scaled ones resulting in 15% file
 * size.
 */
public class Labyrinth {
	private int width;
	private int height;
	private int entryTop;
	private int entryBottom;
	private static final int SOUTH=1;
	private static final int NORTH=2;
	private static final int EAST=4;
	private static final int WEST=8;
	private static final int NOTFREE=16;
	private static String sSrc = "";
	private static String sScale = "";
	private static boolean debug = true;
	private DebugInfo [][] debugInfo;
	private int biasToLongWalls;
	/**
	 * grid of possible wall intersections
	 */
	private int [][] grid;
	/**
	 * for debugging this grid contains the order of the above grid
	 * @author leo
	 *
	 */
	public class DebugInfo {
		public class Color {
			public int r;
			public int g;
			public int b;
			public String toString(int alpha){
				alpha=Math.min(255,Math.max(16,alpha));
				return "#"+String.format("%02X",r*alpha/256)+String.format("%02X",g*alpha/256)+String.format("%02X",b*alpha/256);
			}
			public void setRandomColor() {
				r=(int) (Math.random()*256);
				g=(int) (Math.random()*256);
				b=(int) (Math.random()*256);
				//normalize
				float max=Math.max(Math.max(r,g),b);
				float min=Math.min(Math.min(r,g),b);
				float mult=256/(max-min);
				r=(int) ((r-min)*mult);
				g=(int) ((g-min)*mult);
				b=(int) ((b-min)*mult);
			}
		}
		public Color c;
		public int step;
		DebugInfo() {
			c=new Color();
		}
	}
	/**
	 * little helper to check the input values
	 * @param a lower valid bound
	 * @param b upper valid bound
	 * @param value actual value
	 * @param name name to be mentioned if value exceeds the bounds
	 */
	private static void assertIntervall(int a, int b, int value, String name) {
		if(value<a || value>b) {
			System.out.println(name+" must be between "+a+" and "+b);
			System.exit(-1);
		}
	}
	/**
	 * The constructor prepares all variables but does not yet calculate an actual labyrinth
	 * @param width width of the labyrinth must be between 2 (even 2 will not be
     * enough to get a labyrinth but the result is generatable) and 100 (to
     * limit the output size)
	 * @param height height of the labyrinth must be between 2 (even 2 will not
	 * be enough to get a labyrinth but the result is generatable) and 100 (to 
	 * limit the output size)
	 * @param biasToLongWalls a statistical parameter that is used to bias the
	 * pick of a wall to build from towards lately built walls. The formula
	 * chosen to solve this is <i>1-abs(2*RND*RND* <small>(<em>biasToLongWalls</em> times)</small>-1)</i>
	 * where <i>RND in [0;1[</i>.
	 * @param scale
	 */
	public Labyrinth(int width, int height, int biasToLongWalls, int scale) {
		assertIntervall( 2,100,width,"width");
		assertIntervall( 2,100,height,"height");
		assertIntervall( 1, 20,biasToLongWalls,"biasToLongWalls");
		assertIntervall(10,200,scale,"scale");
		this.biasToLongWalls=biasToLongWalls;
		this.width=width;
		this.height=height;
		this.entryTop=0;//(int) (Math.random()*(width-1));
		this.entryBottom=width-2;//(int) (Math.random()*(width-1));
		if (scale==25) {
			sSrc="xs";
		} else if (scale==50) {
			sSrc="s";
		} else if (scale==100) {
		} else if (scale<25) {
			sSrc="xs";
			sScale=" style=\"width:"+scale+";height:"+scale+";\"";
		} else if (scale<50) {
			sSrc="s";
			sScale=" style=\"width:"+scale+";height:"+scale+";\"";
		} else {
			sScale=" style=\"width:"+scale+";height:"+scale+";\"";
		}
		grid=new int[width][height];
		debugInfo=new DebugInfo[width][height];
		for (int x=0; x<width;x++)
			for (int y=0; y<height; y++)
				debugInfo[x][y]=new DebugInfo();
	}
	public String picString(int i) {
		switch (i) {
		case 0: return "0.png";

		case NORTH: return "N.png";
		case SOUTH: return "S.png";
		case EAST: return "E.png";
		case WEST: return "W.png";

		case NORTH | SOUTH: return "SN.png";
		case EAST | WEST: return "EW.png";
		case NORTH | EAST: return "EN.png";
		case NORTH | WEST: return "WN.png";
		case WEST | SOUTH: return "WS.png";
		case EAST | SOUTH: return "ES.png";

		case SOUTH | EAST | WEST: return "EWS.png";
		case NORTH | EAST | WEST: return "EWN.png";
		case NORTH | SOUTH | WEST: return "WSN.png";
		case NORTH | SOUTH | EAST: return "ESN.png";

		case NORTH | SOUTH | EAST | WEST: return "EWSN.png";
		default: return ""+i;
		}

	}
	public String toString() {
		String retVal = ("<br /><br />");
		retVal+=("<table border=0 cellpadding=0 cellspacing=0>");
		for(int i=0; i<height; i++) {
			retVal+=("<tr>");
			for(int j=0;j<width;j++) {
				retVal+="<td";
				if(debug) {
					DebugInfo.Color c=debugInfo[j][i].c;
					String style="background-color:"+c.toString((255-debugInfo[j][i].step*4)%256)+";";
					retVal+=" style=\""+style+"\"";
				}
				retVal+=">";
				//es interessiert nur die richtung:
				int toDisplay=grid[j][i]&(NORTH | SOUTH | EAST | WEST);
				if(j==0)
					toDisplay^=WEST;
				if(j==width-1)
					toDisplay^=EAST;
				if(i==0) {
					toDisplay^=NORTH;
					if(j==entryTop)
						toDisplay^=EAST;
					if(j==entryTop+1)
						toDisplay^=WEST;
				}
				if(i==height-1) {
					toDisplay^=SOUTH;
					if(j==entryBottom)
						toDisplay^=EAST;
					if(j==entryBottom+1)
						toDisplay^=WEST;
				}
				retVal+="<img src=\"data/"+sSrc+picString(toDisplay)+"\""+sScale+" /></td>";
			}
			retVal+=("</tr>");
		}
		retVal+=("</table>");
		return retVal;
	}
	public String getGrid() {
		String retVal="";
		int freeNodeCount=0;
		///Knoten, die noch wachsen können. Array maximal großzügig dimensioniert.
		int [] freeNode = new int[width*height];
		//init
		//leeres grid innen
		for (int i=0; i<width; i++ ) {
			for (int j=0; j<height;j++) {
				grid[i][j]=0;
			}
		}
		//oben und unten wand. nach innen offen
		for (int i=1;i<width-1 ;i++ ) {
			grid[i][0]       = WEST | EAST | NORTH;
			grid[i][height-1]= WEST | EAST | SOUTH;
			freeNode[freeNodeCount++]= i;
			freeNode[freeNodeCount++]=(height-1)*width+i;
		}
		//rechts und links wand. nach innen offen
		for (int j=1;j<height-1 ;j++ ) {
			grid[0][j]       = WEST | SOUTH | NORTH;
			grid[width-1][j] = NORTH | EAST | SOUTH;
			freeNode[freeNodeCount++]=width*j;
			freeNode[freeNodeCount++]=width*j+width-1;
		}
		//in den ecken wand
		int [][] pos={{0,0},{width-1,0},{0,height-1},{width-1,height-1}};
		for (int i=0; i<4; i++) {
			grid     [pos[i][0]][pos[i][1]]=EAST | SOUTH | WEST  | NORTH;
			debugInfo[pos[i][0]][pos[i][1]].c.setRandomColor();
			debugInfo[pos[i][0]][pos[i][1]].step=0;
		}
		
		//as freenode is read from with a tendency to read from the end to get longer branches,
		//the border nodes need shuffling
		shuffle(freeNode,freeNodeCount);
		
		for(int i=0;i<freeNodeCount;i++) {
			debugInfo[freeNode[i]%width][freeNode[i]/width].c.setRandomColor();
			debugInfo[freeNode[i]%width][freeNode[i]/width].step=0;
		}
		while (freeNodeCount>0) {
			double smoothedRandom=.999999999999;
			for(int i=0;i<biasToLongWalls;i++)
				smoothedRandom*=Math.random();
			double randomPick=1-Math.abs(2*smoothedRandom-1);
			int freenodeid=(int) (randomPick*freeNodeCount);
			int nodeId=freeNode[freenodeid];
			int nodeX=freeNode[freenodeid]%width;
			int nodeY=freeNode[freenodeid]/width;
			int direction = (1 << (int)(Math.random()*4));
			switch (direction) {
			//nicht rauf und oben noch frei
			case NORTH:
				if (0==(grid[nodeX][nodeY] & NORTH) && 0==(grid[nodeX][nodeY-1])) {
					grid[nodeX][nodeY] |= NORTH;
					grid[nodeX][nodeY-1] |= SOUTH;
					freeNode[freeNodeCount++]=nodeId-width;
					if(debug){
						debugInfo[nodeX][nodeY-1].c=debugInfo[nodeX][nodeY].c;
						debugInfo[nodeX][nodeY-1].step=debugInfo[nodeX][nodeY].step+1;
					}
				}
				break;
			case SOUTH:
				if (0==(grid[nodeX][nodeY] & SOUTH) && 0==(grid[nodeX][nodeY+1])) {
					grid[nodeX][nodeY] |= SOUTH;
					grid[nodeX][nodeY+1] |= NORTH;
					freeNode[freeNodeCount++]=nodeId+width;
					if(debug){
						debugInfo[nodeX][nodeY+1].c=debugInfo[nodeX][nodeY].c;
						debugInfo[nodeX][nodeY+1].step=debugInfo[nodeX][nodeY].step+1;
					}
				}
				break;
			case WEST:
				if (0==(grid[nodeX][nodeY] & WEST) && 0==(grid[nodeX-1][nodeY])) {
					grid[nodeX][nodeY] |= WEST;
					grid[nodeX-1][nodeY] |= EAST;
					freeNode[freeNodeCount++]=nodeId-1;
					if(debug){
						debugInfo[nodeX-1][nodeY].c=debugInfo[nodeX][nodeY].c;
						debugInfo[nodeX-1][nodeY].step=debugInfo[nodeX][nodeY].step+1;
					}
				}
				break;
			case EAST:
				if (0==(grid[nodeX][nodeY] & EAST) && 0==(grid[nodeX+1][nodeY])) {
					grid[nodeX][nodeY] |= EAST;
					grid[nodeX+1][nodeY] |= WEST;
					freeNode[freeNodeCount++]=nodeId+1;
					if(debug){
						debugInfo[nodeX+1][nodeY].c=debugInfo[nodeX][nodeY].c;
						debugInfo[nodeX+1][nodeY].step=debugInfo[nodeX][nodeY].step+1;
					}
				}
				break;
			}
			//test, ob der aktuelle node noch wachsen kann.
			if((0!=(grid[nodeX][nodeY] & NORTH)   || 0!=(grid[nodeX][nodeY-1])) &&
					(0!=(grid[nodeX][nodeY] & SOUTH) || 0!=(grid[nodeX][nodeY+1])) &&
					(0!=(grid[nodeX][nodeY] & WEST)  || 0!=(grid[nodeX-1][nodeY])) &&
					(0!=(grid[nodeX][nodeY] & EAST) || 0!=(grid[nodeX+1][nodeY])) ) {
				//				retVal+=((freenode[freenodeid]%GridWidth) << "/" << (freenode[freenodeid]/GridWidth) << " ";
				grid[nodeX][nodeY] |= NOTFREE;
				freeNode[freenodeid]=freeNode[--freeNodeCount];
			}
		}
		retVal+=toString();
		return retVal;
	}
	//shuffling like that is not 100% random but easy
	private void shuffle(int[] freeNode, int freeNodeCount) {
		for(int i=0; i<freeNodeCount; swap(freeNode,i++,(int) (freeNodeCount*Math.random())));
		
	}
	private void swap(int[] freeNode, int swap, int i) {
			int tmp=freeNode[swap];
			freeNode[swap]=freeNode[i];
			freeNode[i]=tmp;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Labyrinth l=new Labyrinth(25,20,5,10);
		File f=new File("test.html");
		try {
			FileOutputStream fos=new FileOutputStream(f);
			fos.write("<html><body>".getBytes());
			Labyrinth.debug=true;
			for (int i=0; i<5; i++)
				fos.write(("<h1>Labyrinth("+l.width+","+l.height+")</h1>"+l.getGrid()).getBytes());
			Labyrinth.debug=false;
			for (int i=0; i<5; i++)
				fos.write(("<h1>Labyrinth("+l.width+","+l.height+")</h1>"+l.getGrid()).getBytes());
			
			fos.write("</body></html>".getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
