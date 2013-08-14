/*
 * Copyright (c) 2013, Intel Corporation. 
 * All rights reserved.
 * 
 * The contents of this file are released under the BSD license, you may not use this file except in compliance with the License.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.intel.mtwilson.io;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.EnvironmentConfiguration;

/**
 * Automatically tries an ALL_CAPS version of a preference name.
 * For example, the preferenec mtwilson.ssl.required may be set as a JVM
 * property or in a configuration file, but as an environment variable it 
 * is most likely to be called MTWILSON_SSL_REQUIRED. 
 * 
 * This Configuration implementation automatically converts dot.names to ALL_CAPS
 * to find them in the environment. It wraps the Apache Commons EnvironmentConfiguration.
 * 
 * Because the EnvironmentConfiguration does not support modifying any of the
 * environment variables, this class does not support that either.
 * 
 * Recommended usage:  Use a CompositeConfiguration, add EnvironmentConfiguration to it first and then add AllCapsEnvironmentConfiguration.
 * That way, if you access environment variables that do exist you will get them without a performance penalty. But, 
 * if you use dot.notation and they exist as ALL_CAPS instead of the dot.notation this class will find them for you.
 * 
 * @author jbuhacoff
 */
public class AllCapsEnvironmentConfiguration extends AbstractConfiguration {
    private EnvironmentConfiguration env;
    private Pattern camelCase = Pattern.compile("[a-z][A-Z]"); // TODO: use the character classes to support all unicode camelcase
    
    public AllCapsEnvironmentConfiguration() {
        env = new EnvironmentConfiguration();
    }
    
    /**
     * The transformation of dot.camelCase to ALL_CAPS:
     * All letters are uppercased.
     * Dots are converted to underscores.
     * CamelCase is converted to SEPARATE_WORDS.
     * @param propertyName
     * @return all-uppercase version of property name, dots converted to underscores, and camelCase words separated by underscore
     */
    public String toAllCaps(String propertyName) {
        StringBuilder underscoreWords = new StringBuilder();
        Matcher m = camelCase.matcher(propertyName);
        int cur = 0;
        while( m.find() ) {
            int end = m.end(); // one after the uppercase character
            underscoreWords.append(propertyName.substring(cur, end-1)).append('_');
            cur = end-1;
        }
        underscoreWords.append(propertyName.substring(cur));
        String allCaps = underscoreWords.toString().toUpperCase().replace(".", "_");
        return allCaps;
    }
    
    @Override
    protected void addPropertyDirect(String string, Object o) {
        env.addProperty(string, o);
    }

    @Override
    public boolean isEmpty() {
        return env.isEmpty();
    }

    @Override
    public boolean containsKey(String string) {
        return env.containsKey(toAllCaps(string));
    }

    @Override
    public Object getProperty(String string) {
        return env.getProperty(toAllCaps(string));
    }

    /**
     * 
     * @return the real names of environment variables; does not modify output
     */
    @Override
    public Iterator<String> getKeys() {
        return env.getKeys();
    }
    
}
