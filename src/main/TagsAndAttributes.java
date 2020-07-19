package main;
import main.components.*;
import java.io.*;
import java.util.*;

public class TagsAndAttributes {
	static ArrayList<Tag> tags = new ArrayList<Tag>();
	static ArrayList<Attribute> attributes = new ArrayList<Attribute>();

	public static void main(String[] args) throws IOException {
		readfile();
		inputs();
		writeOutput();
	}
	
	private static void inputs(){
		Scanner input = new Scanner(System.in);
		boolean isDone = false;
		String nextAction;
		
		while(!isDone) {
			System.out.println("What would you like to do?");
			nextAction = input.nextLine().toLowerCase();
			
			switch(nextAction) {
				case "deletea":		delete(input, "attribute");break;
				case "deletet":		delete(input, "tag");break;
				case "showa":		show(input, "attribute");break;
				case "showt":		show(input, "tag");break;
				case "showall":		showAll();break;
				case "combine":		combine(input); break;
				case "addt": 		addComponent(input,"tag"); break;
				case "divorce":		divorce(input);break;
				case "adda": 		addComponent(input, "attribute"); break;	
				case "addtd":		addComponentDescription(input,"tag"); break;
				case "addad":		addComponentDescription(input, "attribute");break;
				case "done": 		isDone = true; break;
				default: 			System.out.println("I do not recognize that command");
			}
		}
		input.close();
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
	
	private static void readfile() throws IOException {
		FileReader frTag = new FileReader("Tags.txt");
		FileReader frAtt = new FileReader("Attributes.txt");
		BufferedReader tags = new BufferedReader(frTag);
		BufferedReader atts = new BufferedReader(frAtt);

		String attributeString = "";
		String tagString = "";
		String aLine = "";
		String tLine = "";
		
		
		while((aLine = atts.readLine()) != null) {
			attributeString += aLine;
		}
		seperateComponent(attributeString,"attribute");
		
		while((tLine = tags.readLine()) != null) {
			tagString += tLine;
		}
		seperateComponent(tagString,"tag");
		
		tags.close();
		atts.close();
	}

	private static void seperateComponent(String component,String type) {//seperates teh text file into individual attributes
		int hashtagIndex = component.indexOf('#');
		int nextHashtagIndex = component.indexOf('#', hashtagIndex+1);
		ArrayList<String> comps = new ArrayList<String>();

		while(nextHashtagIndex != -1) {
			comps.add(component.substring(hashtagIndex,nextHashtagIndex));
			hashtagIndex = nextHashtagIndex;
			nextHashtagIndex = component.indexOf('#', hashtagIndex+1);
		}
		
		comps.add(component.substring(hashtagIndex));
		
		addComp(comps,type);
				
		if(type.equals("tag"))
			addPartners(comps);
	}



	private static void addPartners(ArrayList<String> comps) {
		for(String comp: comps) {
			int colonIndex = comp.indexOf(':');
			int openBracketIndex = comp.indexOf('[');
			int closeBracketIndex = comp.indexOf(']');
			String name = comp.substring(1,colonIndex);
			String partners = comp.substring(openBracketIndex+1,closeBracketIndex);
			int commaIndex = partners.indexOf(", ");
			Tag t = (Tag)getComponent(name, "tag");
			Attribute partner = new Attribute("test"); 
			int start = 0;
			
			if(partners.contentEquals("")) {//nothing in the arraylists.
				continue;
			}
			
			if(commaIndex == -1) {//there is only one partner
				partner = (Attribute)getComponent(partners,"attribute");
				t.addAttribute(partner);
				partner.addTag(t);
				continue;
			}
			
			while(commaIndex != -1) {
				partner = (Attribute) getComponent(partners.substring(start, commaIndex),"attribute");
				t.addAttribute(partner);
				partner.addTag(t);
				start = commaIndex+2;
				commaIndex = partners.indexOf(',', commaIndex+1);
			}

			partner = (Attribute) getComponent(partners.substring(start),"attribute");
			t.addAttribute(partner);
			partner.addTag(t);
		}
	}


	private static void addComp(ArrayList<String> comps,String type) {//adds the components into the arraylist
		for(String comp: comps) {
			int colonIndex = comp.indexOf(':');
			int semicolonIndex = comp.indexOf(';');
			String componentName = comp.substring(1,colonIndex);
			String description = comp.substring(colonIndex+2,semicolonIndex);
			
			if(type.contentEquals("tag")) {
				Tag t = new Tag(componentName);
				t.setDescription(description);
				tags.add(t);
			}else {
				Attribute a = new Attribute(componentName);
				a.setDescription(description);
				attributes.add(a);
			}
		}
	}
	
	private static void writeOutput() throws IOException{
		System.out.println(tags);
		System.out.println(attributes);
		writeXML();
		FileWriter fwTag = new FileWriter("Tags.txt");
		FileWriter fwAttribute = new FileWriter("Attributes.txt");
		BufferedWriter tagFile = new BufferedWriter(fwTag);
		BufferedWriter attributeFile = new BufferedWriter(fwAttribute);
		
		
		for(Tag tag: tags) {
			String outputThis = tag.detailedOutput();
			tagFile.write(outputThis + "\n");
		}
		
		for(Attribute attribute: attributes) {
			String outputThis = attribute.detailedOutput();
			attributeFile.write(outputThis + "\n");
		}
		attributeFile.close();
		tagFile.close();
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
}
