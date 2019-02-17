// EDC: Assignment 3
// By: Adam Bartlett, ID:a1646071
// Date: 31/10/18

// Import Libraries:
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;

public class MapEditor {
	
	// GUI display properties:
	static final int iconSize = 20;
	static final Color backgroundColor = Color.WHITE;	
	
	// Global Variables:
	static private JFrame window;
	static private MapPanel GUI;
	static private boolean changesMade;	// To-do
	static private String state;
	
	// Main Method: Done
	public static void main(String[] args) {		
		
		// Initialize Global Variables:
		changesMade = false;
		
		// Set-up the GUI:
		setupGui();		
	}
	
	
	// Initialize the MapEditor GUI: Done
	private static void setupGui() {
		
		// Variables: GUI Components
		window = new JFrame();
		GUI = new MapPanel();
		JMenuBar menuBar = new JMenuBar();		
		
		//----------------------------------------------------------------
		// Set-up Frame:	
		window.setTitle("Assignmnet 3 - Map Editor");
		window.setSize(800, 800);	
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = (int) ((screenSize.getWidth() - window.getWidth()) / 2);
	    int y = (int) ((screenSize.getHeight() - window.getHeight()) / 2);
	    window.setLocation(x, y);	    
	    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//----------------------------------------------------------------	
		// Set-up Menu Bar:
		JMenu file = new JMenu("File");		
		JMenu edit = new JMenu("Edit");	
		file.addMenuListener(new MapEditorMenuListener());
		edit.addMenuListener(new MapEditorMenuListener());
					
		// Create File Menu Items:
		JMenuItem open = new JMenuItem("Open...");
		JMenuItem saveAs = new JMenuItem("Save As...");
		JMenuItem append = new JMenuItem("Append...");
		JMenuItem quit = new JMenuItem("Quit");			
		open.addActionListener(new fileOpenListener());
		saveAs.addActionListener(new fileSaveAsListener());
		append.addActionListener(new fileAppendListener());
		quit.addActionListener(new fileQuitListener());			
		file.add(open);
		file.add(saveAs);
		file.add(append);
		file.add(quit);
					
		// Set Up File Menu Shortcuts:
		KeyStroke open_SC = KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke saveAs_SC = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke append_SC = KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
		KeyStroke quit_SC = KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK);
		open.setAccelerator(open_SC);
		saveAs.setAccelerator(saveAs_SC);
		append.setAccelerator(append_SC);
		quit.setAccelerator(quit_SC);
					
		// Create Edit Menu Items:
		JMenuItem newPlace = new JMenuItem("New place");
		JMenuItem newRoad = new JMenuItem("New road");
		JMenuItem setStart = new JMenuItem("Set start");
		JMenuItem unsetStart = new JMenuItem("Unset start");
		JMenuItem setEnd = new JMenuItem("Set end");
		JMenuItem unsetEnd = new JMenuItem("Unset end");
		JMenuItem delete = new JMenuItem("Delete");			
		newPlace.addActionListener(new editNewPlaceListener());
		newRoad.addActionListener(new editNewRoadListener());
		setStart.addActionListener(new editSetStartListener());
		unsetStart.addActionListener(new editUnsetStartListener());
		setEnd.addActionListener(new editSetEndListener());
		unsetEnd.addActionListener(new editUnsetEndListener());
		delete.addActionListener(new editDeleteListener());			
		edit.add(newPlace);
		edit.add(newRoad);
		edit.add(setStart);
		edit.add(unsetStart);
		edit.add(setEnd);
		edit.add(unsetEnd);
		edit.add(delete);		
		
		//----------------------------------------------------------------
		// Set-up MapPanel:
		GUI.setLayout(null);
		GUI.setState("Normal");
		GUI.setBackground(backgroundColor);		
		
