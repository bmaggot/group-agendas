package com.groupagendas.groupagenda.metadata;

import com.groupagendas.groupagenda.metadata.anno.ValueConversion;

/**
 * Allows retrieving an object of a different type than is saved in the database.
 * 
 * @author Tadas
 */
public enum TypeConversion {
	NONE(null),
	NUMERIC_BOOLEAN(new ValueTypeConverter() {
		@ValueConversion(toRuntimeType = true, forJSON = false)
		public Boolean getFromDatabase(Integer dbValue) {
			return (dbValue != 0);
		}

		@ValueConversion(toRuntimeType = true, forJSON = false)
		public Boolean getFromDatabase(Long dbValue) {
			return (dbValue != 0);
		}

		@ValueConversion(toRuntimeType = true, forJSON = false)
		public Boolean getFromDatabase(Short dbValue) {
			return (dbValue != 0);
		}

		@ValueConversion(toRuntimeType = true, forJSON = false)
		public Boolean getFromDatabase(Byte dbValue) {
			return (dbValue != 0);
		}

		@ValueConversion(toRuntimeType = true, forJSON = false)
		public Boolean getFromDatabase(Character dbValue) {
			return (dbValue != 0);
		}

		@ValueConversion(toRuntimeType = false, forJSON = false)
		public Integer getIntFromRuntime(Boolean rtValue) {
			return rtValue ? 1 : 0;
		}

		@ValueConversion(toRuntimeType = false, forJSON = false)
		public Long getLongFromRuntime(Boolean rtValue) {
			return rtValue ? 1L : 0L;
		}

		@ValueConversion(toRuntimeType = false, forJSON = false)
		public Short getShortFromRuntime(Boolean rtValue) {
			return (short) (rtValue ? 1 : 0);
		}

		@ValueConversion(toRuntimeType = false, forJSON = false)
		public Byte getByteFromRuntime(Boolean rtValue) {
			return (byte) (rtValue ? 1 : 0);
		}

		@ValueConversion(toRuntimeType = false, forJSON = false)
		public Character getCharFromRuntime(Boolean rtValue) {
			return (char) (rtValue ? 1 : 0);
		}
	});
	
	private ValueTypeConverter inflater;
	
	private TypeConversion(ValueTypeConverter inflater) {
		this.inflater = inflater;
	}
	
	public ValueTypeConverter getConverter() {
		return inflater;
	}
}
