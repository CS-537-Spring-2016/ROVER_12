package common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Coord {
	// thanks to this posting http://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java
	
	public final int xpos;
	public final int ypos;
	
	@Override
	public String toString() {
		return "Coord [xpos=" + xpos + ", ypos=" + ypos + "]";
	}

	public Coord(int x, int y){
		this.xpos = x;
		this.ypos = y;
	}
	
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(xpos).
            append(ypos).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
       if (!(obj instanceof Coord))
            return false;
        if (obj == this)
            return true;
        Coord theOther = (Coord) obj;
        return ((this.xpos == theOther.xpos) && (this.ypos == theOther.ypos));
    }
	
	public Coord clone(){
		return new Coord(xpos, ypos);
	}

	public int getXpos() {
		return this.xpos;
	}

	public int getYpos() {
		return this.ypos;
	}
	
}