import java.io.*;
import java.lang.*;

/* 
	This program builds a class, SeatingChart, that handles the Showclix seating chart puzzle. 
	Even though build() and reserve() can (and will) be written strictly as methods of 
	SeatingChart, they will be included as public, static methods that can be called outside 
	of a particular object.
*/

class SeatingChart {

	public static void main(String[] args) {
		// So, the whole shebang gets kicked off with a call to build:
		String reservedStr[] = new String[] {"R1C4","R1C6","R2C3","R2C7","R3C9","R3C10"};
		SeatingChart sistersShow = build(3, 11, reservedStr);
		SeatingChart tempChart;
		
		sistersShow.displayMap();
		
		// Simple testing		
		boolean run = true;	// Flag, stops execution at false
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		String userInput;	// Placeholder for user input from the command line
		int seatsNeeded;	// Number of seats requested to be reserved
		while (run) {
			
			System.out.println("Would you like to reserve some seats? ");
			try {
				userInput = console.readLine();
			}
			catch (IOException err) {
				// You don't have a keyboard? Really? Just screw it, I'm going home then
				break;
			}
			if (userInput.equalsIgnoreCase("NO")) {
				run = false;
				continue;
			}
			else if (userInput.equalsIgnoreCase("YES")) {
				// Blank, this is good input
			}
			else {
				// Bad input
				System.out.println("I don't understand, yes or no answers only.");
				continue;
			}
			System.out.println("How many seats do you need to reserve? ");
			try {
				userInput = console.readLine();
			}
			catch (IOException err) {
				// You don't have a keyboard? Really? Just screw it, I'm going home then
				break;
			}
			try {
			tempChart = reserve(sistersShow, Integer.parseInt(userInput));
			}
			catch (NumberFormatException err){
				System.out.println("Oh dear, that wasn't a valid number");
				continue;
			}
			
			if (!tempChart.equals(sistersShow)) {
				tempChart.displayMap();
				System.out.println("Save these seats? ");
				
				try {
					userInput = console.readLine();
				}
				catch (IOException err) {
					// You don't have a keyboard? Really? Just screw it, I'm going home then
					break;
				}
				if (userInput.equalsIgnoreCase("YES")) {
					sistersShow = new SeatingChart(tempChart);
				}
			}
		}
	}
	
	// These private static methods are the functions asked to be implemented
	private static SeatingChart build(int rows, int cols, String reserved[]) {
		return new SeatingChart(rows, cols, reserved);
	}
	
