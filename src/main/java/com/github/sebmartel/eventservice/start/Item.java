/**
 * 
 */
package com.github.sebmartel.eventservice.start;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author seb
 *
 */

class LongToDateSerializer extends JsonSerializer<Long> {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
	{
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Override
	public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
  	
		gen.writeString( sdf.format( Date.from(Instant.ofEpochMilli(value)) ) );
	}	
}

public class Item {
	
	@JsonSerialize(using=LongToDateSerializer.class)
	private final long timestamp;
	private final long id;
	
	public Item(long timestamp, long id) {
		this.timestamp = timestamp;
		this.id = id;
	}
	
	@JsonCreator
	public Item(@JsonProperty("timestamp") String timestamp, @JsonProperty("id") long id) {
		this(Instant.parse(timestamp).toEpochMilli(), id);
	}
	
	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
		Item other = (Item) obj;
		if (id != other.id)
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}
	
	
}
