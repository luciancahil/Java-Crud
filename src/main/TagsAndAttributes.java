package main;
import main.components.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.*;


public class TagsAndAttributes {
	private static ArrayList<Tag> tags = new ArrayList<Tag>();
	private static ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	private static Connection conn = null;
	private static Statement stmt = null;
	
	public static void main(String[] args) throws IOException, SQLException {
		
		loginSQL();	
		readXML();
		inputs();
		writeXML();
	}
	
	private static void inputs() throws SQLException{
		Scanner input = new Scanner(System.in);
		boolean isDone = false;
		String nextAction;
		
		while(!isDone) {
			System.out.println("What would you like to do?");
			nextAction = input.nextLine().toLowerCase();
			
			switch(nextAction) {
				case "adda": 		addComponent(input, "att"); break;	
				case "addt": 		addComponent(input,"tag"); break;
				case "changead":	changeComponentDescription(input, "att");break;
				case "changetd":	changeComponentDescription(input,"tag"); break;
				case "combine":		combine(input); break;		
				case "divorce":		divorce(input);break;
				case "deletea":		delete(input, "att");break;
				case "deletet":		delete(input, "tag");break;
				case "showall":		showAll();break;
				case "showa":		show(input, "att");break;
				case "showt":		show(input, "tag");break;
				case "showc":		showCommands();break;
				case "done": 		isDone = true; break;
				default: 			
								System.out.println("I do not recognize that command");
								System.out.println("Enter \"showc\" for a list of commands");
			}
		}
		input.close();
	}

	private static void showCommands() {
		System.out.println("Commands:\n");
		System.out.println("adda: \t\t Add an Attribute");
		System.out.println("addt: \t\t Add a tag");
		System.out.println("changead: \t change an attribute's description");
		System.out.println("changetd: \t change a tag's description");
		System.out.println("combine: \t combine a tag and an attribute");
		System.out.println("divorce: \t seperate a connection between a tag and attribute");
		System.out.println("deletea: \t deletes an attribute");
		System.out.println("deletet: \t deletes a tag");
		System.out.println("showall: \t shows a list of all tags and attributes");
		System.out.println("showa: \t\t shows a single attribute in detail");
		System.out.println("showt: \t\t show a single tag in detail");
		System.out.println("showc: \t\t shows a list of commands");
		System.out.println("done: \t\t ends the program");
		System.out.println("\n");
	}
	

	private static void delete(Scanner input, String type) throws SQLException {
		String name;
		boolean valid = false;
		String sql = "";
		ResultSet rs;
		char confirmation;
		
		while(!valid) {
			System.out.println("Which " + type + " would you like to delete?");
			name = input.nextLine();
			
			if(name.toUpperCase().equals("NONE"))
				return;
			
			if(isInvalid(name))
				continue;
			
			sql = "SELECT COUNT(*) FROM " + type + "s WHERE " + type + "_name = '" + name + "'";
			
			rs = stmt.executeQuery(sql);
			rs.next();
			
			
			if(rs.getInt(1) == 0) {
				System.out.println(type + " " + name + "does not exist.");
				continue;
			}
			
			System.out.println("Are you sure you want to delete " + name + "(y/n)?");
			
			confirmation = input.nextLine().charAt(0);
			
			if(confirmation != 'y') {
				continue;
			}
			
			sql = "DELETE FROM " + type + "s WHERE " + type + "_name = '" + name + "'";
			stmt.execute(sql);
			
			
			sql = "DELETE FROM partners WHERE " + type + "_name = '" + name + "'";
			stmt.execute(sql);
		}
	}
	
