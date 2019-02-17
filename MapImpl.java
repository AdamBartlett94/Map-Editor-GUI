// EDC: Assignment 3
// By: Adam Bartlett, ID:a1646071
// Date: 30/10/18

// Import appropriate Java libraries:
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

// Class Description: Implementation of map class
// Refer to assignment 2 specification for more details
public class MapImpl implements Map {
	
	// Variables:
	private Set<MapListener> mapListeners;
	private Set<Place> places;
	private Set<Road> roads;
	private Place start;
	private Place end;
	
	//---------------------------------------------------------------------------------------------
	// ---Done---
	// Constructor: Creates an empty map
	MapImpl() {
		// Initialize a new map
		this.mapListeners = new HashSet<>();
		this.places = new HashSet<>();
		this.roads = new HashSet<>();		
		this.start = null;
		this.end = null;	
	}
	
	
	//---------------------------------------------------------------------------------------------
	// Auto Inherited Methods - Map
	
	// ---Done---
	// Add the MapListener ml to this map.
	// Note: A map can have multiple listeners
	public void addListener(MapListener ml) {
		mapListeners.add(ml);
	}
	

	// ---Done---
	// Delete the MapListener ml from this map.
	public void deleteListener(MapListener ml) {
		mapListeners.remove(ml);
	}
	

	// ---Done---
	// Create a new Place and add it to this map
	// Return the new place
	// Throws IllegalArgumentException if:
	// 		- the name is not valid or is the same as that
	//  	- of an existing place
	// Note: A valid placeName begins with a letter, and is 
	// followed by optional letters, digits, or underscore characters	
	public Place newPlace(String placeName, int xPos, int yPos) throws IllegalArgumentException {
		
		// Check if placeName is valid:
		boolean isValidName = checkPlaceName(placeName);
		
		if(isValidName) {
			// Create the new place:
			Place newPlace = new Placelpl(placeName, xPos, yPos);
			
			// Try to add newPlace to places: True = unique (therefore not in list)
			// False = not unique (therefore already in list)
			boolean isUnique = checkPlaceUniqueness(newPlace);
			
			if(isUnique) {
				places.add(newPlace);
				mlChangeOccured("place");
				return newPlace;				
			}
			else {
				throw new IllegalArgumentException("Err: Place already exists in map...");
			}			
		}
		else {
			throw new IllegalArgumentException("Err: Place name is invalid...");
		}
	}
	

	// ---Done---
	// Remove a place from the map
	// If the place does not exist, returns without error
	public void deletePlace(Place s) {
		
		// Search for place to delete:
		Place toDelete = findPlace(s.getName());
		
		// Delete place if it exists & any associated roads:
		if(toDelete != null) {			
			
			// Delete roads associated with toDelete:
			Road currentRoad = null;
			Set<Road> associatedRoads = toDelete.toRoads();
			Iterator<Road> itr = associatedRoads.iterator();
			
			while(itr.hasNext()) {
				currentRoad = itr.next();
				deleteRoad(currentRoad);
			}
			
			// Check if the place was the starting location:
			if(s.isStartPlace()) {
				this.setStartPlace(null);
			}
			
			// Check if place was the ending location:
			if(s.isEndPlace()) {
				this.setEndPlace(null);
			}
			
			// Delete the place:
			places.remove(toDelete);
			mlChangeOccured("place");
		}
	}
	

	// ---Done---
	// Find and return the Place with the given name
	// If no place exists with given name, return NULL
	public Place findPlace(String placeName) {

		// Variables:
		Place currentLoc = null;
		Iterator<Place> itr = places.iterator();
		
		// Search through places to see if desired place exists:		
		while(itr.hasNext()) {			
			currentLoc = itr.next();			
			if(currentLoc.getName().compareTo(placeName) == 0) {
				return currentLoc;
			}	
		}
		
		// Place not found
		return null;
	}
	
	// ---Done---
	// Return a set containing all the places in this map
	public Set<Place> getPlaces() {
		return places;
	}
	

