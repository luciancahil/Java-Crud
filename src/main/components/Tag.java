package main.components;
import java.util.*;


public class Tag extends Component {
	ArrayList<Attribute> attributes = new ArrayList<Attribute>();
	
	public Tag(String name) {
		super(name);
	}
	
	public boolean contains(Attribute a) {
		if(attributes.indexOf(a) != -1) {
			return true;
		}
		
		return false;
	}
	
	public void addAttribute(Attribute a) {
		String name = a.getName();
		boolean added = false;
		int size = attributes.size();
		
		
		for(int i = 0; i<size; i ++) {
			if(name.compareToIgnoreCase(((Component)attributes.get(i)).getName())<0) {
				attributes.add(i, a);
				added = true;
				break;
			}
		}
		
		if(!added) {
			attributes.add(a);
		}		
	}
	
	public String detailedOutput() {
		return super.detailedOutput() + "Attributes: " + attributes + "\n";
	}
}
