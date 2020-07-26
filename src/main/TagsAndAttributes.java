package main;
import main.components.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class TagsAndAttributes {
	private static ArrayList<Tag> tags = new ArrayList<Tag>();
	private static ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	private static Connection conn = null;
	private static Statement stmt = null;
	
	public static void main(String[] args) throws IOException {
		try {
			loginSQL();
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readXML();
		inputs();
		writeXML();
	}
	
	private static void inputs(){
		Scanner input = new Scanner(System.in);
		boolean isDone = false;
		String nextAction;
		
		while(!isDone) {
			System.out.println("What would you like to do?");
			nextAction = input.nextLine().toLowerCase();
			
			switch(nextAction) {
				case "adda": 		addComponent(input, "attribute"); break;	
				case "addt": 		addComponent(input,"tag"); break;
				case "changead":	addComponentDescription(input, "attribute");break;
				case "changetd":	addComponentDescription(input,"tag"); break;
				case "combine":		combine(input); break;		
				case "divorce":		divorce(input);break;
				case "deletea":		delete(input, "attribute");break;
				case "deletet":		delete(input, "tag");break;
				case "showall":		showAll();break;
				case "showa":		show(input, "attribute");break;
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
	

	@SuppressWarnings("unchecked")
	private static void delete(Scanner input, String type) {
		String name;
		Component c;
		ArrayList<Component> partners;
		
		System.out.println("Which " + type + " would you like to delete?");
		name = input.nextLine();
		
		c = getComponent(name, type);
		
		if(c == null) {
			System.out.println(name + " is not a valid " + type + ".");
			return;
		}
		
		
		//remove the tag from it's partners lists
		partners = c.getPartners();
		
		for(Component partner: partners) {
			partner.getPartners().remove(c);
		}
		
		if(type.equals("tag"))
			tags.remove(c);
		else
			attributes.remove(c);
		
		System.out.println(name + " has been removed.");
	}
	
	private static void show(Scanner input, String type) {
		Component c;
		String name;
		
		System.out.println("Which " + type + " would you like to show?");
		name = input.nextLine();
		
		c = getComponent(name, type);
		
		if(c == null) {
			System.out.println(name + " is not a valid " + type + ".");
			return;
		}
		
		System.out.println(c.detailedOutput());
		
		
	}
	
	private static void showAll() {
		System.out.println("Tags: " + tags);
		System.out.println("Attributes " + attributes);
	}

	private static void combine(Scanner input) {//adds a tag and an attribute to each other
		String fullText;
		int colonPlace;
		String tagString;
		String attributeString;
		Tag tag;
		Attribute attribute;
		boolean isDone = false;
		
		while(!isDone) {
			System.out.println("What would you like to combine?");
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
			tag = (Tag)getComponent(tagString,"tag");
			attribute = (Attribute)getComponent(attributeString,"attribute");
			
			if(tag==null) {
				System.out.println("Tag not found");
				continue;
			}else if(attribute == null) {
				System.out.println("Attribute not found");
				continue;
			}else if(tag.contains(attribute)) {
				System.out.println(tag + " already contains " + attribute);
				continue;
			}
			
			tag.addAttribute(attribute);
			attribute.addTag(tag);
			isDone = true;
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
	
	private static void addComponent(Scanner input, String type) {
		String name = null, description = null;
		Component component;
		boolean invalid = true;
		int colonPlace;
		String fullText;
		
		while(invalid) {
			System.out.println("What " + type + " would you like to add?");
			fullText = input.nextLine();
			colonPlace = fullText.indexOf(':');
			
			if(colonPlace == -1) {
				System.out.println("Please use the format [tagName]:[description] (no spaces around colon)\n");
				continue;
			}
			
			name = fullText.substring(0, colonPlace);
			description = fullText.substring(colonPlace + 1);
			
			invalid = isInvalid(name);
			invalid = isInvalid(description);
			
			if(name.toUpperCase().equals("NONE")) {
				return;
			}
			
		}
		
		if(type.equals("tag")) {
			component = new Tag(name);
		}else {
			component = new Attribute(name);
		}
		
		if(tags.contains(component)||attributes.contains(component)) {
			System.out.println(type + " " + name + " already exists");
			try {
				return;
			}catch(Exception e) {}
		}
		
		component.setDescription(description);
		
		findOrder(component, type);
	}

	private static void addComponentDescription(Scanner input,String type) {
		String fullText;
		String componentName;
		String description;
		boolean isDone = false;
		Component component;
		
		while(!isDone) {
			System.out.println("What " + type + " description do you wish to add?");
			fullText = input.nextLine();
			int colonPlace = fullText.indexOf(':');
			
			if(fullText.equals("exit")) {
				break;
			}
			
			if(colonPlace == -1) {
				System.out.println("Please use the format [tagName]:[description] (no spaces around colon)\n");
				continue;
			}
			
			componentName = fullText.substring(0, colonPlace);
			description = fullText.substring(colonPlace+1);
			component = getComponent(componentName,type);	
			
			if(isInvalid(description)) {
				continue;
			}
			
			if(component == null) {
				System.out.println(type + " not found");
				continue;
			}
			
			component.setDescription(description);
			isDone = true;
		}
	}
	
	
	

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void findOrder(Component c, String type) {
		String name = c.getName();
		boolean added = false;
		
		ArrayList list;
		if(type.equals("tag")){
			list = tags;
		}else {
			list = attributes;
		}
		int size = list.size();
		
		for(int i = 0; i<size; i ++) {//adds to the list in alphabetical order
			if(name.compareToIgnoreCase(((Component)list.get(i)).getName())<0) {
				list.add(i, c);
				added = true;
				break;
			}
		}
		
		if(!added) {			//adds to the end of the list if it is the last alphabetically
			list.add(c);
		}		
	}
	
	private static boolean isInvalid(String check) {
		String[] illegals = {"#", ":", ";", "[", "]", ","};
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
        
        if(conn != null) {
        	System.out.println("Connected!");
        }
        
        stmt = conn.createStatement();
        System.out.println();
        test();
        
        passR.close();
	}
	
	private static void test() {
		String s = "DROP TABLE dummy";
		try {
			stmt.executeUpdate(s);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
