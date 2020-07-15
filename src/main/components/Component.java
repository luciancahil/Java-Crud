package main.components;


public class Component implements Cloneable{
	String name;
	String description;
	
	public Component(String name) {
		this.name = name;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public String detailedOutput() {
		return "#" + name + ": " + description + ";\n";
	}
	
	public boolean equals(Object o) {
		if(((Component) o).getName().contentEquals(name))
			return true;
		
		return false;
	}
	
	public String getDescription() 							{return description;}
	public String getName() 								{return name;}
	public String toString() 								{return name;}
	public void setDescription(String description) 			{this.description = description;}
}
