package se.msc.android.droidcouch.tests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import se.msc.android.droidcouch.CouchConflictException;
import se.msc.android.droidcouch.CouchDatabase;
import se.msc.android.droidcouch.CouchDesignDocument;
import se.msc.android.droidcouch.CouchException;
import se.msc.android.droidcouch.CouchJsonDocument;
import se.msc.android.droidcouch.CouchQuery;
import se.msc.android.droidcouch.CouchServer;
import se.msc.android.droidcouch.ICouchDocument;
import android.test.AndroidTestCase;


public class CouchTest extends AndroidTestCase {
    /// <summary>
    /// Unit tests for Divan. Operates in a separate CouchDB database called divan_unit_tests.
    /// If you are not running a CouchDB on localhost:5984 you will need to edit
    /// the Tests/App.config file.
    /// 
    /// Run from command line using something like:
    /// 	nunit-console2 --labels -run=Divan.Test.CouchTest Tests/bin/Debug/Tests.dll
    /// </summary>
//        #region Setup/Teardown

    public void SetUp()
    {
	    String host = "localhost";
	    int port = 5984;
        server = new CouchServer(host, port);
        try {
			db = server.GetNewDatabase(DbName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void TearDown()
    {
        try {
			db.Delete();
		} catch (CouchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private CouchServer server;
    private CouchDatabase db;
    private String DbName = "divan_unit_tests";

    // Test
    public void testShouldCheckChangedDocument()
    {
        CouchJsonDocument doc;
        CouchJsonDocument doc2;
		try {
			doc = db.CreateDocument("{\"CPU\": \"Intel\"}");
	        doc2 = db.GetDocument(doc.Id());
	        assertTrue(db.HasDocumentChanged(doc));
	        doc2.Obj.put("CPU", "AMD");
	        db.WriteDocument(doc2);
	        assertTrue(db.HasDocumentChanged(doc));
		} catch (Exception e) {
			assertTrue(false);
		}
    }

    // [Test]
    public void testShouldCountDocuments()
    {
        assertTrue(db.CountDocuments() == 0);
        try {
			db.CreateDocument("{\"CPU\": \"Intel\"}");
		} catch (CouchException e) {
			assertTrue(false);
			return;
		} 
        assertEquals(db.CountDocuments(), 1);
    }

    // [Test]
    public void testShouldCreateDocument()
    {
        CouchJsonDocument doc = new CouchJsonDocument("{\"CPU\": \"Intel\"}");
        ICouchDocument cd;
		try {
			cd = db.CreateDocument(doc);
		} catch (CouchException e) {
			assertTrue(false);
			return;
		}
        assertTrue(db.CountDocuments() == 1);
        assertNotNull(cd.Id());
        assertNotNull(cd.Rev());
    }

    // [Test]
    public void testShouldCreateDocuments()
    {
        String doc = "{\"CPU\": \"Intel\"}";
        CouchJsonDocument doc1 = new CouchJsonDocument(doc);
        CouchJsonDocument doc2 = new CouchJsonDocument(doc);
        ArrayList<ICouchDocument> list = new ArrayList<ICouchDocument> ();
        list.add(doc1);
        list.add(doc2);
        try {
			db.SaveDocuments(list, true);
		} catch (CouchException e) {
			assertTrue(false);
			return;
		}
        assertTrue(db.CountDocuments() == 2);
        assertNotNull(doc1.Id());
        assertNotNull(doc1.Rev());
        assertNotNull(doc2.Id());
        assertNotNull(doc2.Rev());
        assertTrue(doc1.Id() != doc2.Id());
    }

    // [Test, ExpectedException(typeof (CouchNotFoundException))]
    public void testShouldDeleteDatabase()
    {
        try {
			db.Delete();
		} catch (CouchException e1) {
			assertTrue(false);
			return;
		}
        assertFalse(server.HasDatabase(db.Name()));
        boolean f = false;
        try {
        	server.DeleteDatabase(db.Name()); // one more time should fail
        } catch (CouchException e) {
			f = true;
		}
        assertTrue(f);
    }

    // [Test]
    public void testShouldDeleteDocuments()
    {
        String doc = "{\"CPU\": \"Intel\"}";
        try {
	        CouchJsonDocument doc1 = db.CreateDocument(doc);
	        CouchJsonDocument doc2 = db.CreateDocument(doc);
	        if (doc1.Id().compareTo(doc2.Id()) < 0)
	        {
	            db.DeleteDocuments(doc1.Id(), doc2.Id());
	        }
	        else
	        {
	            db.DeleteDocuments(doc2.Id(), doc1.Id());
	        }
	        assertFalse(db.HasDocument(doc1.Id()));
	        assertFalse(db.HasDocument(doc2.Id()));
        } catch (CouchException e) {
        	assertTrue(false);
        	return;
        }
    }

    // [Test, ExpectedException(typeof (CouchException))]
    public void testShouldFailCreateDatabase()
    {
        boolean f = false;
        try {
            server.CreateDatabase(db.Name()); // one more time should fail
        } catch (CouchException e) {
			f = true;
		}
        assertTrue(f);
    }

    // [Test]
    public void testShouldGetDatabaseNames()
    {
        boolean result = server.GetDatabaseNames().contains(db.Name());
        assertTrue(result);
    }

    // [Test]
    public void testShouldGetDocument()
    {
        String doc = "{\"CPU\": \"Intel\"}";
        CouchJsonDocument oldDoc;
        CouchJsonDocument newDoc;
		try {
			oldDoc = db.CreateDocument(doc);
	        newDoc = db.GetDocument(oldDoc.Id());
	        assertEquals(oldDoc.Id(), newDoc.Id());
	        assertEquals(oldDoc.Rev(), newDoc.Rev());
		} catch (CouchException e) {
			e.printStackTrace();
			assertTrue(false);
		}
    }

    // [Test]
    public void testShouldGetDocuments()
    {
        String doc = "{\"CPU\": \"Intel\"}";
        CouchJsonDocument doc1;
        CouchJsonDocument doc2;
		try {
			doc1 = db.CreateDocument(doc);
			doc2 = db.CreateDocument(doc);
	        ArrayList<String> ids = new ArrayList<String>();
	        ids.add(doc1.Id());
	        ids.add(doc2.Id());

	        // Bulk request for multiple keys.
	        List<CouchJsonDocument> docs = db.GetDocuments(ids);
	        assertEquals(docs.size(), 2);
	        assertEquals(doc1.Id(), docs.get(0).Id());
	        assertEquals(doc2.Id(), docs.get(1).Id());

	        ArrayList<String> keys = new ArrayList<String> ();
	        keys.add(doc1.Id());
	        keys.add(doc2.Id());
	        // Bulk query on a view for multple keys.
	        docs = db.QueryAllDocuments().Keys(keys).IncludeDocuments().GetResult().Documents();
	        assertEquals(doc1.Id(), docs.get(0).Id());
	        assertEquals(doc2.Id(), docs.get(1).Id());
		} catch (CouchException e) {
			e.printStackTrace();
		}
    }

    // [Test]
    public void testShouldReturnNullWhenNotFound()
    {
    	try {
	        CouchJsonDocument doc = db.GetDocument("jadda");
	        assertNull(doc);
	        CouchJsonDocument doc2 = db.GetDocument("jadda");
	        assertNull(doc2);
		} catch (CouchException e) {
			e.printStackTrace();
		}
    }

    // [Test]
    public void testShouldSaveDocumentWithId()
    {
    	try {
	    	CouchJsonDocument doc = new CouchJsonDocument("{\"_id\":\"123\", \"CPU\": \"Intel\"}");
	        ICouchDocument cd = db.SaveDocument(doc);
	        assertEquals(db.CountDocuments(), 1);
	        assertNotNull(cd.Id());
	        assertNotNull(cd.Rev());
		} catch (CouchException e) {
			e.printStackTrace();
		}
    }

    // [Test]
    public void testShouldSaveDocumentWithoutId()
    {
    	try {
    		CouchJsonDocument doc = new CouchJsonDocument("{\"CPU\": \"Intel\"}");
    		ICouchDocument cd = db.SaveDocument(doc);
    		assertEquals(db.CountDocuments(), 1);
    		assertNotNull(cd.Id());
    		assertNotNull(cd.Rev());
    	} catch (CouchException e) {
    		e.printStackTrace();
    	}
    }

    // [Test]
    public void testShouldStoreGetAndDeleteAttachment()
    {
    	try {
	    	CouchJsonDocument doc = new CouchJsonDocument("{\"CPU\": \"Intel\"}");
	        ICouchDocument cd = db.CreateDocument(doc);
	        assertFalse(db.HasAttachment(cd));
	        db.WriteAttachment(cd, "jabbadabba", "text/plain");
	        assertTrue(db.HasAttachment(cd));
	        assertEquals(db.ReadAttachment(cd), "jabbadabba");
	        db.WriteAttachment(cd, "jabbadabba-doo", "text/plain");
	        assertTrue(db.HasAttachment(cd));
	        assertEquals(db.ReadAttachment(cd), "jabbadabba-doo");
	        db.DeleteAttachment(cd);
	        assertFalse(db.HasAttachment(cd));
		} catch (CouchException e) {
			e.printStackTrace();
		}
        
    }

    // [Test, ExpectedException(typeof (CouchConflictException))]
    public void testShouldThrowConflictExceptionOnAlreadyExists()
    {
    	try {
	        String doc = "{\"CPU\": \"Intel\"}";
	        CouchJsonDocument doc1 = db.CreateDocument(doc);
	        CouchJsonDocument doc2 = new CouchJsonDocument(doc);
	        doc2.Id(doc1.Id());
	        boolean f = false;
	        try {
	            db.WriteDocument(doc2);
	        } catch (CouchConflictException e) {
				f = true;
			}
	        assertTrue(f);
		} catch (CouchException e) {
			e.printStackTrace();
		}

    }

    // [Test, ExpectedException(typeof (CouchConflictException))]
    public void testShouldThrowConflictExceptionOnStaleWrite()
    {
    	try {
	        String doc = "{\"CPU\": \"Intel\"}";
	        CouchJsonDocument doc1 = db.CreateDocument(doc);
	        CouchJsonDocument doc2 = db.GetDocument(doc1.Id());
	        doc1.Obj.putOpt("CPU", "AMD");
	        db.SaveDocument(doc1);
	        doc2.Obj.putOpt("CPU","Via");
	        boolean f = false;
	        try {
	            db.SaveDocument(doc2);
	        } catch (CouchConflictException e) {
				f = true;
			}
	        assertTrue(f);
		} catch (CouchException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }

    // [Test]
    public void testShouldUseETagForView()
    {
    	try {
	        CouchDesignDocument design = db.NewDesignDocument("computers");
	        design.AddView("by_cpumake",
	                       "function(doc) { emit(doc.CPU, doc); }");
	        db.WriteDocument(design);
	
	        CouchJsonDocument doc1 = db.CreateDocument("{\"CPU\": \"Intel\"}");
	        db.CreateDocument("{\"CPU\": \"AMD\"}");
	        db.CreateDocument("{\"CPU\": \"Via\"}");
	        db.CreateDocument("{\"CPU\": \"Sparq\"}");
	
	        CouchQuery query = db.Query("computers", "by_cpumake").StartKey("Intel").EndKey("Via").CheckETagUsingHead();
	        // Query has no result yet so should not be cached
	        assertFalse(query.IsCachedAndValid());
	        query.GetResult();
	        // Now it is cached and should be valid
	        assertTrue(query.IsCachedAndValid());
	        // Make a change invalidating the view
	        db.SaveDocument(doc1);
	        // It should now be false
	        assertFalse(query.IsCachedAndValid());
	        query.GetResult();
	        // And now it should be cached again
	        assertTrue(query.IsCachedAndValid());
	        query.GetResult();
	        // Still cached of course
	        assertTrue(query.IsCachedAndValid());
		} catch (CouchException e) {
			e.printStackTrace();
		}
	}

    // [Test]
    public void testShouldWriteDocument()
    {
    	try {
	    	CouchJsonDocument doc = new CouchJsonDocument("{\"_id\":\"123\", \"CPU\": \"Intel\"}");
	        ICouchDocument cd = db.WriteDocument(doc);
	        assertEquals(db.CountDocuments(), 1);
	        assertNotNull(cd.Id());
	        assertNotNull(cd.Rev());
		} catch (CouchException e) {
			e.printStackTrace();
		}
   }

    // [Test]
    public void testShouldSyncDesignDocuments()
    {
    	try {
	        CouchDesignDocument design = db.NewDesignDocument("computers");
	        design.AddView("by_cpumake", "function(doc) { emit(doc.CPU, doc); }");
	        db.SynchDesignDocuments(); // This writes them to the db.
	
	        CouchDatabase db2 = server.GetDatabase(DbName);
	        design = db2.NewDesignDocument("computers");
	        design.AddView("by_cpumake", "function(doc) { emit(doc.CPU, nil); }");
	        db2.SynchDesignDocuments(); // This should detect difference and overwrite the one in the db
	
	        assertEquals(
	        		db.GetDocument(CouchDesignDocument.class,"_design/computers").Definitions.get(0).Map,
	        		"function(doc) { emit(doc.CPU, doc); }"
	                );
		} catch (CouchException e) {
			e.printStackTrace();
		}
    }

    /// <summary>
    /// Test that keys can be given as C# types representing proper JSON values:
    ///  string, number, true, false, null, JSON array and JSON object.
    /// </summary>
    // [Test]
    public void testQueryKeyShouldGiveProperJsonValue()
    {
        CouchQuery query = db.Query("test", "test");
        query.Key("a string");
        assertEquals( query.Options.get("key") , "\"a string\"");
        query.Key(12);
        assertEquals( query.Options.get("key") , "12");
        query.Key(-12.0);
        assertEquals( query.Options.get("key") , "-12.0");
        query.Key(true);
        assertEquals( query.Options.get("key") , "true");
        query.Key(false);
        assertEquals( query.Options.get("key") , "false");
        query.Key(null);
        assertEquals( query.Options.get("key") , "null");

        String[] arr = { "one", "two" };
        query.Key(arr);
        
        String json = query.Options.get("key").replaceAll("\\s", ""); // removes all whitespace
        assertEquals(json, "[\"one\",\"two\"]");

/*        dict = new HashMap<string, string>();
        dict["one"] = "two";
        dict["three"] = "four";
        query.Key(dict);
        json = Regex.Replace(query.Options["key"], @"\s", ""); // removes all whitespace
        Assert.That(json.Equals("{\"one\":\"two\",\"three\":\"four\"}"));
*/        
    }
}

