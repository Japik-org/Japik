package com.japik.properties;

import java.util.Properties;

public final class ProjectProperties extends Properties {

    public String getVersion(){
        return getProperty("version");
    }

    public String getArtifactId(){
        return getProperty("artifactId");
    }
}