	// ---Done---
	// Create a new Road and add it to this map
	// Returns the new road.
	// Throws IllegalArgumentException if:
    	//	- the firstPlace or secondPlace does not exist or
    	//  	- the roadName is invalid or
    	//  	- the length is negative
    	// Note: A valid roadName is either the empty string, or starts
    	// with a letter and is followed by optional letters and digits
	public Road newRoad(Place from, Place to, String roadName, int length) throws IllegalArgumentException {

		// Check if the firstPlace/secondPlace exists and length is no negative:
		if(this.findPlace(from.getName()) == null) {
			throw new IllegalArgumentException("Err: Place-from does not exist...");
		}
		else if(this.findPlace(to.getName()) == null) {
			throw new IllegalArgumentException("Err: Place-to does not exist...");
		}
		else if(length < 0) {
			throw new IllegalArgumentException("Err: Length is negative");
		}
		
		// Check if road name is valid:
		boolean isValidName = checkRoadName(roadName);
		
		if(isValidName) {
			
			// Create the new road:
			Road newRoad = new Roadlrl(from, to, roadName, length);
						
			// Check if the road is unique: i.e: doesn't already exist on map(same name + to & from)
			boolean isUnique = checkRoadUniqueness(newRoad);
			
			if(isUnique) {
				// Add road to map:
				roads.add(newRoad);
				mlChangeOccured("road");
				return newRoad;
			}
			else {
				throw new IllegalArgumentException("Err: Road already exists in map...");
			}	
		}
		else {
			throw new IllegalArgumentException("Err: roadName is invalid...");
		}
	}
	

	// ---Done---
    	// Remove a road r from the map
    	// If the road does not exist, returns without error
	public void deleteRoad(Road r) {
		
		// Convert roads into array form:
		Road currentRoad = null;
		Iterator<Road> itr = roads.iterator();
		
		// Search through roadsArr to see if desired road exists:
		while(itr.hasNext()) {
			currentRoad = itr.next();			
			
			// Check for common road name:
			if(currentRoad.roadName().compareTo(r.roadName()) == 0) {
				
				// Check for common road places:
				if((currentRoad.firstPlace().getName().compareTo(r.firstPlace().getName()) == 0) 
						&& (currentRoad.secondPlace().getName().compareTo(r.secondPlace().getName()) == 0)) {
					roads.remove(currentRoad);
					mlChangeOccured("road");
					return;
				}				
			}			
		}
	}
	

	// ---Done---
	// Return a set containing all the roads in this map
	public Set<Road> getRoads() {
		return roads;
	}
	
	
	// ---Done---
    	// Set the place p as the starting place
    	// If p==null, unsets the starting place
    	// Throws IllegalArgumentException if the place p is not in the map
	public void setStartPlace(Place p) throws IllegalArgumentException {
		
		if(p == null) {
			start = null;
			mlChangeOccured("other");
		}
		else {
			// Make sure p is on the map:
			Place newStart = this.findPlace(p.getName());
			
			if(newStart != null) {
				start = newStart;
				mlChangeOccured("other");
			}
			else {
				throw new IllegalArgumentException("Err: Place does not exist on map");
			}
		}	
	}
	

	// ---Done---
    	// Return the starting place of this map
	public Place getStartPlace() {
		return start;
	}
	

	// ---Done---
    	// Set the place p as the ending place
    	// If p==null, unsets the ending place
    	// Throws IllegalArgumentException if the place p is not in the map
	public void setEndPlace(Place p) throws IllegalArgumentException {
		
		if(p == null) {
			end = null;
			mlChangeOccured("other");
		}
		else {
			// Make sure p is on the map:
			Place newEnd = this.findPlace(p.getName());
			
			if(newEnd != null) {
				end = newEnd;
				mlChangeOccured("other");
			}
			else {
				throw new IllegalArgumentException("Err: Place does not exist on map");
			}
		}				
	}
	

	// ---Done---
    	// Return the ending place of this map
	public Place getEndPlace() {
		return end;
	}
	

