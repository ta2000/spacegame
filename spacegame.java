import java.io.*;
// File
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
// Scanner
import java.util.Scanner;
import java.util.Random;
// Array
import java.util.ArrayList;

class random
{
	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}
}

class Location
{
	String name;
	Faction faction;
	int x;
	int y;
	int outputMin;
	int outputMax;
	int maxHp;
	int hp;
	boolean hidden;

	Location(String name, Faction faction, int x, int y, int min, int max, int hp)
	{
		this.name = name;
		this.faction = faction;
		this.x = x;
		this.y = y;
		this.outputMin = min;
		this.outputMax = max;
		this.hp = hp;
		this.maxHp = hp;
		this.hidden = true;
	}

	public void print()
	{
		System.out.println("name: " + this.name + "\n  faction:" + this.faction.name + ",\n  x: " + this.x + ",\n  y: " + this.y + ",\n  min: " + this.outputMin + ",\n  max: " + this.outputMax + ",\n  hp: " + this.hp +"\n");
	}

	public void action()
	{
		if (this.faction.name == "United Empires")
			this.hidden = false;

	}

}

class fileRead
{
	/************ FILE READING ************/
	public static void read()
	{
		boolean headers = true;
		try
		{
			FileInputStream in = new FileInputStream("resources.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
 
			while((strLine = br.readLine())!= null)
			{
				if (headers) {
					headers = false;
					continue;
				}
   				if (strLine.length() > 1) {
	   				System.out.println("Parsing: " + strLine);
	   				parseLine(strLine);
   				}
  			}
 
		} catch (Exception e){
			System.out.println(e);
		}
	}

	public static void parseLine(String line)
	{
		String name;
		Faction faction;
		int x;
		int y;
		int min;
		int max;
		int hp;

		Scanner lScanner = new Scanner(line);
		lScanner.useDelimiter(",");
		name = lScanner.next();
		faction = spacegame.factions.get(random.randInt(0,spacegame.factions.size()-1));
		x = random.randInt(1, spacegame.map.length);
		y = random.randInt(1, spacegame.map[0].length);
		while (spacegame.map[x-1][y-1]!=null)
		{
			x = random.randInt(1, spacegame.map.length);
			y = random.randInt(1, spacegame.map[0].length);
		}
		min = lScanner.nextInt();
		max = lScanner.nextInt();
		hp = lScanner.nextInt();

		Location tempLoc = new Location(name, faction, x, y, min, max, hp);
		spacegame.fillMap(tempLoc);
		//tempLoc.print();
	}

}

class Faction
{
	String name;
	int attackCool;
	int[] resources;
	Location target = null;
	ArrayList<TradeRoute> traderoutes = new ArrayList<TradeRoute>();

	Faction(String name) {
		this.name = name;
		this.attackCool = random.randInt(5,20);
		this.resources = new int[] { 200, 100, 0, 1000 };
		// Metal, Ships, Tech, Credits
	}

	public void print()
	{
		System.out.println("Faction - " + this.name + "\n   Metal: " + this.resources[0] + "\n   Ships: " + this.resources[1] + "\n   Tech: " + this.resources[2] + "\n   Credits: " + this.resources[3]);
	}

	public void action()
	{
		// Resources
		if (spacegame.turn % 10 == 0) {
			int[] tempResources = {this.resources[0], this.resources[1], this.resources[2], this.resources[3]};
			resourceGain();
			if (this.name=="United Empires") {
				System.out.println("===== RESOURCES GAINED THIS TURN =====");
				System.out.println("Metal:\t " + this.resources[0] + "\t(" + (this.resources[0] - tempResources[0]) + ")" + "\nShips:\t " + this.resources[1] + "\t(" + (this.resources[1] - tempResources[1]) + ")" + "\nTech:\t " + this.resources[2] + "\t(" + (this.resources[2] - tempResources[2]) + ")" + "\nCredits: " + this.resources[3] + "\t(" + (this.resources[3] - tempResources[3]) + ")");
			}
		}
		// Attacking
		if (this.name != "United Empires") {
			if (this.attackCool < 6) {
				if (this.target==null || this.target.faction == this) {
					this.target = this.randLoc();
					System.out.println("Our data shows that " + this.name + " is planning to attack a " + this.target.faction.name + " " + this.target.name + " soon.");
					spacegame.attackLog.add("   " + this.name + " attacking " + this.target.faction.name + " " + this.target.name);
				}
				if (this.attackCool < 1) {
					this.attackCool = random.randInt(10,30);
					this.attack(this.target);
				}
			}
			
			this.attackCool--;
			
		}
	}

