import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author leo
 *
 */
public class Labyrinth {
	private int width;
	private int height;
	private int holeTop;
	private int holeBottom;
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
		this.holeTop=(int) (Math.random()*(width-1));
		this.holeBottom=(int) (Math.random()*(width-1));
		grid=new int[width][height];
	}
	public String picString(int i) {
		switch (i) {
		case 0: return "0.png";

		case RAUF: return "N.png";
		case RUNTER: return "S.png";
		case RECHTS: return "E.png";
		case LINKS: return "W.png";

		case RAUF | RUNTER: return "SN.png";
		case RECHTS | LINKS: return "EW.png";
		case RAUF | RECHTS: return "EN.png";
		case RAUF | LINKS: return "WN.png";
		case LINKS | RUNTER: return "WS.png";
		case RECHTS | RUNTER: return "ES.png";

		case RUNTER | RECHTS | LINKS: return "EWS.png";
		case RAUF | RECHTS | LINKS: return "EWN.png";
		case RAUF | RUNTER | LINKS: return "WSN.png";
		case RAUF | RUNTER | RECHTS: return "ESN.png";

		case RAUF | RUNTER | RECHTS | LINKS: return "EWSN.png";
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
					String style="background-color:";
					if(0!=(grid[j][i] & NOTFREE))
						style+="#ff0";
					else
						style+="#0ff";
					retVal+=" style=\""+style+"\"";
				}
				retVal+="><img src=\"data/";
				//es interessiert nur die richtung:
				int toDisplay=grid[j][i]&(RAUF | RUNTER | RECHTS | LINKS);
				if(j==0)
					toDisplay^=LINKS;
				if(j==width-1)
					toDisplay^=RECHTS;
				if(i==0) {
					toDisplay^=RAUF;
					if(j==holeTop)
						toDisplay^=RECHTS;
					if(j==holeTop+1)
						toDisplay^=LINKS;
				}
				if(i==height-1) {
					toDisplay^=RUNTER;
					if(j==holeBottom)
						toDisplay^=RECHTS;
					if(j==holeBottom+1)
						toDisplay^=LINKS;
				}
				retVal+=picString(toDisplay)+"\" /></td>";
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
		boolean newline = true;
		//init
		//leeres grid innen
		for (int i=0; i<width; i++ ) {
			for (int j=0; j<height;j++) {
				grid[i][j]=0;
			}
		}
		//oben und unten wand. nach innen offen
		for (int i=1;i<width-1 ;i++ ) {
			grid[i][0]       = LINKS | RECHTS | RAUF;
			grid[i][height-1]= LINKS | RECHTS | RUNTER;
			freeNode[freeNodeCount++]= i;
			freeNode[freeNodeCount++]=(height-1)*width+i;
		}
		//rechts und links wand. nach innen offen
		for (int j=1;j<height-1 ;j++ ) {
			grid[0][j]       = LINKS | RUNTER | RAUF;
			grid[width-1][j] = RAUF | RECHTS | RUNTER;
			freeNode[freeNodeCount++]=width*j;
			freeNode[freeNodeCount++]=width*j+width-1;
		}
		//in den ecken wand
		grid[0      ][0       ]=
		grid[width-1][0       ]=
		grid[0      ][height-1]=
		grid[width-1][height-1]=RECHTS | RUNTER | LINKS  | RAUF;

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
		Labyrinth l=new Labyrinth(15,9);
		File f=new File("test.html");
		try {
			FileOutputStream fos=new FileOutputStream(f);
			fos.write(("<html><body><h1>Labyrinth("+l.width+","+l.height+")</h1>").getBytes());
			fos.write(l.getGrid().getBytes());
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