	// ---TBA---
    	// Causes the map to compute the shortest trip between the
    	// "start" and "end" places
    	// For each road on the shortest route, sets the "isChosen" property
    	// to "true".
    	// Returns the total distance of the trip.
    	// Returns -1, if there is no route from start to end
	public int getTripDistance() {

		// TBA: Not required for this assignment
		return -1;
	}	
	
	
	// ---Done---
	// Return a string describing this map
	// Returns a string that contains (in this order):
	// for each place in the map, a line (terminated by \n)
	//  	PLACE followed the toString result for that place
	// for each road in the map, a line (terminated by \n)
	// 	ROAD followed the toString result for that road
	// if a starting place has been defined, a line containing
	//  	START followed the name of the starting-place (terminated by \n)
	// if an ending place has been defined, a line containing
	//  	END followed the name of the ending-place (terminated by \n)
	public String toString() {

		// Variables:
		String mapDetails = "";
		Place[] placesArr = places.toArray(new Place[0]);
		Road[] roadsArr = roads.toArray(new Road[0]);

		// Add place information:
		for(int i = 0; i < placesArr.length; i++) {
			mapDetails += "PLACE " + placesArr[i].toString() + "\n";
		}

		// Add road information:
		for(int i = 0; i < roadsArr.length; i++) {
			mapDetails += "ROAD " + roadsArr[i].toString() + "\n";
		}

		// Add start and end location information:
		if(start != null) {
			mapDetails += ("START " + start.getName() + "\n");
		}

		if(end != null) {
			mapDetails += ("END " + end.getName() + "\n");
		}

		return mapDetails;
	}
    
	//---------------------------------------------------------------------------------------------
	// Private methods

	// ---Done---
	// Checks if a place name is valid
	// Note: A valid placeName begins with a letter, and is 
	// followed by optional letters, digits, or underscore characters
	private boolean checkPlaceName(String placeName) {

		// Check first character:   	    	
		if(!Character.isLetter(placeName.charAt(0))) {
			return false;
		}

		// Check remaining characters:
		for(int i = 1; i < placeName.length(); i++) {    		
			// Check if current character is valid:    		
			if(!Character.isLetterOrDigit(placeName.charAt(i))) {    			
				if(placeName.charAt(i) != '_') {
					return false;
				}
			}
		}

		// The name is valid:
		return true;
	}
    
    
    	// ---Done---
    	// Checks if the place p already exists in the map or not
    	// True = it doesn't, false = it does
	private boolean checkPlaceUniqueness(Place p) {

		// Convert places hash set into an array:
		Place currentLoc = null;
		Iterator<Place> itr = places.iterator();

		// Iterate through the array and make sure p's name is unique:
		while(itr.hasNext()) {
			currentLoc = itr.next();
			if(currentLoc.getName().compareTo(p.getName()) == 0) {
				return false;
			}
		}

		// No duplicates found, therefore name is unique
		return true;
	}
    
    
	// ---Done---
	// Checks if a road name is valid
	// Note: A valid roadName is either the empty string, or starts
	// with a letter and is followed by optional letters and digits
	private boolean checkRoadName(String roadName) {

		// Check if empty sting:
		if(roadName.compareTo("") == 0) {
			return true;
		}

		// Check first character:   	    	
		if(!Character.isLetter(roadName.charAt(0))) {
			return false;
		}

		// Check remaining characters:
		for(int i = 1; i < roadName.length(); i++) {    		
			// Check if current character is valid:    		
			if(!Character.isLetterOrDigit(roadName.charAt(i))) {    			
				return false;
			}
		}

		// The name is valid:
		return true;
	}
    
    
	// ---Done---
	// Checks if a road with same name and places already exists on the map
	private boolean checkRoadUniqueness(Road r) {

		// Convert roads hash set into an array:
		Road currentRoad = null;
		Iterator<Road> itr = roads.iterator();

		// Search for roads with similar name:
		while(itr.hasNext()) {
			currentRoad = itr.next();
			if(currentRoad.roadName().compareTo(r.roadName()) == 0) {

				// Road name already exists, therefore check to & from:
				if((currentRoad.firstPlace().getName().compareTo(r.firstPlace().getName()) == 0)
						&& (currentRoad.secondPlace().getName().compareTo(r.secondPlace().getName()) == 0)) {
					return false;
				}
			}
		}

		// No duplicates found, therefore name is unique
		return true;
	}
    
	// ---Done---
	// Alert all map listeners that places have changed
	private void mlChangeOccured(String type) throws IllegalArgumentException{

		// Variables
		MapListener currentML = null;
		Iterator<MapListener> itr = mapListeners.iterator();

		// Call placesChanged for all map listeners:
		while(itr.hasNext()) {
			currentML = itr.next();

			if(type.compareTo("place") == 0) {
				currentML.placesChanged();
			}
			else if(type.compareTo("road") == 0) {
				currentML.roadsChanged();
			}
			else if(type.compareTo("other") == 0) {
				currentML.otherChanged();
			}
			else {
				throw new IllegalArgumentException("Err: type is invalid");
			}
		}    	
	}
    
