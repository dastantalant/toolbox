package com.toolbox.io.util;

import jdk.incubator.vector.ByteVector;
import jdk.incubator.vector.VectorSpecies;

public class VectorSupport {
  private VectorSupport() {}

  public static final VectorSpecies<Byte> SPECIES;
  public static final int LEN;
  public static final long FULL_MASK;

  static {
    VectorSpecies<Byte> species = ByteVector.SPECIES_PREFERRED;

    // We need to store the mask as a Long field instead of a VectorMask[Byte]
    // to get proper optimization from HotSpot so we're limited to 64 lanes.
    if(species.length() > 64) species = ByteVector.SPECIES_512;

    SPECIES = species;
    LEN = SPECIES.length();
    FULL_MASK = -1L << (64- LEN) >>> (64- LEN);
  }
}
