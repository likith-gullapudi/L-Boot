// AppConfig.java
package org.likith.config;

import com.google.gson.Gson;
import org.likith.annotations.LBean;
import org.likith.annotations.LConfiguration;

@LConfiguration
public class AppConfig {

    @LBean
    public Gson gson() {
        return new Gson();
    }

}