/**
 * 
 */
package com.github.sebmartel.eventservice.start;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author seb
 *
 */
public class Event {
	private final Item item;

	@JsonCreator
	public Event(@JsonProperty("item") Item item) {
		this.item = item;
	}
	
	public Event(long timestamp, long id) {
		this.item = new Item(timestamp, id);
	}

	/**
	 * @return the item
	 */
	public Item getItem() {
		return item;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((item == null) ? 0 : item.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Event other = (Event) obj;
		if (item == null) {
			if (other.item != null)
				return false;
		} else if (!item.equals(other.item))
			return false;
		return true;
	}
}
