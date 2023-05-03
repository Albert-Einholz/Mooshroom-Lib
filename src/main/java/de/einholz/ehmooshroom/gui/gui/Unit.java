package de.einholz.ehmooshroom.gui.gui;

import java.text.NumberFormat;

// based on https://github.com/CottonMC/UniversalComponents/blob/master/src/main/java/io/github/cottonmc/component/data/impl/SimpleUnit.java

/*
 * Copyright 2019 B0undarybreaker (Meredith Espinosa)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public class Unit {
	public static final NumberFormat FORMAT_STANDARD = NumberFormat.getNumberInstance();
	static {
		FORMAT_STANDARD.setMinimumFractionDigits(2);
		FORMAT_STANDARD.setMaximumFractionDigits(2);
	}

	// Fluids
	// TODO whatever the hell is gonna go on with fluids in Fabric
	public static final Unit BUCKETS_ANY = new Unit("BU", 0x283593); //800 indigo
	public static final Unit BUCKETS_WATER = new Unit("BU", 0x1976D2); //700 blue
	public static final Unit BUCKETS_LAVA  = new Unit("BU", 0xFF8F00); //800 amber
	// Data
	public static final Unit BYTES = new Unit("B", 0x76FF03); //A400 light green
	// Electricity
	// TODO use custom #005A5A, Material A700 teal, or Material 800 teal?
	public static final Unit ELECTRICITY = new Unit("Wh", 0x00BFA5); //A700 teal
	public static final Unit ELECTRICITY_PER_TICK = new Unit("Wh/t", 0x00BFA5); //Also A700 teal
	// Temperature
	public static final Unit KELVIN = new Unit("K", 0xFF0000); //Programmer Red
    // Time
    public static final Unit TICKS = new Unit("ticks", 0xAAAAAA); // Terrified Grey
    // Other
	public static final Unit PERCENT = new Unit("%", NumberFormat.getIntegerInstance(), 0xAAAAAA, false); // Terrified Grey

	// private static final long QUETTA = 1_000_000_000_000_000_000_000_000_000_000L;
	// private static final long RONNA  = 1_000_000_000_000_000_000_000_000_000L;
	// private static final long YOTTA  = 1_000_000_000_000_000_000_000_000L;
	// private static final long ZETTA  = 1_000_000_000_000_000_000_000L;
	private static final long EXA    = 1_000_000_000_000_000_000L;
	private static final long PETA   = 1_000_000_000_000_000L;
	private static final long TERA   = 1_000_000_000_000L;
	private static final long GIGA   = 1_000_000_000L;
	private static final long MEGA   = 1_000_000L;
	private static final long KILO   = 1_000L;

	// private static final long MILLI  = 1/1_000L;
	// private static final long MICRO  = 1/1_000_000L;
	// private static final long NANO   = 1/1_000_000_000L;
	// private static final long PICO   = 1/1_000_000_000_000L;
	// private static final long FEMPTO = 1/1_000_000_000_000_000L;
	// private static final long ATTO   = 1/1_000_000_000_000_000_000L;
	// private static final long ZEPTO  = 1/1_000_000_000_000_000_000_000L;
	// private static final long YOCTO  = 1/1_000_000_000_000_000_000_000_000L;
	// private static final long RONTO  = 1/1_000_000_000_000_000_000_000_000_000L;
	// private static final long QUECTO = 1/1_000_000_000_000_000_000_000_000_000_000L;
    
    private final String abbr;
    private final NumberFormat format;
    private final int color;
    private final String space;

    public Unit(String abbr, int color) {
        this(abbr, FORMAT_STANDARD, color, true);
    }

    public Unit(String abbr, NumberFormat format, int color, boolean space) {
        this.abbr = abbr;
        this.format = format;
        this.color = color;
        this.space = space ? " " : "";
    }

	public String format(long l) {
		if (l == 0) return format.format(l) + space + abbr;
		//else if (l == Long.MAX_VALUE) return "∞" + space + abbr;
		//else if (l == Long.MIN_VALUE) return "-∞" + space + abbr;
		long magnitude = Math.abs(l);
		if (magnitude >= EXA) return FORMAT_STANDARD.format(l/EXA) + space + "E" + abbr;
		else if (magnitude >= PETA) return FORMAT_STANDARD.format(l/PETA) + space + "P" + abbr;
		else if (magnitude >= TERA) return FORMAT_STANDARD.format(l/TERA) + space + "T" + abbr;
		else if (magnitude >= GIGA) return FORMAT_STANDARD.format(l/GIGA) + space + "G" + abbr;
		else if (magnitude >= MEGA) return FORMAT_STANDARD.format(l/MEGA) + space + "M" + abbr;
		else if (magnitude >= KILO) return FORMAT_STANDARD.format(l/KILO) + space + "k" + abbr;
		else return FORMAT_STANDARD.format(l) + space + abbr;
	}

    public int getColor() {
        return color;
    }
}
