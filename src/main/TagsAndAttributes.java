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
				case "combine":		combine(input); break;
				case "done": 		isDone = isDone(); break;
				case "addt": 		addComponent(input,"tag"); break;
				case "adda": 		addComponent(input, "attribute"); break;	
				case "addtd":		addComponentDescription(input,"tag"); break;
				case "addad":		addComponentDescription(input, "attribute");break;
				default: 			System.out.println("I do not recognize that command");
			}
		}
		input.close();
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
	
	private static void addComponent(Scanner input, String type) {
		String name = "";
		Component component;
		boolean invalid = true;
		
		while(invalid) {
			System.out.println("What " + type + " would you like to add?");
			name = input.nextLine();
			invalid = isInvalid(name);
			
			if(name.toUpperCase().equals("NONE")) {
				try {
					return;
				}catch(Exception e) {}
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
		
		findOrder(component,type);
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
		
		for(int i = 0; i<size; i ++) {
			if(name.compareToIgnoreCase(((Component)list.get(i)).getName())<0) {
				list.add(i, c);
				added = true;
				break;
			}
		}
		
		if(!added) {
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
	
	private static boolean isDone() { //checks to see if there are any null descriptions.
		ArrayList<String> nullDescription = new ArrayList<String>();
		boolean isDone = true;
		
		for(Attribute attribute: attributes) {
			if(attribute.getDescription() == null) {
				nullDescription.add(attribute.getName());
				isDone = false;
			}
		}
		
		for(Tag tag: tags) {
			if(tag.getDescription() == null) {
				nullDescription.add(tag.getName());
				isDone = false;
			}
		}
		
		if(!isDone) {
			System.out.println("ERROR!");
			System.out.println(nullDescription + " has no description!");
		}
		
		return isDone;
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

}