	public void resourceGain()
	{
		for (int i=0; i<this.getLocs().size(); i++) {
			int temp;
			switch (this.getLocs().get(i).name) {
				case "mine":
						this.resources[0]+=random.randInt(this.getLocs().get(i).outputMin, this.getLocs().get(i).outputMax);
					break;
				case "fighter base":
						for (int j=0; j<random.randInt(this.getLocs().get(i).outputMin, this.getLocs().get(i).outputMax); j++) {
							if (this.resources[0]>=10) {
								this.resources[1]++;
								this.resources[0]-=10;
							}
						}
					break;
				case "research facility":
						for (int j=0; j<random.randInt(this.getLocs().get(i).outputMin, this.getLocs().get(i).outputMax); j++) {
							if (this.resources[3]>=100) {
								this.resources[2]++;
								this.resources[3]-=100;
							}
						}
					break;
				case "colonized planet":
						this.resources[3]+=random.randInt(this.getLocs().get(i).outputMin, this.getLocs().get(i).outputMax);
					break;
				default:
						System.out.println("LOCATION NAME NOT YET DEFINED");
					break;
			}
		}

	}

	public Location randLoc()
	{
		ArrayList<Location> attackOpts = new ArrayList<Location>();

		Location targetLoc;

		for (int i=0; i<spacegame.map.length; i++) {
			for (int j=0; j<spacegame.map[0].length; j++) {
				if (spacegame.map[i][j].faction.name != "none" && spacegame.map[i][j].faction.name != this.name) {
					attackOpts.add(spacegame.map[i][j]);
				}
			}
		}

		targetLoc = attackOpts.get(random.randInt(0, (attackOpts.size()-1)));

		return targetLoc;
	}

	public boolean attack(Location targetLoc)
	{
		// Pass params to attack()
		spacegame.attack(this, targetLoc, (targetLoc.hp/7));

		// Broadcast attack outcome
		if (targetLoc.faction == this) {
			System.out.print("The " + this.name + " have captured a " + targetLoc.name);
			if (!targetLoc.hidden) {
				System.out.println(" at " + targetLoc.x + ", " + targetLoc.y);
			} else {
				System.out.print("\n");
			}
			return true;
		} else {
			System.out.println("The " + this.name + " unsuccessfully attacked a " + targetLoc.faction.name + " " + targetLoc.name + " (" + targetLoc.hp + "/" + targetLoc.maxHp + ").");
			return false;
		}

	}

	// Returns every location of specified faction
	public ArrayList<Location> getLocs()
	{
		ArrayList<Location> factionLocs = new ArrayList<Location>();

		for (int i=0; i<spacegame.map.length; i++) {
			for (int j=0; j<spacegame.map[0].length; j++) {
				if (spacegame.map[i][j].faction == this)
					factionLocs.add(spacegame.map[i][j]);
			}
		}

		return factionLocs;

	}

	// Trading
	public TradeRoute createTrade(int trading, int receiving, Faction fac2)
	{
		TradeRoute traderoute = new TradeRoute(this, fac2, trading, receiving, (this.resources[trading-1]/2), (fac2.resources[receiving-1]/2)); // Change to divide by number of trade routes

		this.traderoutes.add(traderoute);

		return traderoute;
	}

}

class TradeRoute {
	Faction fac1;
	Faction fac2;
	int trade1;
	int trade2;
	int amount1; // what fac1 is giving and fac2 is receiving
	int amount2; // what fac2 is giving and fac1 is receiving

