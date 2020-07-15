package main.components;
import java.util.*;


public class Attribute extends Component{
	ArrayList<Tag> tags = new ArrayList<Tag>();
	
	public Attribute(String name) {
		super(name);
	}
	
	public ArrayList<Tag> getPartners(){
		return tags;
	}
	
	public String detailedOutput() {
		return super.detailedOutput() + "Tags: " + tags + "\n";
	}

	public void addTag(Tag t) {
		String name = t.getName();
		boolean added = false;
		int size = tags.size();
		
		
		for(int i = 0; i<size; i ++) {
			if(name.compareToIgnoreCase(((Component)tags.get(i)).getName())<0) {
				tags.add(i, t);
				added = true;
				break;
			}
		}
		
		if(!added) {
			tags.add(t);
		}		
	}
}
