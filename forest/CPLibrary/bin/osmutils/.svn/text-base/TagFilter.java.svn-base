package osmutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.osmosis.core.domain.v0_6.Tag;


public class TagFilter {
	/** Included tags map. */
	private Map<String, Set<String>> include;
	
	/** Excluded tags map. */
	private Map<String, Set<String>> exclude;
	
	
	public TagFilter() {
		this.include = new HashMap<String, Set<String>>();
		this.exclude = new HashMap<String, Set<String>>();
	}
	
	public void addIncludedTag(String key, String value) {
		
		Set<String> values = include.get(key);
		
		if(values == null) {
			values = new HashSet<String>();	
		}
		
		values.add(value);
		
		include.put(key, values);
		
	}
	
	public void addExcludedTag(String key, String value) {
		
		Set<String> values = exclude.get(key);
		
		if(values == null) {
			values = new HashSet<String>();
		}
		
		values.add(value);
		
		exclude.put(key, values);
	}
	
	public boolean allow(Collection<Tag> tags) {
		
		if (include.containsKey("*")) {
			return true;
		} else {
		
			boolean inc = false;
			boolean exc = false;
			
			for (Tag tag : tags) {
				
				Set<String> includes = include.get(tag.getKey());
				Set<String> excludes = exclude.get(tag.getKey());
				
				if ((includes != null) && (includes.contains(tag.getValue()) || includes.contains("*"))) {
					inc = true;
				}
				if ((excludes != null) && (excludes.contains(tag.getValue()) || excludes.contains("*"))) {
					exc = true;
				}
				
				// We can finish if both are changed.
				if (inc && exc) {
					break;
				}
				
			}
			return inc && !exc;
		}
		
	}
	
	
	public boolean allow(Map<String, Set<String>> tags) {
		
		if (include.containsKey("*")) {
			return true;
		} else {
		
			boolean inc = false;
			boolean exc = false;
			
			for (String key : tags.keySet()) {
				
				Set<String> includes = include.get(key);
				Set<String> excludes = exclude.get(key);
				
				
				// Check wildcards 
				if (includes.contains("*")) {
					inc = true;
				}
				if (excludes.contains("*")) {
					exc = true;
				}
				
				// We can stop if both are changed
				if (inc && exc) {
					break;
				}
				
				Set<String> values = tags.get(key);
				
				// Look for intersection in values for key 
				for (String value : values) {
					if (includes.contains(value)) {
						inc = true;
					}
					if (excludes.contains(value)) {
						exc = true;
					}
				}
				
				// We can stop if both are changed
				if (inc && exc) {
					break;
				}
			}
			
			return inc && !exc;
		}
	}
	
	/** Merge other TagFilter to this one. */
	public void merge(TagFilter other) {
		merge(other, true, true);
	}
	
	
	/** Merge other TagFilter to this one. */
	public void merge(TagFilter other, boolean includes, boolean excludes) {
		
		if (includes) {
			for (String key : other.include.keySet()) {
				for (String value : other.include.get(key)) {
					addIncludedTag(key, value);
				}
			}
		}
		
		if (excludes) {
			for (String key : other.exclude.keySet()) {
				for (String value : other.exclude.get(key)) {
					addIncludedTag(key, value);
				}
			}
		}
		
	}
	
	
	// TODO RFCT deklarace promenny uprostred souboru
	private static Map<Collection<Tag>, Map<String, Set<String>>> mapCache = new HashMap<Collection<Tag>, Map<String,Set<String>>>();
	
	public static Map<String, Set<String>> tags2map(Collection<Tag> tags) {
		
		Map<String, Set<String>> map = mapCache.get(tags);
		
		if (map == null) {
			map = new HashMap<String, Set<String>>();
			
			for (Tag tag : tags) {
				Set<String> set = map.get(tag.getKey());
				
				if (set == null) {
					set = new HashSet<String>();
					map.put(tag.getKey(), set);
				}
				
				set.add(tag.getValue());
			}
			
			mapCache.put(tags, map);
		}
		
		return map;
	}
	
	
}