	TradeRoute(Faction fac1, Faction fac2, int trade1, int trade2, int amount1, int amount2)
	{
		this.fac1 = fac1;
		this.fac2 = fac2;
		this.trade1 = trade1;
		this.trade2 = trade2;
		this.amount1 = amount1;
		this.amount2 = amount2;
	}

	public void trade()
	{
		fac1.resources[trade1-1] -= amount1;
		fac2.resources[trade1-1] += amount1;

		fac2.resources[trade2-1] -= amount2;
		fac1.resources[trade2-1] += amount2;

		System.out.println(fac1.name + " gave " + amount1 + " " + (trade1-1) + " to " + fac2.name + " in exchange for " + amount2 + " " + (trade2-1));

		// Reset trading values after giving items
		amount1 = (fac1.resources[trade1-1]/2);
		amount2 = (fac2.resources[trade2-1]/2);
	}
}

class spacegame
{
	// Map array
	public static Location[][] map = new Location[9][9];

	// Turn counter
	public static int turn = 0;

	// Factions
	public static String factionlist[] = {
		"United Empires",
		"Enemy",
		"Maxis",
		"Humans",
		"Androids"
	};
	public static ArrayList<Faction> factions = new ArrayList<Faction>();

	// Attack log
	public static ArrayList<String> attackLog = new ArrayList<String>();

