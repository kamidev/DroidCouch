package se.msc.android.droidcouch.tests;

import junit.framework.TestSuite;
import android.test.suitebuilder.TestSuiteBuilder;

public class DroidTestSuite extends junit.framework.TestSuite {
	
	
    public static TestSuite suite() {
        return(new TestSuiteBuilder(DroidTestSuite.class)
                        .includeAllPackagesUnderHere()
                        .build());
} 
}