	private static SeatingChart reserve(SeatingChart map, int n) {
		// This method is the biggun, it's going to be selecting the best block of seats available
		
		SeatingChart tempy = new SeatingChart(map);
		
		// Size check
		if (map.isTooSmall(n) || n > 10) {
			System.out.println("Not available.");
			return tempy;
		}
		
		int iRow = -1, iCol = -1;	// Start of the found block
		boolean chart[][] = tempy.getChart();
		int rows = chart.length;	// # of rows to check
		int cols = chart[0].length;	// # of columns to check
		int startC = 0;				// How deep into the row to start looking
		int mid = tempy.getBestSeat();	// Best seat
		int iCompZone[] = new int[2];	// Indexes for the complex (overlap) zone of a row
		iCompZone[0] = mid - n + 2;		// Start
		iCompZone[1] = mid - 1;			// End
		mid = mid - n/2;				// Start of the best block
		
		for (int r = 0; r < rows; r++) {
			int start = -1;
			int count = 0;
			int tempR = -1, tempC = -1;
			boolean foundBest = false;	// Flag, once we've found the best block in a row
			for (int c = startC; c < cols && !foundBest; c++) {
			
				if (count == n && start > mid && tempR < 0) {
					// Stop looking cause there won't be a better one
					tempR = r;
					tempC = start;
					break;
				}
				
				if (!chart[r][c]) {
					count++;
					if (start < 0) {
						start = c;
					}
				}
				
				else if (count < n) {
					count = 0;
					start = -1;
				}
				
				if ((chart[r][c] || c == cols - 1) && count >= n) {
					// Find closest block to the middle for the nontrivia cases
					if (count != n) {
						// NOTE: On exit, start should be the start of the best block
						int lastPoss = start + count - n;	// Last index that a block could start at
						
						// First the trivias
						if (lastPoss < mid) {
							// All starting points to the left of the best start
							start = lastPoss;
						}
						else if (start == mid) {
							// Start is set right and we have the best
							foundBest = true;
						}
						else if (lastPoss == mid) {
							start = lastPoss;
							// We have the best
							foundBest = true;
						}
						else if (start < mid && lastPoss > mid) {
							// Mid is in range
							start = mid;
							// We have the best
							foundBest = true;
						}
						else {
							// Start is the start, also, best block of the row
							foundBest = true;
						}
					}
					
					if (tempR < 0) {
						// Haven't found one yet
						tempR = r;
						tempC = start;
						start = -1;
						count = 0;
					}
					else {
						// We already have one
						if (Math.abs(tempC - mid) > Math.abs(start - mid)) {
							// The one we have is better, this also means we have the best one
							break;
						}
						
						else if (Math.abs(tempC - mid) == Math.abs(start - mid)){
							// Duplicates? Randomly choose one
							tempC = Math.random() < 0.5d ? tempC : start;
							// Again, we're done here with this loop
							break;
						}
						
						else {
							// New block is better
							tempC = start;
							start = -1;
							count = 0;
						}
					}
				}
			}
			
			if (tempR >= 0) {
				boolean updateChecks = false;	// Flag, flipped if we found a new/better block
				if (iRow < 0) {
					// First block found
					iRow = tempR;
					iCol = tempC;
					updateChecks = true;
				}
				
				else {
					// Nontrivia
					if (((iCol < iCompZone[0] || iCol > iCompZone[1]) && 
							(tempC < iCompZone[0] || tempC > iCompZone[1])) ||
							((iCol > iCompZone[0] && iCol < iCompZone[1]) && 
							(tempC > iCompZone[0] && tempC < iCompZone[1]))) {
						// So long as both are in or out of the zone, the math is easy
						int diffR = tempR - iRow;	// Penalize the new block for being further back
						if (Math.abs(iCol - mid) > (Math.abs(tempC - mid) + diffR)) {
							// Old one is worse
							iRow = tempR;
							iCol = tempC;
							updateChecks = true;
						}
					}
					else {
						// If only one is in the complicated zone, it's complicated
						int realMid = tempy.getBestSeat();
						double curAvg = 0.0, newAvg = 0.0;
						
						for (int i = iCol; i < iCol + n; i++) {
							curAvg += Math.abs(i - realMid) + iRow;
						}
						curAvg = curAvg/n;
						
						for (int i = tempC; i < tempC + n; i++) {
							newAvg += Math.abs(i - realMid) + tempR;
						}
						newAvg = newAvg/n;
						
						if (curAvg < newAvg) {
							iRow = tempR;
							iCol = tempC;
							updateChecks = true;
						}
					}
				}
				
				if (updateChecks) {
					int blockMid = iCol + (n - 1)/2;
					rows = Math.abs(tempy.getBestSeat() - blockMid) + iRow; // A close enough approximation
					if (rows > chart.length) {
						// Make sure we aren't looking at rows that aren't there
						rows = chart.length;
					}
					// The seats must be closer to the middle than this one
					startC = iCol;
					cols = chart[0].length - (iCol + 1);
				}
				
				if (iRow >= 0) {
					// The further back we go, the closer the seats have to be
					startC++;
					cols--;
					if (startC > chart[0].length || cols < startC) {
						// Shouldn't be here, but break if this happens
						break;
					}
				}
			}
			
		}
		
		// Did we find one?
		if (iRow < 0) {
			System.out.println("Not available.");
		}
		else {
			for (int c = iCol; c < iCol + n; c++) {
				tempy.book(iRow, c);
			}
			System.out.printf("Reserved seats at R%dC%d through R%dC%d.\n", 
					iRow+1, iCol+1, iRow+1, (iCol+n));
		}
		
		return tempy;
	}
	
	/*****************Begin SeatingChart class definition*************/
	// Fields
	private int rows;					// Number of rows the venue has
	private int columns;				// Number of columns (or seats per row) a venue has
	private boolean reservedSeats[][];	// The status of the seats, false if available
	private int middle;					// The index for the center seat of a row
	// Note: For middle, we want there to be more seats greater than middle than less than
	
	// Contructors
	
	public SeatingChart(int r, int c) {
		// This constructor builds the class fully and sets all the seats to avaiable.
		// This is provided that the user didn't mess things up and give us bad values...
		
		rows = r;
		columns = c;
		if (rows < 1 || columns < 1) {
			// A zero or negative number is not valid input, go to error state
			middle = -1;
			reservedSeats = null;
		}
		else {
			middle = (c-1)/2;	// This gets the right index for both evens and odds
			reservedSeats = new boolean[r][c];	// In Java, this inializes to false
		}
	}
	