	public static void main(String[] args)
	{
		// Faction init
		for (int i=0; i<factionlist.length; i++) {
			factions.add(new Faction(factionlist[i]));
		}

		// Load map
		for (int i=0; i<8; i++) {
			fileRead.read();
		}
		

		System.out.println(map.length + ", " + map[0].length);

		// Fill with empty space locations
		for (int i=0; i<map.length; i++) {
			for (int j=0; j<map[0].length; j++) {
				if (map[i][j]!=null) {
					map[i][j].print();
				}
				else {
					map[i][j] = new Location("Empty space",new Faction("none"),i+1,j+1,0,0,0);
					map[i][j].hidden = true;
				}
			}
		}

		showMap(true);

		// Game
		gametick();

		// For submenus
		ArrayList<Integer> tempIntArr = new ArrayList<Integer>();

		// Game loop
		Scanner in = new Scanner(System.in);
		String uInput = in.next();		
		while (true)
		{
			// Increment turn
			turn++;
			
			switch (uInput.toUpperCase()) {
				case "HELP":
					System.out.println("---HELP MENU---");
					System.out.println("Input is accepted in upper and lower case.");
					System.out.println(" R or RESOURCES will show you your resources.");
					System.out.println(" ATTACK prompts attack sub menu\n     X,Y,N will attack galactic coordinates X,Y with N ships.");
					System.out.println(" LIST lists all known locations and their factions.");
					System.out.println(" RSCAN prompts the radar scan menu\n     X,Y of one of your locations will scan nearby occupied locations.");
					System.out.println(" RSCANALL scans all occupied locations nearby your locations.");					
					System.out.println(" MAP refreshes the galactic map.");
					System.out.println(" FMAP shows the faction map.");
					System.out.println(" TRADE prompts the trade route sub menu.");
					System.out.println(" C clears the screen");
					System.out.println(" TURN displays the current turn.");
					System.out.println(" LOGS shows attack logs.");
					break;
				case "R":
				case "RESOURCES":
					factions.get(0).print();
					break;
				// VIEW - remove this later (shows deatils about any X/Y)
				case "VIEW":
					tempIntArr = takeInputs(2, "X:,Y:");
					map[tempIntArr.get(0)-1][tempIntArr.get(1)-1].print();
					break;
				case "ATTACK":
						System.out.println("Enter X and Y coordinate to attack with N ships, X,Y,N");

						tempIntArr = takeInputs(3, "X coordinate to attack:,Y coordinate to attack:,Number of ships to attack with:");


						if (map[tempIntArr.get(0)-1][tempIntArr.get(1)-1].name!="Empty space" || map[tempIntArr.get(0)-1][tempIntArr.get(1)-1].hidden==false) {
							System.out.println("Attacking " + tempIntArr.get(0) + ", " + tempIntArr.get(1) + " ( " + spacegame.map[tempIntArr.get(0)-1][tempIntArr.get(1)-1].name + ", " + map[tempIntArr.get(0)-1][tempIntArr.get(1)-1].faction.name + " ) " + " with " + tempIntArr.get(2) + " ships.");


							ArrayList<String> outputs = attack(factions.get(0), map[tempIntArr.get(0)-1][tempIntArr.get(1)-1], tempIntArr.get(2));
							for (int i=0; i<outputs.size(); i++) {
								System.out.println(outputs.get(i));
							}

						} else {
							System.out.println("There is nothing here to attack according to our data.");
						}
					break;
				case "LIST":
					list();
					break;
				case "RSCAN":
					tempIntArr = takeInputs(2, "X coordinate to scan:,Y coordinate to scan:");
					rscan(tempIntArr.get(0), tempIntArr.get(1));
					break;
				case "RSCANALL":
					for (int i=0; i<factions.get(0).getLocs().size(); i++) {
						rscan(factions.get(0).getLocs().get(i).x, factions.get(0).getLocs().get(i).y);
					}
					System.out.print("\n");	
					break;
				case "MAP":
					showMap(false);
					break;
				case "FMAP":
					showMap(true);
					break;
				case "TRADE":
					tempIntArr = takeInputs(2, "What resource do you need?:\n1. Metal\n2. Ships\n3. Tech\n4. Credits,What resource can you trade?:\n1. Metal\n2. Ships\n3. Tech\n4. Credits");
					System.out.println("Which faction would you like to trade with?\nFaction - Amount giving - Amount receiving");

					ArrayList<Faction> tempFacArr = new ArrayList<Faction>();
					for (int i=0; i<factions.size(); i++) {
						if (factions.get(i).resources[tempIntArr.get(0)-1] > factions.get(0).resources[tempIntArr.get(0)-1] && factions.get(i).resources[tempIntArr.get(1)-1] < factions.get(0).resources[tempIntArr.get(1)-1])							
							tempFacArr.add(factions.get(i));
					}

					String facOpts = "";
					for (int i=0; i<tempFacArr.size(); i++) {
						facOpts+=((i+1) + ". " + tempFacArr.get(i).name + "\n");
					}

					factions.get(0).createTrade(tempIntArr.get(1), tempIntArr.get(0), tempFacArr.get(((takeInputs(1, facOpts)).get(0))-1));
					
					break;
				case "C":
					// Clear map
					for (int i=0; i<64; i++) {
						System.out.println("\n");
					}
					showMap(false);
					break;
				case "TURN":
					System.out.println(turn);
					break;
				// DEV - SHOWS ATTACK COOL DOWNS
				case "COOLS":
					for (int i=0; i<factions.size(); i++) {
						System.out.println(factions.get(i).name + " : " + factions.get(i).attackCool);
					}
					break;
				// DEV - SHOWS ALL FACTION LOCATIONS
				case "FACLOC":
					for (int i=0; i<factions.size(); i++) {
						for (int j=0; j<factions.get(i).getLocs().size(); j++) {
							System.out.println(factions.get(i).name + " : " + factions.get(i).getLocs().get(j).name + " - hp: " + factions.get(i).getLocs().get(j).hp);
						}
						System.out.print("\n");	
					}
					break;
				// DEV - SHOWS ALL FACTION RESOURCES
				case "FACRES":
					for (int i=0; i<factions.size(); i++) {
						factions.get(i).print();
					}
					break;
				case "LOGS":
					// list attack messages
					for (int i=0; i<attackLog.size(); i++) {
						System.out.println(attackLog.get(i));
					}
					System.out.print("\n");
					break;
				default:
					System.out.println("INPUT ERROR\n");
					break;
			}

			gametick();


			uInput = in.next();

			
		}

	}

