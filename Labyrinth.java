import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 */

/**
 * @author leo
 *
 */
public class Labyrinth {
	private int width;
	private int height;
	private static final int RUNTER=1;
	private static final int RAUF=2;
	private static final int RECHTS=4;
	private static final int LINKS=8;
	private static final int NOTFREE=16;
	private static final boolean debug = false;

	private int [][] grid;

	public Labyrinth(int width, int height) {
		this.width=width;
		this.height=height;
		grid=new int[width][height];
	}
	public String toString() {
		String retVal = ("<br /><br />");
		retVal+=("<table border=0 cellpadding=0 cellspacing=0>");
		for(int i=0; i<height; i++) {
			retVal+=("<tr>");
			for(int j=0;j<width;j++) {
				switch (grid[j][i]) {
				case 0: retVal+=("<td></td>"); break;

				case RAUF: retVal+=("<td>&#9474;</td>"); break;
				case RUNTER: retVal+=("<td>&#9474;</td>"); break;
				case RECHTS: retVal+=("<td>&#9472;</td>"); break;
				case LINKS: retVal+=("<td>&#9472;</td>"); break;

				case RAUF | RUNTER: retVal+=("<td>&#9474;</td>"); break;
				case RECHTS | LINKS: retVal+=("<td>&#9472;</td>"); break;
				case RAUF | RECHTS: retVal+=("<td>&#9492;</td>"); break;
				case RAUF | LINKS: retVal+=("<td>&#9496;</td>"); break;
				case LINKS | RUNTER: retVal+=("<td>&#9488;</td>"); break;
				case RECHTS | RUNTER: retVal+=("<td>&#9484;</td>"); break;

				case RUNTER | RECHTS | LINKS: retVal+=("<td>&#9516;</td>"); break;
				case RAUF | RECHTS | LINKS: retVal+=("<td>&#9524;</td>"); break;
				case RAUF | RUNTER | LINKS: retVal+=("<td>&#9508;</td>"); break;
				case RAUF | RUNTER | RECHTS: retVal+=("<td>&#9500;</td>"); break;

				case RAUF | RUNTER | RECHTS | LINKS: retVal+=("<td>&#9532;</td>"); break;

				case NOTFREE | 0: retVal+=("<td style=color:#FF0000>0</td>"); break;

				case NOTFREE | RAUF: retVal+=("<td style=color:#FF0000>&#9474;</td>"); break;
				case NOTFREE | RUNTER: retVal+=("<td style=color:#FF0000>&#9474;</td>"); break;
				case NOTFREE | RECHTS: retVal+=("<td style=color:#FF0000>&#9472;</td>"); break;
				case NOTFREE | LINKS: retVal+=("<td style=color:#FF0000>&#9472;</td>"); break;

				case NOTFREE | RAUF | RUNTER: retVal+=("<td style=color:#FF0000>&#9474;</td>"); break;
				case NOTFREE | RECHTS | LINKS: retVal+=("<td style=color:#FF0000>&#9472;</td>"); break;
				case NOTFREE | RAUF | RECHTS: retVal+=("<td style=color:#FF0000>&#9492;</td>"); break;
				case NOTFREE | RAUF | LINKS: retVal+=("<td style=color:#FF0000>&#9496;</td>"); break;
				case NOTFREE | LINKS | RUNTER: retVal+=("<td style=color:#FF0000>&#9488;</td>"); break;
				case NOTFREE | RECHTS | RUNTER: retVal+=("<td style=color:#FF0000>&#9484;</td>"); break;

				case NOTFREE | RUNTER | RECHTS | LINKS: retVal+=("<td style=color:#FF0000>&#9516;</td>"); break;
				case NOTFREE | RAUF | RECHTS | LINKS: retVal+=("<td style=color:#FF0000>&#9524;</td>"); break;
				case NOTFREE | RAUF | RUNTER | LINKS: retVal+=("<td style=color:#FF0000>&#9508;</td>"); break;
				case NOTFREE | RAUF | RUNTER | RECHTS: retVal+=("<td style=color:#FF0000>&#9500;</td>"); break;

				case NOTFREE | RAUF | RUNTER | RECHTS | LINKS: retVal+=("<td style=color:#FF0000>&#9532;</td>"); break;

				default: retVal+=(grid[i*width+j]);
				}
			}
			retVal+=("</tr>");
		}
		retVal+=("</table>");
		return retVal;
	}
	String getGrid() {
		String retVal="";
		int freeNodeCount=0;
		///Knoten, die noch wachsen können
		int [] freeNode = new int[width*height];
		boolean newline = true;
		for (int i=0; i<width; i++ ) { //leeres grid innen
			for (int j=0; j<height;j++) {
				grid[i][j]=0;
			}
		}
		for (int i=1;i<width-1 ;i++ ) {//oben und unten wand
			grid[i][0]       = LINKS | RECHTS | RAUF;
			grid[i][height-1]= LINKS | RECHTS | RUNTER;
			freeNode[freeNodeCount++]= i;
			freeNode[freeNodeCount++]=(height-1)*width+i;
		}
		for (int j=1;j<height-1 ;j++ ) {//rechts und links wand
			grid[0][j]       = LINKS | RUNTER | RAUF;
			grid[width-1][j] = RAUF | RECHTS | RUNTER;
			freeNode[freeNodeCount++]=width*j;
			freeNode[freeNodeCount++]=width*j+width-1;
		}
		//in den ecken wand
		grid[0      ][0       ]=RECHTS | RUNTER;
		grid[width-1][0       ]=LINKS  | RUNTER;
		grid[0      ][height-1]=RECHTS | RAUF;
		grid[width-1][height-1]=LINKS  | RAUF;

		while (freeNodeCount>0) {
			int freenodeid=(int) (Math.random()*freeNodeCount);
			int nodeId=freeNode[freenodeid];
			int nodeX=freeNode[freenodeid]%width;
			int nodeY=freeNode[freenodeid]/width;
			int direction = (1 << (int)(Math.random()*4));
			switch (direction) {
			//nicht rauf und oben noch frei
			case RAUF:
				if (0==(grid[nodeX][nodeY] & RAUF) && 0==(grid[nodeX][nodeY-1])) {
					grid[nodeX][nodeY] |= RAUF;
					grid[nodeX][nodeY-1] |= RUNTER;
					freeNode[freeNodeCount++]=nodeId-width;
					newline=true;
				}
				break;
			case RUNTER:
				if (0==(grid[nodeX][nodeY] & RUNTER) && 0==(grid[nodeX][nodeY+1])) {
					grid[nodeX][nodeY] |= RUNTER;
					grid[nodeX][nodeY+1] |= RAUF;
					freeNode[freeNodeCount++]=nodeId+width;
					newline=true;
				}
				break;
			case LINKS:
				if (0==(grid[nodeX][nodeY] & LINKS) && 0==(grid[nodeX-1][nodeY])) {
					grid[nodeX][nodeY] |= LINKS;
					grid[nodeX-1][nodeY] |= RECHTS;
					freeNode[freeNodeCount++]=nodeId-1;
					newline=true;
				}
				break;
			case RECHTS:
				if (0==(grid[nodeX][nodeY] & RECHTS) && 0==(grid[nodeX+1][nodeY])) {
					grid[nodeX][nodeY] |= RECHTS;
					grid[nodeX+1][nodeY] |= LINKS;
					freeNode[freeNodeCount++]=nodeId+1;
					newline=true;
				}
				break;
			}
			//test, ob der aktuelle node noch wachsen kann.
			if((0!=(grid[nodeX][nodeY] & RAUF)   || 0!=(grid[nodeX][nodeY-1])) &&
					(0!=(grid[nodeX][nodeY] & RUNTER) || 0!=(grid[nodeX][nodeY+1])) &&
					(0!=(grid[nodeX][nodeY] & LINKS)  || 0!=(grid[nodeX-1][nodeY])) &&
					(0!=(grid[nodeX][nodeY] & RECHTS) || 0!=(grid[nodeX+1][nodeY])) ) {
				//				retVal+=((freenode[freenodeid]%GridWidth) << "/" << (freenode[freenodeid]/GridWidth) << " ";
				grid[nodeX][nodeY] |= NOTFREE;
				freeNode[freenodeid]=freeNode[--freeNodeCount];
				newline=true;
			}
			if(newline && debug) {
				retVal+=toString();
				newline=false;
			}
		}
		retVal+=toString();
		return retVal;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Labyrinth l=new Labyrinth(7,5);
		File f=new File("test.html");
		try {
			FileOutputStream fos=new FileOutputStream(f);
			fos.write(("<html><body><h1>Labyrinth("+l.width+","+l.height+")</h1>").getBytes());
			fos.write(l.getGrid().getBytes());
			fos.write("</body></html>".getBytes());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
