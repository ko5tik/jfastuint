# Architectural invariants and goals for Arrays class
- high-performance unsigned int implementation for 128 and 256 bit
- always use the full amount of bits/ never shorten a backing array
- use little endian data arrangement in backing arrays
- avoid allocation of new arrays at all costs (except for creating new instance by immutable methods)
- do not expand data type. in case of overflow, abort computation and return overflow flag
- provide 2 versions of methods. one version constructs new array and calls mutable version which modifies supplied array and returns the new array
- use threadlocal scrathcpad for temporary data arrays

# Architectural invariants and goals for Uint  Uint128 and Uint256
- provide basic arythmetic methods from BigInteger
- delegate to  Arrays class
- provide 2 variants of methods -  imutable (creates new instance form immutable Arrays class mehtod)  and mutable (modifies own array)