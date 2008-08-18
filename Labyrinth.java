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
	private String sSrc = "";
	private String sScale = "";
	private boolean colorful = false;
	private DebugInfo [][] colorInfo;
	/**
	 * 1 = evenly choose a wall segment to extend from. >1 = prefere wall
	 * segments built later and thus prefere getting long walls. 
	 */
	private int biasToLongWalls;
	/**
	 * grid of possible wall intersections
	 */
	private int [][] grid;
	/**
	 * for debugging this grid contains the build order of the above grid such
	 * as the color of the original outer wall segment. 
	 * @author leo
	 */
	public class DebugInfo {
		public class Color {
			public int r;
			public int g;
			public int b;
			/**
			 * @param brightnes a value between 0 and 255 that the rgb-values
			 * get scaled with
			 * @return a 6 digit hex representation as used in html.<br>
			 * Example: #FF3300
			 */
			public String toString(int brightnes){
				brightnes=Math.min(255,Math.max(16,brightnes));
				return "#"
				+String.format("%02X",r*brightnes/256)
				+String.format("%02X",g*brightnes/256)
				+String.format("%02X",b*brightnes/256);
			}
			/**
			 * sets RGB to a random value normalized such that one component 
			 * will be 00, another will be FF and the third something inbetween
			 */
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
	public Labyrinth(int width, int height, int biasToLongWalls, int scale, boolean colorful) {
		assertIntervall( 2,100,width,"width");
		assertIntervall( 2,100,height,"height");
		assertIntervall( -20, 40,biasToLongWalls,"biasToLongWalls");
		assertIntervall(10,200,scale,"scale");
		this.biasToLongWalls=biasToLongWalls;
		this.width=width;
		this.height=height;
		this.entryTop=0;//(int) (Math.random()*(width-1));
		this.entryBottom=width-2;//(int) (Math.random()*(width-1));
		this.colorful=colorful;
		this.sSrc="";
		this.sScale="";
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
		colorInfo=new DebugInfo[width][height];
		for (int x=0; x<width;x++)
			for (int y=0; y<height; y++)
				colorInfo[x][y]=new DebugInfo();
	}
	/**
	 * Maps the direction-bit-mask to a fitting image's name.
	 * @param direction is a bit mask representing N, S, E and W walls.
	 * @return name of an image
	 */
	public String picString(int direction) {
		switch (direction) {
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
		default: return ""+direction;
		}
	}
	/**
	 * @return html-table to visualize the labyrinth
	 */
	public String toString() {
		String retVal = "";
		retVal+=("<table border=0 cellpadding=0 cellspacing=0>");
		for(int i=0; i<height; i++) {
			retVal+=("<tr>");
			for(int j=0;j<width;j++) {
				retVal+="<td";
				if(colorful) {
					DebugInfo.Color c=colorInfo[j][i].c;
					String style="background-color:"+c.toString((255-colorInfo[j][i].step*4)%256)+";";
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
	/**
	 * generates a new random labyrinth
	 */
	public void generate() {
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
			colorInfo[pos[i][0]][pos[i][1]].c.setRandomColor();
			colorInfo[pos[i][0]][pos[i][1]].step=0;
		}
		
		//as freenode is read from with a tendency to read from the end to get longer branches,
		//the border nodes need shuffling
		shuffle(freeNode,freeNodeCount);
		
		for(int i=0;i<freeNodeCount;i++) {
			colorInfo[freeNode[i]%width][freeNode[i]/width].c.setRandomColor();
			colorInfo[freeNode[i]%width][freeNode[i]/width].step=0;
		}
		while (freeNodeCount>0) {
			double smoothedRandom=0;
			double randomPick;
			if(biasToLongWalls>0) {
				for(int i=0;i<Math.abs(biasToLongWalls);i++)
					smoothedRandom+=Math.random();
				smoothedRandom/=Math.abs(biasToLongWalls);
				randomPick=1-Math.abs(2*smoothedRandom-1);
			} else if(biasToLongWalls==0)
				randomPick=0.9999;//not very random
			else
				randomPick=0;//not very random
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
					if(colorful){
						colorInfo[nodeX][nodeY-1].c=colorInfo[nodeX][nodeY].c;
						colorInfo[nodeX][nodeY-1].step=colorInfo[nodeX][nodeY].step+1;
					}
				}
				break;
			case SOUTH:
				if (0==(grid[nodeX][nodeY] & SOUTH) && 0==(grid[nodeX][nodeY+1])) {
					grid[nodeX][nodeY] |= SOUTH;
					grid[nodeX][nodeY+1] |= NORTH;
					freeNode[freeNodeCount++]=nodeId+width;
					if(colorful){
						colorInfo[nodeX][nodeY+1].c=colorInfo[nodeX][nodeY].c;
						colorInfo[nodeX][nodeY+1].step=colorInfo[nodeX][nodeY].step+1;
					}
				}
				break;
			case WEST:
				if (0==(grid[nodeX][nodeY] & WEST) && 0==(grid[nodeX-1][nodeY])) {
					grid[nodeX][nodeY] |= WEST;
					grid[nodeX-1][nodeY] |= EAST;
					freeNode[freeNodeCount++]=nodeId-1;
					if(colorful){
						colorInfo[nodeX-1][nodeY].c=colorInfo[nodeX][nodeY].c;
						colorInfo[nodeX-1][nodeY].step=colorInfo[nodeX][nodeY].step+1;
					}
				}
				break;
			case EAST:
				if (0==(grid[nodeX][nodeY] & EAST) && 0==(grid[nodeX+1][nodeY])) {
					grid[nodeX][nodeY] |= EAST;
					grid[nodeX+1][nodeY] |= WEST;
					freeNode[freeNodeCount++]=nodeId+1;
					if(colorful){
						colorInfo[nodeX+1][nodeY].c=colorInfo[nodeX][nodeY].c;
						colorInfo[nodeX+1][nodeY].step=colorInfo[nodeX][nodeY].step+1;
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
//				grid[nodeX][nodeY] |= NOTFREE;
				for(int i=freenodeid;i<freeNodeCount-1;i++)
					freeNode[i]=freeNode[i+1];
				freeNodeCount--;
			}
		}
	}
	/**
	 * This shuffler should do a fair shuffle according to http://www.cigital.com/papers/download/developer_gambling.pdf
	 * @param freeNode
	 * @param freeNodeCount shuffle only shuffles the first <em>freeNodeCount</em> elements of <em>freeNode</em>
	 */
	private void shuffle(int[] freeNode, int freeNodeCount) {
		for (int i=0; i<freeNodeCount; i++)
			swap(freeNode,i,i+(int) ((freeNodeCount-i)*Math.random()));
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
		File f=new File("test.html");
		try {
			FileOutputStream fos=new FileOutputStream(f);
			fos.write("<html><head><title>Labyrinth results</title><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"></head><body>".getBytes());
			
			Labyrinth l=new Labyrinth(2,2,5,50,false);
			l.generate();
			fos.write(("<br /><br /><h1>For Beginners I</h1>"+l).getBytes());
			
			l=new Labyrinth(15,2,5,50,false);
			l.generate();
			fos.write(("<br /><br /><h1>For Beginners II</h1>"+l).getBytes());
			
			l=new Labyrinth(10,10,5,100,false);
			l.generate();
			fos.write(("<br /><br /><h1>For Moles</h1>"+l).getBytes());
			
			l=new Labyrinth(40,40,1,25,true);
			l.generate();
			fos.write(("<br /><br /><h1>For Beginners III (very short walls)</h1>As statistic effects become more obvious at larger scales, this is a 40x40 labyrinth. "+l).getBytes());
			
			l=new Labyrinth(20,20,-1,50,true);
			l.generate();
			fos.write(("<br /><br /><h1>What if we always pick the wall to extend that's not been touched the longest?</h1>"+l).getBytes());
			
			l=new Labyrinth(20,20,0,50,true);
			l.generate();
			fos.write(("<br /><br /><h1>For Beginners IV (very very long walls)</h1>This is a bit off the sceme of the other examples as here explicitly only the last built wall gets extended. A too strong bias to the lastly built walls can yield the same effect while in some cases it may result in very difficult labyrinths."+l).getBytes());
			
			l=new Labyrinth(20,20,10,50,true);
			l.generate();
			fos.write("<br /><br /><h1>My pick for wall length. Some with some without color to test ;)</h1>".getBytes());
			for(boolean j:new boolean [] {true,false}) {
				l.colorful=j;
				for (int i=0; i<5; i++) {
					l.generate();
					fos.write(("<br /><br />"+l).getBytes());
				}
			}
			fos.write("</body></html>".getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