		//----------------------------------------------------------------
		// Construct the GUI:		
		menuBar.add(file);
		menuBar.add(edit);		
		window.setJMenuBar(menuBar);
		window.add(GUI);
		window.setVisible(true);		
	}
	
	
	// Creates a backup of the currentMap: Done
	private static void createBackup() {		
		
		MapReaderWriter mrw = new MapReaderWriter();
		File backup = new File(System.getProperty("user.dir") + "\\backup.txt");
		
		try {
			Writer w = new FileWriter(backup.getPath());			
			mrw.write(w, GUI.getMap());					
			w.close();			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// Retrieves the backup of the currentMap: Done
	private static void retrieveBackup() {
		
		GUI.createNewMap();
		MapReaderWriter mrw = new MapReaderWriter();
		File backup = new File(System.getProperty("user.dir") + "\\backup.txt");
		
		try {
			Reader r = new FileReader(backup.getPath());			
			mrw.read(r, GUI.getMap());					
			r.close();			
		} 
		catch (IOException | MapFormatException e) {
			e.printStackTrace();
		}
		deleteBackup();
	}
	
	
	// Deletes the backup of the currentMap: Done
	private static void deleteBackup() {
		File backup = new File(System.getProperty("user.dir") + "\\backup.txt");
		backup.delete();
	}
	
	
	//-----------------------------------------------------------------------------------------
	// Menu Listeners:
	private static class MapEditorMenuListener implements MenuListener {

		@Override
		public void menuCanceled(MenuEvent arg0) {
			// Do nothing...
			
		}

		@Override
		public void menuDeselected(MenuEvent arg0) {
			// Do nothing...
			
		}

		@Override
		public void menuSelected(MenuEvent arg0) {
			GUI.setState("Normal");
			GUI.repaint();
		}
	}
	
	
	//-----------------------------------------------------------------------------------------
	// File Menu Listeners:
	private static class fileOpenListener implements ActionListener {		
		
		// Attempts to open the file selected by the user: Done
		public void actionPerformed(ActionEvent e) {
			
			// Create a backup of the current map in case something goes wrong:
			createBackup();
			
			// Variables:
			Reader r = null;			
			MapReaderWriter mrw = new MapReaderWriter();
			JFileChooser fc = new JFileChooser();
			
			// File chooser set-up:
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("*.map", "map"));
			fc.setAcceptAllFileFilterUsed(false);
			
			// Prompt user to select the appropriate file:
			if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				
				// Obtain selected file:
				File toOpen = fc.getSelectedFile();
				
				// Set up reader:
				try {
					r = new FileReader(toOpen.getAbsolutePath());
					
					// Create new map:
					GUI.createNewMap();
					
					// Read in map					
					mrw.read(r, GUI.getMap());					
		            r.close();		            
					deleteBackup();
		            
				} catch (IOException | MapFormatException e1) {
					retrieveBackup();
					JOptionPane.showMessageDialog(window, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				changesMade = false;
			}
			else {
				retrieveBackup();
			}					
		}			
	}	
		
	private static class fileSaveAsListener implements ActionListener {		
		
		// Save the current map in the users desired location: Done
		public void actionPerformed(ActionEvent e) {
			
			// Variables:
			Writer w = null;
			MapReaderWriter mrw = new MapReaderWriter();
			JFileChooser fc = new JFileChooser();
			
			// File chooser set-up:
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("*.map", "map"));
			fc.setAcceptAllFileFilterUsed(false);
			
			// Prompt user to select location to save the file:
			if(fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
							
				// Obtain selected file:
				File toSave = fc.getSelectedFile();
				
				// Make sure file is of type map:
				if(!toSave.toString().endsWith(".map")) {
					toSave = new File(toSave.getParent(), toSave.getName() + ".map");					
				}
					
				// Set up writer:
				try {
					w = new FileWriter(toSave.getPath());
									
					// Save Map:
					mrw.write(w, GUI.getMap());					
					w.close();						
				} 
				catch (IOException e1) {
					JOptionPane.showMessageDialog(window, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				changesMade = false;
			}			
		}			
	}	
		
	private static class fileAppendListener implements ActionListener {		
		
		// Load in additional information to append to the current map: Done
		public void actionPerformed(ActionEvent e) {

			// Create a backup of the current map in case something goes wrong:
			createBackup();
			
			// Variables:
			Reader r = null;
			MapReaderWriter mrw = new MapReaderWriter();
			JFileChooser fc = new JFileChooser();
			
			// File chooser set-up:
			fc.setCurrentDirectory(new File(System.getProperty("user.dir")));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("*.map", "map"));
			fc.setAcceptAllFileFilterUsed(false);
						
			// Prompt user to select appropriate file:
			if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							
				// Obtain selected file:
				File toOpen = fc.getSelectedFile();
							
				// Set up reader:
				try {
					r = new FileReader(toOpen.getAbsolutePath());
								
					// Read in map
					mrw.read(r, GUI.getMap());					
			        r.close();					
					deleteBackup();					            
				} 
				catch (IOException | MapFormatException e1) {
					retrieveBackup();
					JOptionPane.showMessageDialog(window, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);					
				}
			}
			else {
				retrieveBackup();
			}
		}			
	}	
		
	private static class fileQuitListener implements ActionListener {		
		
		// Done:
		public void actionPerformed(ActionEvent e) {
			
			// Check if any changes have been made to the map:
			if(changesMade) {				
				int input = JOptionPane.showConfirmDialog(window, "Unsaved changes have been made to the map.\n"
						+ "Are you sure you want to quit?", "Warning!", JOptionPane.WARNING_MESSAGE);

				// Exit if the user answered yes:
				if(input == 0) {
					System.exit(0);
				}
			}
			else {
				System.exit(0);
			}		
		}			
	}
	
		
	//-----------------------------------------------------------------------------------------
	// Edit Menu Listeners:	
	private static class editNewPlaceListener implements ActionListener {		
		
		// Attempt to add a new place to the current map: Done
		public void actionPerformed(ActionEvent e) {
			
			// Prompt user for name of the new place using a pop up box:
			String input = JOptionPane.showInputDialog("Please enter the new places name:");

			// If required add the new place to the map:
			if(input != null) {				
				try {
					GUI.getMap().newPlace(input, (window.getWidth()/2) - iconSize, 
							(window.getHeight()/2) - iconSize);
				}
				catch(IllegalArgumentException e2) {
					JOptionPane.showMessageDialog(window, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}			
	}	
		
	private static class editNewRoadListener implements ActionListener {		
		
		// Attempt to add a new road to the current map: Done
		public void actionPerformed(ActionEvent e) {
			
			// Prompt user for name and length of the new road using a pop up box:
			String input = JOptionPane.showInputDialog("Please enter the new roads name and length"
					+ "\nEx: name length");
			
			// Switch the state of the GUI into RoadInsert Mode:
			if(input != null) {
				
				// Split input into road name and length:
				String[] inputWords = input.split(" ");
				
				if(inputWords.length == 2) {
					GUI.setNewRoadName(inputWords[0]);
					GUI.setNewRoadLength(Integer.parseInt(inputWords[1]));
					GUI.deSelectAll();
					GUI.setState("RoadInsert");
				}
				else {
					JOptionPane.showMessageDialog(window, "Incorrect Input...", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}			
		}			
	}	
	
	private static class editSetStartListener implements ActionListener {		
		
		// Update the current starting location on the map: Done
		public void actionPerformed(ActionEvent e) {

			// Variables:
			PlaceIcon currentPI = null;
			Set<PlaceIcon> selectedPlaces = new HashSet<>();
			Iterator<PlaceIcon> itr = GUI.getDisplayedPlaces().iterator();
			
			// Find out what PlaceIcons are currently selected:
			while(itr.hasNext()) {				
				currentPI = itr.next();				
				if(currentPI.getIsSelected()) {
					selectedPlaces.add(currentPI);
				}
			}
			
			// Perform the appropriate action depending on the number of selected places:
			if(selectedPlaces.size() == 1) {
				
				// Set place as starting location:
				selectedPlaces.iterator().next().deSelect();
				GUI.getMap().setStartPlace(selectedPlaces.iterator().next().getPlace());		
			}
			else if(selectedPlaces.size() == 0) {
				JOptionPane.showMessageDialog(window, "You must select a place before you can set "
						+ "the starting location.", "Whoopsie...", JOptionPane.INFORMATION_MESSAGE);				
			}
			else {
				JOptionPane.showMessageDialog(window, "Only one place can be selected as the "
						+ "starting location...", "Warning!", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
		
	private static class editUnsetStartListener implements ActionListener {		
		
		// Un-set the starting location on the current map: Done
		public void actionPerformed(ActionEvent e) {			
			if((GUI.getMap().getStartPlace()) != null) {
				GUI.getMap().setStartPlace(null);
			}					
		}			
	}	
		
	private static class editSetEndListener implements ActionListener {		
		
		// Update the current ending location on the map: Done
		public void actionPerformed(ActionEvent e) {
			
			// Variables:
			PlaceIcon currentPI = null;
			Set<PlaceIcon> selectedPlaces = new HashSet<>();
			Iterator<PlaceIcon> itr = GUI.getDisplayedPlaces().iterator();
			
			// Find out what PlaceIcons are currently selected:
			while(itr.hasNext()) {				
				currentPI = itr.next();				
				if(currentPI.getIsSelected()) {
					selectedPlaces.add(currentPI);
				}
			}
			
			// Perform the appropriate action depending on the number of selected places:
			if(selectedPlaces.size() == 1) {
				
				// Set place as ending location:
				selectedPlaces.iterator().next().deSelect();
				GUI.getMap().setEndPlace(selectedPlaces.iterator().next().getPlace());		
			}
			else if(selectedPlaces.size() == 0) {
				JOptionPane.showMessageDialog(window, "You must select a place before you can set "
						+ "the ending location.", "Whoopsie...", JOptionPane.INFORMATION_MESSAGE);				
			}
			else {
				JOptionPane.showMessageDialog(window, "Only one place can be selected as the "
						+ "ending location...", "Warning!", JOptionPane.WARNING_MESSAGE);
			}
		}			
	}	
	
	private static class editUnsetEndListener implements ActionListener {		
		
		// Un-set the ending location on the current map: Done
		public void actionPerformed(ActionEvent e) {
			if((GUI.getMap().getEndPlace()) != null) {
				GUI.getMap().setEndPlace(null);
			}	
		}			
	}	
		
	private static class editDeleteListener implements ActionListener {		
	
		// Delete the selected items from the map: Done
		public void actionPerformed(ActionEvent e) {
			
			// Variables:
			PlaceIcon currentPI = null;
			Set<PlaceIcon> selectedPlaces = new HashSet<>();
			Iterator<PlaceIcon> itr = GUI.getDisplayedPlaces().iterator();
			
			// Find out what PlaceIcons are currently selected:
			while(itr.hasNext()) {				
				currentPI = itr.next();				
				if(currentPI.getIsSelected()) {
					selectedPlaces.add(currentPI);
				}
			}
			
			Iterator<PlaceIcon> itr2 = selectedPlaces.iterator();
			
			// Delete all of the selected places:
			while(itr2.hasNext()) {
				currentPI = itr2.next();
				GUI.getMap().deletePlace(currentPI.getPlace());
			}	
		}			
	}
	
	
	//---------------------------------------------------------------------------------------------
	// Map Panel Class
	//---------------------------------------------------------------------------------------------
	private static class MapPanel extends JPanel implements MapListener {
		
		// Class Variables:		
		private Map currentMap;
		private int tripDistance;
		private Set<RoadIcon> displayedRoads;
		private Set<PlaceIcon> displayedPlaces;
		
		// Selection Box Variables:
		private boolean drawSelBox;
		private Rectangle selectionBox;	
		private int selBoxStartX, selBoxStartY, selBoxCurrentX, selBoxCurrentY;
		
		// RoadInsert State Variables:
		private int newRodLength;
		private String newRoadName;
		private Place newRoadCon1, newRoadCon2;
		private int cursorXpos, cursorYpos;
		
		// Constructor: Done
		MapPanel() {

			// Initialize class variables:
			displayedPlaces = new HashSet<>();
			displayedRoads = new HashSet<>();
			createNewMap();
			tripDistance = -1;
			
			// Initialize selection box variables:
			selectionBox = new Rectangle();
			drawSelBox = false;
			
			//-------------------------------------------------------------------------------------
			// Set-up the mouse listener for the MapPanel:
			this.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					if(state.compareTo("Normal") == 0) {
						drawSelBox = false;
						repaint();
					}					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					if(state.compareTo("Normal") == 0) {
						selBoxStartX = e.getX();
						selBoxStartY = e.getY();
						selBoxCurrentX = selBoxStartX;
						selBoxCurrentY = selBoxStartY;
						drawSelBox = true;
					}
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// Do nothing...
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					// Do nothing...
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {					
					if(state.compareTo("Normal") == 0) {						
						deSelectAll();	
					}
					else if(state.compareTo("RoadInsert") == 0) {
						setState("Normal");
					}
				}
			});
			
			//-------------------------------------------------------------------------------------
			// Set-up mouse motion listener for the mapPanel:
			this.addMouseMotionListener(new MouseMotionListener() {
				
				@Override
				public void mouseMoved(MouseEvent e) {
					if(state.compareTo("RoadInsert") == 0) {
						cursorXpos = e.getX();
						cursorYpos = e.getY();
						repaint();
					}
				}
				
				@Override
				public void mouseDragged(MouseEvent e) {					
					if(state.compareTo("Normal") == 0) {
						// Variables:
						PlaceIcon currentPI = null;
						Iterator<PlaceIcon> itr = displayedPlaces.iterator();
						
						// Update current selection box location:
						selBoxCurrentX = e.getX();
						selBoxCurrentY = e.getY();	
						
						// Update the selected icons depending on whether or not they are located
						// in the selection box:
						while(itr.hasNext()) {
							currentPI = itr.next();								
							if(selectionBox.intersects(currentPI.getBounds())) {
								currentPI.select();
							}
							else {
								currentPI.deSelect();
							}						
						}
						repaint();
					}					
				}
			});			
		}
		
		
		//-----------------------------------------------------------------------------------------
		// Accessors:
		public Map getMap() {
			return currentMap;
		}
		
		public Set<PlaceIcon> getDisplayedPlaces() {
			return displayedPlaces;
		}
		
		public Place getNewRoadCon1() {
			return newRoadCon1;
		}
				
		public Place getNewRoadCon2() {
			return newRoadCon2;
		}
		
		public String getNewRoadName() {
			return newRoadName;
		}
		
		public int getNewRoadLength() {
			return newRodLength;
		}
		
		
		//-----------------------------------------------------------------------------------------
		// Modifiers:
		public void setNewRoadCon1(Place p) {
			newRoadCon1 = p;
		}
		
		public void setNewRoadCon2(Place p) {
			newRoadCon2 = p;
		}

		public void setNewRoadName(String s) {
			this.newRoadName = s;
		}
		
		public void setNewRoadLength(int x) {
			this.newRodLength = x;
		}
		
		
		//-----------------------------------------------------------------------------------------
		// Class Methods:
		
		// Creates a new map: Done
		public void createNewMap() {
			
			// Create the new map and associated listener:
			currentMap = new MapImpl();
			currentMap.addListener(this);
			
			// Wipe all previously painted PlaceIcons off of the GUI:
			PlaceIcon currentPI = null;
			Iterator<PlaceIcon> itrPI = displayedPlaces.iterator();
			
			while(itrPI.hasNext()) {				
				currentPI = itrPI.next();
				GUI.remove(currentPI);
				repaint();
			}
			this.displayedPlaces = new HashSet<>();
			
			
			// Wipe all previously painted RoadIcons off of the GUI:
			RoadIcon currentRI = null;
			Iterator<RoadIcon> itrRI = displayedRoads.iterator();
			
			while(itrRI.hasNext()) {				
				currentRI = itrRI.next();
				GUI.remove(currentRI);
				repaint();
			}			
			this.displayedRoads = new HashSet<>();
		}
		
		
		// Change the current state of the GUI: Done
		public void setState(String s) {			
			if(s.compareTo("Normal") == 0) {
				
				// Update cursor type:
				Cursor NormalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
				window.setCursor(NormalCursor);
				
				// Reset variables:
				newRoadCon1 = null;
				newRoadCon2 = null;				
				
				// Switch state:
				state = "Normal";
			}
			else if(s.compareTo("RoadInsert") == 0) {
				
				// Update cursor type:
				Cursor crossHairCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
				window.setCursor(crossHairCursor);
				
				// Switch state:
				state = "RoadInsert";
			}
			else {
				System.out.println("Err: Invalid cursor type...");
			}			
		}
		
		
		// De-select all of the PlaceIcons: Done
		public void deSelectAll() {
			PlaceIcon currentPI = null;
			Iterator<PlaceIcon> itr = displayedPlaces.iterator();
			
			// De-Select all PlaceIcons 
			while(itr.hasNext()) {
				currentPI = itr.next();
				currentPI.deSelect();
			}
		}
		
		
		//-----------------------------------------------------------------------------------------
		// Map Listener Methods:
		
		// Called whenever the number of places in the map has changed: Done
	    public void placesChanged() {
	    	
	    	changesMade = true;
	    	
	    	// Variables:	    	
	    	Place currentMP;
	    	PlaceIcon currentVP;
	    	boolean isNewPlace = true;
	    	Set<PlaceIcon> toRemove = new HashSet<>();
	    	Iterator<Place> modelItr = currentMap.getPlaces().iterator();
	    	Iterator<PlaceIcon> viewItr = displayedPlaces.iterator();	    	
	    	
	    	//-------------------------------------------------------------------------------------
	    	// Check for new places:	    	
	    	while(modelItr.hasNext()) {	    	    		
	    		
	    		currentMP = modelItr.next();
	    		viewItr = displayedPlaces.iterator();
	    		
	    		// Compare model list of places against current list of displayed places:
	    		while(viewItr.hasNext()) {	    			
	    			currentVP = viewItr.next();
	    			if(currentMP.getName().compareTo(currentVP.getPlace().getName()) == 0) {
	    				isNewPlace = false;
	    			}	    			
	    		}
	    		
	    		if(isNewPlace) {
	    			
	    			// Create new PlaceIcon:
	    			PlaceIcon newPI = new PlaceIcon(currentMP);
	    			
	    			// Add PlaceIcon as listener of new place:
	    			currentMP.addListener(newPI);
	    			
	    			// Add PlaceIcon to list of displayed places:
	    			displayedPlaces.add(newPI);	    			
	    		}
	    		else {
	    			isNewPlace = true;
	    		}	    		
	    	}
	    	
	    	//-------------------------------------------------------------------------------------
	    	// Reset iterators:
	    	modelItr = currentMap.getPlaces().iterator();
	    	viewItr = displayedPlaces.iterator();
	    	
	    	// Check for deleted places:
	    	while(viewItr.hasNext()) {	    		
	    		currentVP = viewItr.next();	    		
	    		if(currentMap.findPlace(currentVP.getPlace().getName()) == null) {	    			
	    			// Place has been deleted therefore add to remove list:
	    			toRemove.add(currentVP);
	    		}	    		
	    	}	    	
	    	
	    	// Delete required places from GUI:
	    	Iterator<PlaceIcon> deleteItr = toRemove.iterator();
	    	
	    	while(deleteItr.hasNext()) {	    		
	    		currentVP = deleteItr.next();
	    		GUI.remove(currentVP);
    			displayedPlaces.remove(currentVP);	    		
	    	}
	    	GUI.repaint();
	    }

	    
	    // Called whenever the number of roads in the map has changed: Done
	    public void roadsChanged() {
	    	
	    	changesMade = true;
	    	
	    	// Variables:	    	
	    	Road currentMR;
	    	RoadIcon currentVR;
	    	boolean isNewRoad = true;
	    	Set<RoadIcon> toRemove = new HashSet<>();
	    	Iterator<Road> modelItr = currentMap.getRoads().iterator();
	    	Iterator<RoadIcon> viewItr = displayedRoads.iterator();
	    	
	    	//-------------------------------------------------------------------------------------
	    	// Check for new places:	
	    	while(modelItr.hasNext()) {	    		
	    		
	    		currentMR = modelItr.next();
	    		viewItr = displayedRoads.iterator();
	    		
	    		// Compare model list of places against current list of displayed places:
	    		while(viewItr.hasNext()) {	    			
	    			currentVR = viewItr.next();
	    			
	    			// Check name:
	    			if(currentMR.roadName().compareTo(currentVR.getRoad().roadName()) == 0) {
	    				
	    				// Check first and second place:
	    				if((currentMR.firstPlace().getName().compareTo(currentVR.getRoad().firstPlace().getName()) == 0)
	    						&& (currentMR.secondPlace().getName().compareTo(currentVR.getRoad().secondPlace().getName()) == 0)) {
	    					isNewRoad = false;
	    				}
	    			}	    			
	    		}
	    		
	    		if(isNewRoad) {
	    			
	    			// Create new PlaceIcon:
	    			RoadIcon newRI = new RoadIcon(currentMR);
	    			
	    			// Add PlaceIcon as listener of new place:
	    			currentMR.addListener(newRI);
	    			
	    			// Add PlaceIcon to list of displayed places:
	    			displayedRoads.add(newRI);
	    		}
	    		else {
	    			isNewRoad = true;
	    		}
	    	}
	    	
	    	//-------------------------------------------------------------------------------------
    		// Reset iterators:
    		boolean doesExist = false;
    		viewItr = displayedRoads.iterator();
	    	
	    	// Check for deleted roads:
	    	while(viewItr.hasNext()) {
	    		
	    		currentVR = viewItr.next();
	    		modelItr = currentMap.getRoads().iterator();
	    		
	    		// Make sure the model contains the currentVR:
	    		while(modelItr.hasNext()) {
	    			
	    			currentMR = modelItr.next();
	    			
	    			// Check name:
	    			if(currentVR.getRoad().roadName().compareTo(currentMR.roadName()) == 0) {
	    				
	    				// Check first and second place:
	    				if((currentVR.getRoad().firstPlace().getName().compareTo(currentMR.firstPlace().getName()) == 0)
	    						&& (currentVR.getRoad().secondPlace().getName().compareTo(currentMR.secondPlace().getName()) == 0)) {
	    					doesExist = true;
	    				}
	    			}	    			
	    		}
	    		
	    		if(!doesExist) {
	    			// The road no longer exists, therefore add it to the list of roads to remove:
	    			toRemove.add(currentVR);
	    		}
	    		else {
	    			doesExist = false;	
	    		}	    		    		
	    	}
	    	
	    	// Delete required roads from the GUI:
	    	Iterator<RoadIcon> deleteItr = toRemove.iterator();
	    	
	    	while(deleteItr.hasNext()) {
	    		currentVR = deleteItr.next();
	    		GUI.remove(currentVR);
	    		displayedRoads.remove(currentVR);
	    	}
	    	GUI.repaint();
	    }

	    
	    // Called whenever something about the map has changed: TBA
	    public void otherChanged() {	    	
	    	// TBA: This will be used for Ex: 21...
	    	changesMade = true;
	    	repaint();	    	
	    }
	    

	    //-----------------------------------------------------------------------------------------
	  	// Map Panels Paint Method: Done
		@Override
		protected void paintComponent(Graphics g) {
			
			// Variables:
			int horizontalDir = selBoxCurrentX - selBoxStartX;
			int verticalDir = selBoxCurrentY - selBoxStartY;			
			
			// Set-up graphics components:
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, 
					new float[] {5}, 0));
			
			// If required paint new road line:
			if(newRoadCon1 != null) {				
				Line2D roadLine = new Line2D.Double(newRoadCon1.getX() + iconSize/2, 
						newRoadCon1.getY() + iconSize/2, cursorXpos, cursorYpos);
				g2.draw(roadLine);
			}			
			
			// If required draw the selection box in the correct quadrant:
			if(drawSelBox) {
				if((horizontalDir > 0) && (verticalDir < 0)) {	// Up Right:
					selectionBox.setRect(selBoxStartX, selBoxCurrentY, horizontalDir, -verticalDir);					
				}			
				else if((horizontalDir > 0) && (verticalDir > 0)) {	// Down Right:
					selectionBox.setRect(selBoxStartX, selBoxStartY, horizontalDir, verticalDir);
				}
				else if((horizontalDir < 0) && (verticalDir > 0)) {	// Down Left:
					selectionBox.setRect(selBoxCurrentX, selBoxStartY, -horizontalDir, verticalDir);
				}
				else if((horizontalDir < 0) && (verticalDir < 0)) {	// Up Left:
					selectionBox.setRect(selBoxCurrentX, selBoxCurrentY, -horizontalDir, -verticalDir);
				}
			}
			else {
				// Make the selection box non existent:
				selectionBox.setRect(-1,-1,0,0);				
			}
			
			// Draw the rectangle:
			g2.draw(selectionBox);
			
			// Print out the current trip distance:
			if(tripDistance == -1) {
				g2.drawString("Trip Distance = No Route...", 5, window.getHeight() - 70);
			}
			else {
				g2.drawString("Trip Distance = " + tripDistance, 5, window.getHeight() - 70);
			}
		}
	}
	
	//---------------------------------------------------------------------------------------------
	// PlaceIcon Class
	//---------------------------------------------------------------------------------------------
	private static class PlaceIcon extends JComponent implements PlaceListener {
	
		// Class Variables:		
		private Place p;	
		private Rectangle icon;
		private boolean isSelected;
		private int startX, startY, currentX, currentY;
		
		// Constructor: Done
		PlaceIcon(Place p) {
			
			// Initialize Class Variables:			
			this.p = p;
			isSelected = false;
			icon = new Rectangle(0, 0, iconSize, iconSize);			
			
			// Add this PlaceIcon to the map and display it:
			GUI.add(this);
			
			//-------------------------------------------------------------------------------------
			// Set-up the mouse listener for this PlaceIcon: Done
			this.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					// Do nothing...
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					if(state.compareTo("Normal") == 0) {
						startX = e.getX();
						startY = e.getY();
					}											
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// Do nothing...
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					// Do nothing...
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					if(state.compareTo("Normal") == 0) {
						
						// Variables;
						PlaceIcon currentPI = null;
						boolean othersDeselected = false;
						Iterator<PlaceIcon> itr = GUI.getDisplayedPlaces().iterator();
						
						// De-Select all other places:
						while(itr.hasNext()) {
							currentPI = itr.next();						
							if(p.getName().compareTo(currentPI.getPlace().getName()) != 0) {
								
								if(currentPI.getIsSelected()) {									
									currentPI.deSelect();
									othersDeselected = true;
								}								
							}
						}	
						
						// Switch selected state of icon unless it was already selected in a group:
						if(!(othersDeselected && isSelected)) {
							isSelected = !isSelected;
						}						
						GUI.repaint();
					}
					else if(state.compareTo("RoadInsert") == 0) {
						
						// Continue with new road creation:
						if(GUI.getNewRoadCon1() == null) {
							
							// Highlight newRoadCon1:
							select();
							
							// Update initial connection:
							GUI.setNewRoadCon1(p);							
						}
						else if(GUI.getNewRoadCon2() == null) {
							
							// Update final connection:
							GUI.setNewRoadCon2(p);
							
							// Create new road:
							try {
								GUI.currentMap.newRoad(GUI.getNewRoadCon1(), GUI.getNewRoadCon2(),
										GUI.getNewRoadName(), GUI.getNewRoadLength());
							}
							catch(IllegalArgumentException e1) {
								JOptionPane.showMessageDialog(window, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
								GUI.repaint();
							}				
							
							// De-select icons:
							GUI.deSelectAll();
							
							// Finished adding road therefore reset the state to normal:
							GUI.setState("Normal");							
						}
						else {
							System.out.println("Error in PlaceIcon mouse clicked");
						}
					}
				}
			});
			
			//-------------------------------------------------------------------------------------
			// Set-up the mouse motion listener for this PlaceIcon: Done
			this.addMouseMotionListener(new MouseMotionListener() {
				
				@Override
				public void mouseMoved(MouseEvent e) {
					if(state.compareTo("RoadInsert") == 0) {						
						GUI.cursorXpos = p.getX() + e.getX();
						GUI.cursorYpos = p.getY() + e.getY();
						GUI.repaint();
					}
				}
				
				@Override
				public void mouseDragged(MouseEvent e) {
					if(state.compareTo("Normal") == 0) {
						// Get current mouse location in PlaceIcon:
						currentX = e.getX();
						currentY = e.getY();
						
						// Update location of Place:
						p.moveBy((currentX - startX), (currentY - startY));
					}
				}
			});
		}
		
		
		//-----------------------------------------------------------------------------------------
		// Accessors:		
		public Place getPlace() {
			return p;
		}
		
		public boolean getIsSelected() {
			return isSelected;
		}
		
		
		//-----------------------------------------------------------------------------------------
		// Class Methods:
		
		// Selects the PlaceIcon: Done
		public void select() {
			isSelected = true;
			repaint();
		}
		
		// De-selects the PlaceIcon: Done
		public void deSelect() {
			isSelected = false;
			repaint();
		}
		
		
		//-----------------------------------------------------------------------------------------
		// Place Listener Method:
		
		// Called whenever the visible state of a place has changed: Done
		public void placeChanged() {
			changesMade = true;
			this.setBounds(p.getX(), p.getY(), (iconSize + 1), (iconSize + 1));
			GUI.repaint();
		}
		

		//-----------------------------------------------------------------------------------------
	  	// PanelIcons Paint Method: Done
		@Override
		protected void paintComponent(Graphics g) {			

			// Set-up graphics components:
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;			
			
			// Set background color of de-selected icon:
			if(!isSelected) {
				g2.setColor(backgroundColor);
				g2.fill(icon);
			}
			
			// Select appropriate color for the icon:
			if((p.isStartPlace()) && (p.isEndPlace())) {
				g2.setColor(Color.ORANGE);
			}
			else if(p.isStartPlace()) {
				g2.setColor(Color.RED);
			}
			else if(p.isEndPlace()) {
				g2.setColor(Color.GREEN);
			}
			else {
				g2.setColor(Color.BLACK);
			}
			
			// If the item is selected make sure it is fully painted in the correct color:
			if(isSelected) {
				g2.fill(icon);
			}
			
			// Draw the rectangle:	
			g2.setStroke(new BasicStroke(3));
			g2.draw(icon);
			
			// Draw name of place underneath the icon:
			g2.setClip(0, 0, window.getWidth(), window.getHeight());
			g2.setColor(Color.BLACK);
			g2.drawString(p.getName(), 0, 35);
		}
	}
	
	//---------------------------------------------------------------------------------------------
	// RoadIcon Class
	//---------------------------------------------------------------------------------------------
	public static class RoadIcon extends JComponent implements RoadListener {

		// Class Variables:
		private Road r;
		private Place p1, p2;
		private Line2D roadLine;			
		private Point2D firstPlaceLoc, secondPlaceLoc;		
		
		// Constructor: Done
		RoadIcon(Road r) {
			
			// Initialize variables:
			this.r = r;
			this.p1 = r.firstPlace();
			this.p2 = r.secondPlace();
			roadLine = new Line2D.Double(0, 0, 0, 0);
			firstPlaceLoc = new Point2D.Double();
			secondPlaceLoc = new Point2D.Double();
			
			// Add roadIcon to the GUI:
			GUI.add(this);
		}
		
		
		//-----------------------------------------------------------------------------------------
		// Accessors:
		public Road getRoad() {
			return r;
		}		
		
		
		//-----------------------------------------------------------------------------------------
		// Road Listener Method:
		
		// Called whenever the visible state of a road has changed: Done
		public void roadChanged() {			
			changesMade = true;
			firstPlaceLoc.setLocation(p1.getX() + 10, p1.getY() + 10);
			secondPlaceLoc.setLocation(p2.getX() + 10, p2.getY() + 10);
			this.setBounds(0, 0, window.getWidth(), window.getHeight());
		}
		
		
		//-----------------------------------------------------------------------------------------
	  	// RoadIcons Paint Method: Done
		@Override
		protected void paintComponent(Graphics g) {	

			// Set-up graphics components:
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;		

			// Update road location:
			roadLine.setLine(firstPlaceLoc.getX(), firstPlaceLoc.getY(), 
					secondPlaceLoc.getX(), secondPlaceLoc.getY());
			
			// Draw road:
			g2.setStroke(new BasicStroke(2));			
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(roadLine);
			
			// Draw road name:
			g2.setColor(Color.BLACK);
			g2.drawString(r.roadName() + ", " + r.length(), 
					(int) (firstPlaceLoc.getX() + ((secondPlaceLoc.getX() - firstPlaceLoc.getX())/2)), 
					(int) (firstPlaceLoc.getY() + ((secondPlaceLoc.getY() - firstPlaceLoc.getY())/2)));			
		}	
	}
}