	private static void show(Scanner input, String type) throws SQLException {
		String name;
		ResultSet rs;
		String sql;
		boolean valid = false;
		String output = "";
		String partnerType;
		
		if(type.equals("tag")) {
			partnerType = "att";
		}else if (type.equals("att")) {
			partnerType = "tag";
		}else{
			throw new IllegalArgumentException();
		}
		
		while(!valid) {
			System.out.println("Which " + type + " would you like to show?");
			name = input.nextLine();
			System.out.println();
			
			
			if(name.toUpperCase().equals("NONE"))
				return;
			
			if(isInvalid(name))
				continue;
			
			
			sql = "SELECT " + type + "_description FROM " + type + "s WHERE " + type + "_name = '" + name + "'";
			rs = stmt.executeQuery(sql);
			
			if(!rs.next()) {
				System.out.println(type + " " + name + " is invalid.");
				continue;
			}
			
			output += name + ": " + rs.getString(1) + "\n";
			
			sql = "SELECT " + partnerType + "_name FROM partners WHERE " + type + "_name = '" + name + "'";
			rs = stmt.executeQuery(sql);
			
			if(!rs.next())
				break;
			
			if(partnerType.equals("att")) {
				output += "Attribute Partners: ";
			}else {
				output += "Tag Partners: ";
			}
			
			output += rs.getString(1);
			
			while(rs.next()) {
				output += ", " + rs.getString(1);
			}
			
			valid = true;
		}
		
		System.out.println(output);
		System.out.println();
	}
	
	private static void showAll() throws SQLException {
		String tagSQL = "SELECT tag_name FROM tags";
		String attSQL = "SELECT att_name FROM atts";
		String tags = "[";
		String atts = "[";
		ResultSet rs;
		
		rs = stmt.executeQuery(tagSQL);//get all tag names
		rs.next();							//remove the 0
		tags += rs.getString(1);			//add the first element

		while(rs.next())
			tags += ", " + rs.getString("tag_name");
		
		rs = stmt.executeQuery(attSQL);
		
		rs.next();
		atts += rs.getString(1);
		
		while(rs.next()) {
			atts+= ", " + rs.getString("att_name");
		}
		
		atts += "]";
		tags += "]";
		
		System.out.println(tags);
		System.out.println(atts);
	}

	private static void combine(Scanner input) throws SQLException {//adds a tag and an attribute to each other
		String fullText;
		int colonPlace;
		String tagString;
		String attributeString;
		boolean isDone = false;
		String sql;
		ResultSet rs;
		
		while(!isDone) {
			System.out.println("What would you like to combine? \n(Enter \"exit\" to exit)");
			fullText = input.nextLine();
			if(fullText.equals("exit")) {
				break;
			}
			
			colonPlace = fullText.indexOf(':');
			if(colonPlace == -1) {
				System.out.println("Please Use the Format tag:attribute");
				continue;
			}
			tagString = fullText.substring(0, colonPlace);
			attributeString = fullText.substring(colonPlace+1);
			
			if(isInvalid(tagString) || isInvalid(attributeString)) {
				continue;
			}
			
			sql = "SELECT COUNT(*) FROM tags WHERE tag_name = '" + tagString + "'";	//check tag exists
			rs = stmt.executeQuery(sql);
			rs.next();
			
			if(rs.getInt(1) == 0) {
				System.out.println("Tag " + tagString + " doesn't exist.");
				continue;
			}
			
			sql = "SELECT COUNT(*) FROM atts WHERE att_name = '" + attributeString + "'";	//check tag exists
			rs = stmt.executeQuery(sql);
			rs.next();
			
			if(rs.getInt(1) == 0) {
				System.out.println("Attribute " + attributeString + " doesn't exist.");
				continue;
			}
			
			
			sql= "INSERT INTO partners VALUES ('" + tagString + "', '" + attributeString+"')";
			System.out.println(sql);
			stmt.execute(sql);
		}

		
	}
	