	public SeatingChart(int r, int c, String res[]) {
		// This constructor not only builds the class, but reserves the seats specified in res[]
		// I'm really lazy
		this(r, c);		// Will throw an error once that is added
		
		// Now for the reserving part!
		for (int i = 0; i < res.length; i++) {
			// The string format is "R#C#", with # being an unknown number of digits
			String code = res[i].toUpperCase();		// Don't want to have to worry about casing
			int tRow = code.indexOf('R');			// Which row to be reserved
			int tCol = code.indexOf('C');			// Which column to be reserved
			
			// Also, we start those two temps off with the index of R and C so that...
			String tester = new String();
			try {
			// Remember to add 1 to the first index get skip the letter code
			tester = code.substring(tRow+1, tCol);
			tRow = Integer.parseInt(tester) - 1;
			tester = code.substring(tCol+1);
			tCol = Integer.parseInt(tester) - 1;
			// Oh and dipshit, most people aren't programmers! Row 1 = index 0 >:(
			}
			catch (NumberFormatException err) {
				// This is possible, just skip this string
				// Use this for simple error checking to standard output
				//System.out.printf("The String: %s at index: %d is not valid.\n", tester, i);
				continue;
			}
			//System.out.printf("row = %d; column = %d\n", tRow, tCol);
			reservedSeats[tRow][tCol] = true;
		}
	}
	
	// Copy constructor
	public SeatingChart(SeatingChart sc) {
		this(sc.rows, sc.columns);
		
		// Now to clone the array
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				reservedSeats[r][c] = sc.reservedSeats[r][c];
			}
		}
	}
	
	public String toString() {
		/* This will produce a formated String representing the SeatingChart class. Each seat is
			separated by a comma and each row ends with a semicolon, starting with the first seat 
			of the last row. If a seat is available to be booked, it is marked with an "A", if not, 
			a "R". Such that a 2 row, 5 col venue with the 3 front center seats and back row all 
			booked will look like such:
			R,R,R,R,R;A,R,R,R,A;
		*/
		String free = "A";
		String reserved = "R";
		String seating = new String();
		
		for (int r = reservedSeats.length-1; r >= 0; r--) {
			// Starts with the last row going forward
			
			String temp = "";
			for (int c = 0; c < reservedSeats[r].length; c++) {
				// Starts with the left most seat
				if (c != 0) {
					temp = temp + ",";
				}
				// If it's reserved...
				if (reservedSeats[r][c]) {
					temp = temp + reserved;
				}
				// ...Otherwise
				else {
					temp = temp + free;
				}
			}
			
			// Don't add a semicolon at the end!
			seating = temp + ";";			
		}
		
		return seating;
	}
	
	public boolean book(int r, int c) {
		// Reserves a seat at r, c, returns false if seat is already booked
		if (!reservedSeats[r][c]){
			reservedSeats[r][c] = true;
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getBestSeat() {
		// Simply returns the index for the best seat in the house!
		return middle;
	}
	
	public void displayMap() {
		// Quick and dirty display function, should only be used on a 3 x 11 chart
		
		for (int r = reservedSeats.length-1; r >= 0; r--) {
			System.out.printf("%3d--", r+1);
			for (int c = 0; c < reservedSeats[r].length; c++) {
				if (c != 0) {
					System.out.printf(",");
				}
				
				// If it's booked...
				if (reservedSeats[r][c]) {
					System.out.printf(" R");
				}
				else{
					System.out.printf("__");
				}
			}
			System.out.printf("\n");
		}
		System.out.printf("    |");
		for (int i = 1; i <= reservedSeats[0].length; i++) {
			System.out.printf("%2d ", i);
		}
		System.out.printf("\n");
	}
	
	public boolean isTooSmall(int num) {
		return num > columns;
	}
	
	
	public boolean[][] getChart(){
		return reservedSeats;
	}
	
	public boolean equals(SeatingChart sc) {
		// Returns true if this SeatingChart is equal to another
		if (rows != sc.rows || columns != sc.columns) {
			return false;
		}
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				if (reservedSeats[r][c] ^ sc.reservedSeats[r][c]) {
					return false;
				}
			}
		}
		
		return true; 
	}
	/******************* End SeatingChart class definition********************/
}