	//---------------------------------------------------------------------------------------------
	// Placelpl Class
	//---------------------------------------------------------------------------------------------
	public class Placelpl implements Place {

		// Variables:   	
		private int xPos;
		private int yPos; 
		private String placeName;
		private Set<PlaceListener> placeListeners;

		//---------------------------------------------------------------------------------------------
		// ---Done---
		// Constructor
		Placelpl(String placeName, int xPos, int yPos) {

			// Initialize provided information:
			this.placeName = placeName;
			this.xPos = xPos;
			this.yPos = yPos;

			// Initialize other variables:
			this.placeListeners = new HashSet<>();
		}


		//---------------------------------------------------------------------------------------------
		// Auto Inherited Methods - Place

		// ---Done---
		// Add the PlaceListener pl to this place. 
		// Note: A place can have multiple listeners
		public void addListener(PlaceListener pl) {
			placeListeners.add(pl);
			plChangeOccured();
		}


		// ---Done---
		// Delete the PlaceListener pl from this place.
		public void deleteListener(PlaceListener pl) {
			placeListeners.remove(pl);
			plChangeOccured();
		}


		// ---Done---
		// Return a set containing all roads that reach this place
		public Set<Road> toRoads() {

			// Variables:
			Road currentRoad = null;
			Iterator<Road> itr = roads.iterator();
			Set<Road> avaliableRoads = new HashSet<>();

			// Iterate over roads in map:
			while(itr.hasNext()) {
				currentRoad = itr.next();

				// Check if current road is connected to this place:
				if((currentRoad.firstPlace().getName().compareTo(this.placeName) == 0) 	
						|| (currentRoad.secondPlace().getName().compareTo(this.placeName) == 0)) {
					avaliableRoads.add(currentRoad);
				}    			
			}

			return avaliableRoads;
		}


		// ---Done---
		// Return the road from this place to dest, if it exists
		// Returns null, if it does not
		public Road roadTo(Place dest) {

			// Convert roads to array form:
			Road currentRoad = null;
			Set<Road> roads = this.toRoads();
			Iterator<Road> itr = roads.iterator();

			// Iterate through all of the roads looking for the desired destination:
			while(itr.hasNext()) {
				currentRoad = itr.next();

				// Retrieve places associated with the current road:
				Place firstPlace = currentRoad.firstPlace();
				Place secondPlace = currentRoad.secondPlace();			

				// Check firstPlace:
				if(!(firstPlace.getName().compareTo(this.placeName) == 0)) {				
					if(firstPlace.getName().compareTo(dest.getName()) == 0) {
						return currentRoad;
					}
				}			

				// Check secondPlace
				if(!(secondPlace.getName().compareTo(this.placeName) == 0)) {
					if(secondPlace.getName().compareTo(dest.getName()) == 0) {
						return currentRoad;
					}
				}
			}

			// Desired destination was not found
			return null;
		}


		// ---Done---
		// Move the position of this place 
		// by (dx,dy) from its current position
		public void moveBy(int dx, int dy) {	    		

			// Update xPosition:
			xPos += dx;
			if(xPos < 0) {
				xPos = 0;
			}        	

			// Update yPosition:
			yPos += dy;    	
			if(yPos < 0) {
				yPos = 0;
			}

			// Notify the place listener that the place has changed location:
			plChangeOccured();

			// Notify the roads connected to this place has changed location: 
			// Works because toRoads() calls firstPlace() and secondPlace() which in turn
			// notify the road listeners by calling rlChangeOccured(). Only way i could think to get
			// it to work with the given interface files.
			toRoads();
		}


		// ---Done---
		// Return the name of this place 
		public String getName() {
			return placeName;
		}


		// ---Done---
		// Return the X position of this place
		public int getX() {
			return xPos;
		}


		// ---Done---
		// Return the Y position of this place
		public int getY() {
			return yPos;
		}


		// ---Done---
		// Return true if this place is the starting place for a trip
		public boolean isStartPlace() {    		
			// Check if this is the starting place:
			if(start != null) {
				if(start.getName().compareTo(this.placeName) == 0) {
					return true;
				} 
			}   		   		
			return false;
		}