	private static void divorce(Scanner input) {
		Tag t;
		Attribute a;
		String fullText;
		int colonPlace;
		boolean isDone = false;
		
		while(!isDone) {
			System.out.println("What would you like to divorce?");
			fullText = input.nextLine();
			
			if(fullText.equals("exit")) {
				break;
			}
			
			colonPlace = fullText.indexOf(':');
			if(colonPlace == -1) {
				System.out.println("Please Use the Format tag:attribute");
				return;
			}
			t = (Tag)getComponent(fullText.substring(0, colonPlace),"tag");
			a = (Attribute)getComponent(fullText.substring(colonPlace+1),"attribute");
			
			if(t== null) {
				System.out.println(t + " isn't a valid tag.");
			}
			
			if(a == null) {
				System.out.println(a + " isn't a valid tag.");
			}
			
			if(t.getPartners().indexOf(a) == -1){
				System.out.println(t + " isn't matched with " + a + ".");
				continue;
			}

			t.getPartners().remove(a);
			a.getPartners().remove(t);
			
			isDone = true;
		}
		
	}
	
	private static void addComponent(Scanner input, String type) throws SQLException {
		String name = null, description = null;
		boolean invalid = true;
		int colonPlace;
		String fullText;
		String sql;
		
		while(invalid) {
			System.out.println("What " + type + " would you like to add?");
			fullText = input.nextLine();
			colonPlace = fullText.indexOf(':');
			
			if(fullText.toUpperCase().equals("NONE"))
				return;
			
			
			if(colonPlace == -1) {
				System.out.println("Please use the format [tagName]:[description] (no spaces around colon)\n");
				continue;
			}
			
			name = fullText.substring(0, colonPlace);
			description = fullText.substring(colonPlace + 1);
			
			invalid = isInvalid(name);
			invalid = isInvalid(description);
			
			
			sql = "INSERT INTO " + type + "s VALUES('" + name + "', '" + description + "')";
			try {
				stmt.execute(sql);
			}catch (SQLIntegrityConstraintViolationException e) {
				System.out.println(type + " already contained");
				invalid = true;
			}
			
		}
		
		
	}

	private static void changeComponentDescription(Scanner input,String type) throws SQLException {
		String fullText;
		String componentName;
		String description;
		boolean isDone = false;
		
		while(!isDone) {
			System.out.println("What " + type + " description do you wish to change?");
			fullText = input.nextLine();
			int colonPlace = fullText.indexOf(':');
			ResultSet rs;
			
			if(fullText.equals("exit")) {
				break;
			}
			
			if(colonPlace == -1) {
				System.out.println("Please use the format [tagName]:[description] (no spaces around colon)\n");
				continue;
			}
			
			componentName = fullText.substring(0, colonPlace);
			description = fullText.substring(colonPlace+1);
			
			if(isInvalid(description)) {
				continue;
			}
			
			
			//check if the component is found
			
			String sql = "SELECT COUNT(*) FROM " + type + "s WHERE " + type + "_name = '" + componentName + "'";
			rs = stmt.executeQuery(sql);
			rs.next();
			
			if(rs.getInt(1) == 0) {
				System.out.println(type + " not found");
				continue;
			}
			
			sql = "UPDATE " + type + "s SET " + type + "_description = '" + description  + "' WHERE " + type + "_name='" + componentName + "'";
			stmt.execute(sql);
			isDone = true;
		}
	}
	
	private static boolean isInvalid(String check) {
		String[] illegals = {"#", ":", ";", "[", "]", ",", "'"};
		for(String illegal: illegals) {
			if(check.contains(illegal)) {
				System.out.println("ERROR! Cannot contain {\"#\", \":\", \";\", \"[\", \"]\", \",\"}");
				return true;
			}
		}
		
		return false;
	}
	
