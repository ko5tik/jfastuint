# This is a fork.

This is a fork of JUINT implementation.  Why?  Because I want it mutable.  When you need 
high performance in java, the first rule is to save on garbage collection. The best way is to 
not make garbage. Mutable objects are the best can help to reduce GC pressure and new allocations.

Another source of grieg, are small arrays – array is an object and produces considerable overhead.  Using 
a big partitioned array can help keep memory usage low. 

Due to excessive rewriting of the code, it is unlikely that it will be merged back.  But binary interface
stays the same. All licensing conditions from the original project apply.

## Orginal project

See original documentation [here](https://github.com/nervous-systems/java-unsigned-integers).

## Performance

Needs to be investigated. 

## Building and Releasing

To compile and install the project locally (without signing the artifacts, and installing into the local repository):
```bash
mvn clean install
```

To sign and deploy the artifacts (e.g., to OSSRH / Maven Central):
```bash
mvn clean deploy -P release
```

## License

Like OpenJDK itself, `jfastuint` is distributed under the terms of the _GNU General
Public License_ (version 2) **with the classpath exception**:

> ...The copyright holders of this library give you permission to link this
> library with independent modules to produce an executable, regardless of the
> license terms of these independent modules...

Please see the accompanying `LICENSE` file for details.

### GPL Notes

As mentioned, `jfastuint` offers an interface compatible with `BigInteger`, and
follows a similar strategy to OpenJDK for division and the `(String, int)`
constructor.  While I don't consider it a _derived work_, I don't want to have
to explain that to Gavin Belson in a courtroom.

Despite a strong personal preference for Public Domain software, retaining
OpenJDK's license seems the prudent choice, and doesn't place additional burden
on those consuming this project.