		// ---Done---
		// Return true if this place is the ending place for a trip
		public boolean isEndPlace() {
			// Check if this is the starting place:
			if(end != null) {
				if(end.getName().compareTo(this.placeName) == 0) {
					return true;
				} 
			}    		   		
			return false;
		}


		// ---Done---
		// Return a string containing information about this place 
		// in the form (without the quotes, of course!) :
		// "placeName(xPos,yPos)"  
		public String toString() {        	        	
			return placeName + "(" + Integer.toString(xPos) + "," 
				+ Integer.toString(yPos) + ")";
		}


		// ---Done---
		// Alert all place listeners that places have changed
		private void plChangeOccured() {

			// Variables
			PlaceListener currentPL = null;
			Iterator<PlaceListener> itr = placeListeners.iterator();

			// Call placesChanged for all map listeners:
			while(itr.hasNext()) {
				currentPL = itr.next();
				currentPL.placeChanged();       		
			}    	
		}
	}
    
	//---------------------------------------------------------------------------------------------
	// Roadlrl Class
	//---------------------------------------------------------------------------------------------
	public class Roadlrl implements Road {

		// Variables
		Place to;
		Place from;        
		private int length;
		private String roadName;
		public boolean isChosen;
		private Set<RoadListener> roadListeners;

		//---------------------------------------------------------------------------------------------
		// ---Done---
		// Constructor
		Roadlrl(Place from, Place to, String roadName, int length) {

			// Initialize provided information:
			this.from = from;
			this.to= to;
			this.roadName = roadName;
			this.length = length;    		

			// Initialize other variables:
			this.roadListeners = new HashSet<>();
			this.isChosen = false; 

		}


		//---------------------------------------------------------------------------------------------
		// Auto Inherited Methods - Road

		// ---Done---
		// Add the RoadListener rl to this place.
		// Note: A road can have multiple listeners
		public void addListener(RoadListener rl) {
			roadListeners.add(rl);
			rlChangeOccured();
		}

		// ---Done---
		// Delete the RoadListener rl from this place.
		public void deleteListener(RoadListener rl) {
			roadListeners.remove(rl);
			rlChangeOccured();
		}


		// ---Done---
		// Return the first place of this road
		// Note: The first place of a road is the place whose name
		// comes EARLIER in the alphabet.
		public Place firstPlace() {

			// Figure out which place comes earlier in the alphabet
			int comparisonResult = from.getName().compareTo(to.getName());    		

			// Called when a place has been moved:
			rlChangeOccured();

			// Return the appropriate place
			if(comparisonResult < 0) {
				// From is earlier in the alphabet
				return from;
			}
			else {
				// To is earlier in the alphabet
				return to;
			}
		}


		// ---Done---
		// Return the second place of this road
		// Note: The second place of a road is the place whose name
		// comes LATER in the alphabet.
		public Place secondPlace() {
			// Figure out which place comes earlier in the alphabet
			int comparisonResult = from.getName().compareTo(to.getName());    		

			// Called when a place has been moved:
			rlChangeOccured();        	

			// Return the appropriate place
			if(comparisonResult < 0) {
				// To is later in the alphabet
				return to;
			}
			else {
				// From is later in the alphabet
				return from;
			}
		}


		// ---Done---
		// Return true if this road is chosen as part of the current trip
		public boolean isChosen() {
			return isChosen;
		}


		// ---Done---
		// Return the name of this road
		public String roadName() {
			return roadName;
		}


		// ---Done---
		// Return the length of this road
		public int length() {
			return length;
		}


		// ---Done---
		// Return a string containing information about this road 
		// in the form (without quotes, of course!):
		// "firstPlace(roadName:length)secondPlace"
		public String toString() {        	
			return this.firstPlace().getName() + "(" + roadName + ":" 
					+ Integer.toString(length) + ")" + this.secondPlace().getName();
		}


		// ---Done---
		// Alert all road listeners that places have changed
		// Not used atm, but might be used in the future to change the color of the
		// road or something if it is selected as part of the shortest trip distance
		// (assuming assignment 3 is going to be working with this class on a GUI)
		private void rlChangeOccured() {

			// Variables
			RoadListener currentRL = null;
			Iterator<RoadListener> itr = roadListeners.iterator();

			// Call placesChanged for all map listeners:
			while(itr.hasNext()) {
				currentRL = itr.next();
				currentRL.roadChanged();        		
			}    	
		}
	}
}
