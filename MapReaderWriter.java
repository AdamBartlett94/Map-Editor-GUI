// EDC: Assignment 3
// By: Adam Bartlett, ID:a1646071
// Date: 30/10/18

// Import appropriate Java libraries
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;

// This class handles reading and writing map representations as 
// described in the practical specification	
public class MapReaderWriter implements MapIo {

	// ---Done---
    // Read the description of a map from the 
    // Reader r, and transfers it to Map, m.
    public void read (Reader r, Map m) throws IOException, MapFormatException {
    	
    	// Variables:
    	String[] line;
    	int lineNr = 0;    	
		
    	do {
    		// Read a line:
    		line = readLine(r);
    		lineNr++;
    		
    		// Verify line type
    		if(line[0].compareTo("place") == 0) {      			
    			// Add place to map:
    			if(line.length >= 4) {
    				try {
    					m.newPlace(line[1], Integer.parseInt(line[2]), Integer.parseInt(line[3]));
    				}
    				catch(IllegalArgumentException e1) {
    					throw new IOException(e1.getMessage());
    				}    				
    			}
    			else {
    				throw new MapFormatException(lineNr, "Err: Invalid map input...");
    			}
    		}
    		else if(line[0].compareTo("road") == 0) {    			
    			// Add road to map:
    			if(line.length >= 5) {
    				// Get reference place locations:
    				Place from = m.findPlace(line[1]); 
    				Place to = m.findPlace(line[4]);   
    				
    				if((from == null) || (to == null)) {
    					throw new MapFormatException(lineNr, "Err: Can't' create road, to or from doesn't exist...");
    				}
    				else {    					
    					if(line[2].compareTo("-") == 0) {
    						try {
    							m.newRoad(from, to, "", Integer.parseInt(line[3]));
    						}
    						catch(IllegalArgumentException e1) {
    							throw new IOException(e1.getMessage());
    						}
    					}
    					else {
    						try {
    							m.newRoad(from, to, line[2], Integer.parseInt(line[3]));
    						}
    						catch(IllegalArgumentException e1) {
    							throw new IOException(e1.getMessage());
    						}    						
    					}
    				}
    			}
    			else {
    				throw new MapFormatException(lineNr, "Err: Invalid map input...");
    			}    			
    		}
    		else if(line[0].compareTo("start") == 0) {    			
    			// Set map starting position:
    			Place start = m.findPlace(line[1]);
    			
    			if(start != null) {
    				m.setStartPlace(start);
    			}
    			else {
    				throw new MapFormatException(lineNr, "Err: Can't' set starting location, place doesnt exist");
    			}
    		}
    		else if(line[0].compareTo("end") == 0) {    			
    			// Set map ending position:
    			Place end = m.findPlace(line[1]);
    			
    			if(end != null) {
    				m.setEndPlace(end);
    			}
    			else {
    				throw new MapFormatException(lineNr, "Err: Can't' set ending location, place doesnt exist");
    			}
    		}
    		else if(line[0].compareTo("") == 0) {
    			// Do nothing
    		}
    		else if(line[0].charAt(0) == '#') {
    			// Do nothing
    		}
    		else {
    			throw new MapFormatException(lineNr, "Err: Invalid map input...");
    		}    		
    		
    	} while(line[line.length - 1].compareTo("msg:eof") != 0);
	}    
    
    // ---Done---
    // Write a representation of the Map, m, to the Writer w.
    public void write(Writer w, Map m) throws IOException {    	
    	
    	// Set-up variables:
    	Place currentLoc = null;
    	Road currentRoad = null;
    	Iterator<Place> placeItr = m.getPlaces().iterator();
    	Iterator<Road> roadItr = m.getRoads().iterator();
    	
    	// Write places:
    	w.write("#Places:\n");
    	while(placeItr.hasNext()) {
    		currentLoc = placeItr.next();
    		w.write("place " + currentLoc.getName() + " " + Integer.toString(currentLoc.getX())
				+ " " + Integer.toString(currentLoc.getY()) + "\n");
    	}
    	w.write("\n");
    	
    	// Write roads:
    	w.write("#Roads:\n");
    	while(roadItr.hasNext()) {
    		currentRoad = roadItr.next();
    		w.write("road " + currentRoad.firstPlace().getName() + " " + currentRoad.roadName() + " "
    				+ Integer.toString(currentRoad.length()) + " " + currentRoad.secondPlace().getName() + "\n");
    	}
    	w.write("\n");
    	
    	// Write start location:
    	currentLoc = m.getStartPlace();
    	if(currentLoc != null) {
    		w.write("#Start:\n");
    		w.write("start " + currentLoc.getName() + "\n");
    		w.write("\n");
    	}
    	
    	// Write end location:
    	currentLoc = m.getEndPlace();
    	if(currentLoc != null) {
    		w.write("#End:\n");
    		w.write("end " + currentLoc.getName());
    	}    	
	}

    //---------------------------------------------------------------------------------------------
    // Private Methods:
    
    // ---Done---
    // Read a line from a reader object:
    private String[] readLine(Reader r) throws IOException{
    	
    	// Variables:
    	String line = "";
    	String[] lineArr = new String[0];
    	int data = r.read();
    	
    	// Read a single line
    	while((data != 10) && (data != -1)) {
    		line += (char) data;
    		data = r.read();
    	}
    	
    	if(data == -1) {    		
    		line = line.replaceAll("\\r|\\n", "");
    		line += " msg:eof";    		
    		lineArr = line.split(" ");
    		return lineArr;
    	}
    	else {
    		line = line.replaceAll("\\r|\\n", "");
    		lineArr = line.split(" ");    		
    		return lineArr;  
    	}
    }
}