# cogitate
Simple blockchain browser for Thought Network.

### Building ###
Building cogitate requires Java 8 (or higher) Development Kit and Maven.

cogitate depends on the [thought4j RPC library] (https://github.com/thoughtnetwork/thought4j).  
Clone and install thought4j before building jtminer.

`git clone https://github.com/thoughtnetwork/thought4j.git`  
`cd thought4j`  
`mvn install`  

Once thought4j is installed, clone and build cogitate.

`git clone https://github.com/thoughtnetwork/cogitate`  
`cd cogitate`  
`mvn install`  

The build will produce a shaded jar file in the target directory of the repository.  
