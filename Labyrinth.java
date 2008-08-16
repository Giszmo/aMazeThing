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

	private int [][] grid;
	
	public Labyrinth(int width, int height) {
		this.width=width;
		this.height=height;
		grid=new int[width][height];
	}
	public void printgrid() {
		System.out.println("<table border=0 cellpadding=0 cellspacing=0>");
		for(int i=0; i<height; i++) {
			System.out.println("<tr>");
			for(int j=0;j<width;j++) {
				switch (grid[j][i]) {
					case 0: System.out.println("<td></td>"); break;
					case RAUF | RUNTER: System.out.println("<td>&#9474;</td>"); break;
					case RAUF: System.out.println("<td>&#9474;</td>"); break;
					case RUNTER: System.out.println("<td>&#9474;</td>"); break;
					case RECHTS: System.out.println("<td>&#9472;</td>"); break;
					case LINKS: System.out.println("<td>&#9472;</td>"); break;
					case RECHTS | LINKS: System.out.println("<td>&#9472;</td>"); break;
					case RAUF | RECHTS: System.out.println("<td>&#9492;</td>"); break;
					case RAUF | LINKS: System.out.println("<td>&#9496;</td>"); break;
					case LINKS | RUNTER: System.out.println("<td>&#9488;</td>"); break;
					case RECHTS | RUNTER: System.out.println("<td>&#9484;</td>"); break;
					case RUNTER | RECHTS | LINKS: System.out.println("<td>&#9516;</td>"); break;
					case RAUF | RECHTS | LINKS: System.out.println("<td>&#9524;</td>"); break;
					case RAUF | RUNTER | LINKS: System.out.println("<td>&#9508;</td>"); break;
					case RAUF | RUNTER | RECHTS: System.out.println("<td>&#9500;</td>"); break;
					case RAUF | RUNTER | RECHTS | LINKS: System.out.println("<td>&#9532;</td>"); break;
					case NOTFREE | 0: System.out.println("<td style=color:#FF0000>0</td>"); break;
					case NOTFREE | RAUF | RUNTER: System.out.println("<td style=color:#FF0000>&#9474;</td>"); break;
					case NOTFREE | RAUF: System.out.println("<td style=color:#FF0000>&#9474;</td>"); break;
					case NOTFREE | RUNTER: System.out.println("<td style=color:#FF0000>&#9474;</td>"); break;
					case NOTFREE | RECHTS: System.out.println("<td style=color:#FF0000>&#9472;</td>"); break;
					case NOTFREE | LINKS: System.out.println("<td style=color:#FF0000>&#9472;</td>"); break;
					case NOTFREE | RECHTS | LINKS: System.out.println("<td style=color:#FF0000>&#9472;</td>"); break;
					case NOTFREE | RAUF | RECHTS: System.out.println("<td style=color:#FF0000>&#9492;</td>"); break;
					case NOTFREE | RAUF | LINKS: System.out.println("<td style=color:#FF0000>&#9496;</td>"); break;
					case NOTFREE | LINKS | RUNTER: System.out.println("<td style=color:#FF0000>&#9488;</td>"); break;
					case NOTFREE | RECHTS | RUNTER: System.out.println("<td style=color:#FF0000>&#9484;</td>"); break;
					case NOTFREE | RUNTER | RECHTS | LINKS: System.out.println("<td style=color:#FF0000>&#9516;</td>"); break;
					case NOTFREE | RAUF | RECHTS | LINKS: System.out.println("<td style=color:#FF0000>&#9524;</td>"); break;
					case NOTFREE | RAUF | RUNTER | LINKS: System.out.println("<td style=color:#FF0000>&#9508;</td>"); break;
					case NOTFREE | RAUF | RUNTER | RECHTS: System.out.println("<td style=color:#FF0000>&#9500;</td>"); break;
					case NOTFREE | RAUF | RUNTER | RECHTS | LINKS: System.out.println("<td style=color:#FF0000>&#9532;</td>"); break;
					default: System.out.println(grid[i*width+j]);
				}
			}
			System.out.println("</tr>");
		}
		System.out.println("</table>");
	}

	void getGrid() {
		int freeNodeCount=0;
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
		grid[0][0]=
		grid[width-1][0]=
		grid[0][height-1]=
		grid[width-1][height-1]=LINKS | RECHTS | RUNTER | RAUF;
//		freeNode[freeNodeCount++]=0;
//		freeNode[freeNodeCount++]=width-1;
//		freeNode[freeNodeCount++]=width*(height-1);
//		freeNode[freeNodeCount++]=width*(height-1)+width-1;

		while (freeNodeCount>0) {
			int freenodeid=(int) (Math.random()*freeNodeCount);
//			int freenodeid=freenodecount-1;
			int nodeId=freeNode[freenodeid];
			int nodeX=freeNode[freenodeid]%width;
			int nodeY=freeNode[freenodeid]/width;
			int direction = (1 << (int)(Math.random()*4));
	/*		if(!(tmpgrid[nodeid] & direction)) {
				if(tmpgrid[nodeid+diffnachbar[direction]]==0) {
					tmpgrid[nodeid] |= RAUF;
					tmpgrid[nodeid-GridWidth] |= RUNTER;
					freenode[freenodecount++]=nodeid-GridWidth;*/
			//System.out.println("freenodeid: " << freenodeid << " tmpgrid[" << nodeid << "]: " << tmpgrid[nodeid] << " " << direction << "<br>";
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
//				System.out.println((freenode[freenodeid]%GridWidth) << "/" << (freenode[freenodeid]/GridWidth) << " ";
				grid[nodeX][nodeY] |= NOTFREE;
				freeNode[freenodeid]=freeNode[--freeNodeCount];
				newline=true;
			}
//			newline=false;
			if(newline) {
				printgrid();
				for(int i=0; i<freeNodeCount; i++) {
					System.out.println((freeNode[i]%width) + "/" + (freeNode[i]/width) + "_");
				}
//				System.out.println();
				newline=false;
			}
		}
		printgrid();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Labyrinth l=new Labyrinth(6,6);
		System.out.println("<html><body><h1>Labyrinth("+l.width+","+l.height+")</h1>");
		l.getGrid();
		System.out.println("</body></html>");
	}

}