	// takeInputs command for submenus
	public static ArrayList<Integer> takeInputs(int numInputs, String message) {
		// Messages for each input query
		ArrayList<String> prompts = new ArrayList<String>();

		// ArrayList to return
		ArrayList<Integer> inputs = new ArrayList<Integer>();

		// Keyboard input scanner
		Scanner in = new Scanner(System.in);

		// Message parameter parser
		Scanner lScanner = new Scanner(message);
		lScanner.useDelimiter(",");
		for (int i=0; i<numInputs; i++) {
			prompts.add(lScanner.next());
		}

		for (int i=0; i<prompts.size(); i++) {
			System.out.println(prompts.get(i));
			inputs.add(in.nextInt());
		}

		return inputs;
		
	}

	// List known faction locations
	public static void list() {
		System.out.println("");
		for (int i=0; i<map.length; i++) {
			for (int j=0; j<map[0].length; j++) {
				if (!map[i][j].hidden) {
					System.out.println(map[i][j].name + ", " + map[i][j].x + ", " + map[i][j].y + ", " +  map[i][j].faction.name + ".");
				}
			}
		}
		System.out.println("");
	}

	// Runs action command every 10 turns
	public static void gametick() {
		for (int i=0; i<map.length; i++) {
			for (int j=0; j<map[0].length; j++) {
				if (map[i][j].faction.name!="none") {
					map[i][j].action();
				}

			}
		}
		for (int i=0; i<factions.size(); i++) {
			factions.get(i).action();
			for (int j=0; j<factions.get(i).traderoutes.size(); j++) {
				if (turn % 10 == 0)
					factions.get(i).traderoutes.get(j).trade();
			}
		}
	}

	// Draws the map onto the screen - true = factions, false = resources
	public static void showMap(boolean factions) {
		// Show galaxy map
		//System.out.println("\n");
		for (int j=0; j<map[0].length+1; j++) {
			System.out.print(j+"| ");
		}
		System.out.println("\n");
		for (int i=0; i<map.length; i++) {
			System.out.print(i+1 + "|");

			for (int j=0; j<map[0].length; j++) {

				if (map[i][j]!=null && !map[i][j].hidden) {
					if (factions)
						System.out.print(Character.toUpperCase(map[i][j].faction.name.charAt(0)));
					else
						System.out.print(Character.toUpperCase(map[i][j].name.charAt(0)));
				}
				else
					System.out.print(" ");
				System.out.print(" |");
			}
			System.out.println("\n");
		}
	}

	// RSCAN - scans 3x3 around player location X/Y
	public static void rscan(int x, int y) {
		boolean foundLoc = false;
		if (map[x-1][y-1].faction.name=="United Empires") {
			for (int i=(x-2); i<(x+1); i++) {
				for (int j=(y-2); j<(y+1); j++) {

					if (i>0 && j>0 && i<map.length && j<map[0].length) {
						if (map[i][j].name!="Empty space") {
							map[i][j].print();
							map[i][j].hidden = false;
							if (!foundLoc) {
								foundLoc = true;
							}
						}
					}
				}
			}
			if (!foundLoc) {
				System.out.println("No locations near these coordinates.");
			}
		} else {
			System.out.println("You have no facilities at " + x + ", " + y + " that you can scan the area with.");
		}
		
	}

	// ATTACK METHOD
	public static ArrayList<String> attack(Faction attacker, Location target, int ships) {
	
		ArrayList<String> outputs = new ArrayList<String>();

		// Max out ships integer
		if (ships > attacker.resources[1]) {
			outputs.add("Only " + attacker.resources[1] + " available out of the " + ships + " requested.");
			ships = attacker.resources[1];
		}

		// Calculations
		attacker.resources[1] -= ships;

		int tempHp = target.hp;
		for (int i=0; i<ships; i++) {
			target.hp -= random.randInt(5,10);
		}
		outputs.add("Did " + (tempHp - target.hp) + " damage to target!");
		if (target.hp < 0) {
			target.hp = 0;
			outputs.add(attacker.name + " has successfully taken over the " + target.faction.name + " " + target.name + " at " + target.x + ", " + target.y);
			target.faction = attacker;
			target.hp = target.maxHp;
		} else {
			outputs.add("Target has " + target.hp + " health left.");
		}

		return outputs;
	}

	public static void fillMap(Location loc)
	{
		map[loc.x - 1][loc.y - 1] = loc;
	}


}