	private static Component getComponent(String tagName, String type) {
		Component retrieving = new Component(tagName);
		if(type.contentEquals("tag")) {
			int index = tags.indexOf(retrieving);
			try {
				retrieving = tags.get(index);
			}catch(ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}else {
			int index = attributes.indexOf(retrieving);
			try {
				retrieving = attributes.get(index);
			}catch(ArrayIndexOutOfBoundsException e) {
				return null;
			}
		}
		
		return retrieving;
	}
	
	private static void readXML() throws IOException {
		FileReader frTag = new FileReader("Tags.xml");
		FileReader frAtt = new FileReader("Attributes.xml");
		BufferedReader tagBR = new BufferedReader(frTag);
		BufferedReader atts = new BufferedReader(frAtt);
		
		tagBR.readLine();			//removes the top <Tags> 
		atts.readLine();			//removes the top <Attributes>
		
		
		//reading the attributes file
		while(!atts.readLine().equalsIgnoreCase("</Attribues>")) {	//removes <Attributes> tag at top of document
			String nameLine = atts.readLine();
			nameLine = nameLine.substring(nameLine.indexOf('>') + 1, nameLine.lastIndexOf('<'));
			String descLine = atts.readLine();
			descLine = descLine.substring(descLine.indexOf('>') + 1, descLine.lastIndexOf('<'));
			
			Attribute a = new Attribute(nameLine);
			a.setDescription(descLine);
			
			attributes.add(a);
			
			while(!atts.readLine().equals("\t\t</partners>")) {
			}
			atts.readLine();	//</Attribute>
		}

		//reading the tags file and combining partners
		while(!tagBR.readLine().equalsIgnoreCase("</Tags>")) {	//removes <Attribute>
			String nameLine = tagBR.readLine();
			nameLine = nameLine.substring(nameLine.indexOf('>') + 1, nameLine.lastIndexOf('<'));
			String descLine = tagBR.readLine();
			descLine = descLine.substring(descLine.indexOf('>') + 1, descLine.lastIndexOf('<'));
			String partner;
			
			
			Tag t = new Tag(nameLine);
			t.setDescription(descLine);
			
			tagBR.readLine();		//removes the <partner> tag
			while(!(partner = tagBR.readLine()).equals("\t\t</partners>")) {
				partner = partner.substring(partner.indexOf('>') + 1, partner.lastIndexOf('<'));
				Attribute a = (Attribute) getComponent(partner, "at");
				t.addAttribute(a);
				a.addTag(t);
			}
			
			tags.add(t);
			tagBR.readLine();	//</Tag>
		}
		
		tagBR.close();
		atts.close();
	}
	
	private static void writeXML() throws IOException {
		FileWriter fwTag = new FileWriter("Tags.xml");
		FileWriter fwAttribute = new FileWriter("Attributes.xml");
		BufferedWriter tagFile = new BufferedWriter(fwTag);
		BufferedWriter attributeFile = new BufferedWriter(fwAttribute);
		
		tagFile.write("<Tags>\n");
		for(Tag tag: tags) {
			writeXMLComponent(tag, "Tag", tagFile);
		}
		tagFile.write("</Tags>");
		
		attributeFile.write("<Attribues>\n");
		for(Attribute attribute: attributes) {
			writeXMLComponent(attribute, "Attribue", attributeFile);
		}
		attributeFile.write("</Attribues>");
		
		tagFile.close();
		attributeFile.close();
	}
	
	
	
	private static void writeXMLComponent(Component c, String type, BufferedWriter bw) throws IOException {
		bw.write("\t<" + type + ">\n");
		bw.write("\t\t<name>" + c.toString() + "</name>\n");
		bw.write("\t\t<description>" + c.getDescription() + "</description>\n");
		bw.write("\t\t<partners>\n");
		for(Object partner: c.getPartners()) {
			bw.write("\t\t\t<partner>" + partner.toString() + "</partner>\n");
		}
		bw.write("\t\t</partners>\n");
		bw.write("\t</" + type + ">\n");
	}
	
		
	private static void loginSQL() throws IOException, SQLException {
		FileReader pass = new FileReader("Password.txt");
		BufferedReader passR = new BufferedReader(pass);
		
		String url1 = "jdbc:mysql://localhost:3306/tagsandatts";
        String user = "root";
        String password = passR.readLine();
        
        conn = DriverManager.getConnection(url1, user, password);
        stmt = conn.createStatement();        
        
        
        
        passR.close();
	}
}
